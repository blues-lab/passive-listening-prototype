import argparse
import os
import tempfile
import time
import typing
import webbrowser
from difflib import SequenceMatcher
from pathlib import Path
from sys import platform
from warnings import warn

import easyocr
import numpy as np
from mss import mss
from mutagen.wave import WAVE
from PIL import Image
from sclog import getLogger
from screeninfo import get_monitors
from selenium import webdriver
from selenium.webdriver.chrome.options import Options

logger = getLogger(__name__)
# TODO possibly set to tmp or same directory as wavs are stored
transcription_cache_dir = "transcriptionCache" 

def open_chrome(url: str):
    """
    Opens a full screen 
    """
    options = Options()
    options.add_argument(f"user-data-dir={os.getcwd()}/{transcription_cache_dir}")
    options.add_argument("window-size=1920,1280")
    options.add_argument('log-level=3')
    driver = webdriver.Chrome(options=options)
    driver.get(url)
    return driver

def transcribe(
    filename="recordings/v1/recording1.wav",
    screenshot_directory: Path = Path(),
):
    url = "file://" + os.path.realpath(filename)
    driver = open_chrome(url)

    sleep_time_seconds = 2
    logger.debug("waiting %i seconds for the browser to launch", sleep_time_seconds)
    time.sleep(sleep_time_seconds)

    logger.debug("preparing easyocr")
    reader = easyocr.Reader(["en"])

    logger.debug("loading input file (%s) to get metadata", filename)
    audio = WAVE(filename)

    audio_time = int(audio.info.length)
    logger.debug("the file to transcribe is %i seconds long", audio_time)

    monitor = get_monitors()[0]
    width = monitor.width
    height = monitor.height
    logger.debug("the current monitor's dimensions are %i by %i", width, height)

    bounding_box = {
        "top": int(height * 3 / 4),
        "left": int(width / 4),
        "width": int(width / 2),
        "height": int(height * 1 / 4),
    }
    logger.debug("the bounding box for screen capture is %s", str(bounding_box))

    audio_start = time.time()
    images = []
    i = 0

    logger.debug("beginning screen capture (will continue for %i seconds)", audio_time)
    curr_time = 0
    with mss() as sct:
        while curr_time < audio_time:
            start = time.time()
            i += 1
            img = np.asarray(sct.grab(bounding_box))
            if i % 4 == 0:
                im = Image.fromarray(img).convert("RGB")

                screenshot_filename = f"screenshot{curr_time}.jpeg"
                screenshot_path = screenshot_directory / screenshot_filename
                im.save(screenshot_path)

            images.append(img)
            time.sleep(1)
            end = time.time()
            curr_time += end - start

    elapsed_time = time.time() - audio_start
    logger.debug(
        "Finished screen capture. Captured %i images over %i minutes %i seconds",
        len(images),
        elapsed_time / 60,
        elapsed_time % 60,
    )

    logger.debug("extracting text from screenshots")
    res_text = []
    for image in images:
        read_res = reader.readtext(image)
        read_buffer = ""
        for item in read_res:
            data, text, prob = item
            logger.debug("%s %s %f", data, text, prob)
            if prob > 0.7:
                read_buffer += " " + text
        read_buffer = read_buffer.strip()
        if read_buffer:
            res_text.append(read_buffer)

    logger.debug("raw text captured: %s", res_text)
    logger.debug("Computing Dedup")
    if len(res_text) == 1:
        transcription = res_text[0].replace("Live Caption", "")
        logger.info("Transcription %s", transcription)
        return transcription
    
    phrases = compute_deduped_phrases(res_text)
    logger.debug("Completed dedup phase 1 with %s", phrases)
    merged_phrases = compute_merged_phrases_deduped(phrases)
    logger.debug("Completed dedup phase 2 with %s", merged_phrases)
    result = ". ".join(merged_phrases)
    logger.info("transcription: %s", result)

    driver.close()
    return result


def compute_deduped_phrases(buffer):
    # buffer: list[str]
    # Merge common phrases based on last substring
    i = 1
    phrases = []
    start_token = "Live Caption"
    prev_matched_region = ""
    while i < len(buffer):
        string1 = buffer[i - 1].strip()
        string2 = buffer[i].strip()

        string1 = string1.replace(start_token, "")  # Removes Live Caption phrase
        string2 = string2.replace(start_token, "")  # Removes Live Caption phrase

        match = SequenceMatcher(None, string1, string2).find_longest_match(
            0, len(string1), 0, len(string2)
        )
        matched_region = string1[match.a : match.a + match.size].strip()

        if prev_matched_region and prev_matched_region in matched_region:
            if len(phrases) > 1 and phrases[-1] in prev_matched_region:
                # Case where last phrases contains a portion of last matched region
                # Remove the last entry to remove aliasing
                phrases.pop()
            phrases.append(prev_matched_region)

        prev_matched_region = matched_region
        i += 1
    phrases.append(prev_matched_region)
    return phrases


def merge(s1, s2):
    # Merges s1 with s2 based on end of s1 to start of s2
    # This function is pretty slow as it's run on a buffer that's not too long
    # TODO Optimization start from the end and go backwards
    i = 0
    found_match = True
    while not s2.startswith(s1[i:]):
        i += 1
        if i == len(s1):
            found_match = False
            break
    return s1[:i] + s2, found_match


def compute_merged_phrases_deduped(phrases):
    # phrases: list[str]
    # Merge phrases that have common ending such as
    # "We ate food" + "ate food quickly" -> "We ate food quickly"
    j = 1
    merged_phrases = []
    prev_phrase = phrases[0]
    while j < len(phrases):
        curr_phrase = phrases[j]
        merged_phrase, found_match = merge(prev_phrase, curr_phrase)
        if not found_match:
            # End of merge sequence. Set prev_phase to current and repeat
            merged_phrases.append(prev_phrase)
            prev_phrase = curr_phrase
        else:
            prev_phrase = merged_phrase

        j += 1
    merged_phrases.append(prev_phrase)
    return merged_phrases


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="transcribe given WAV file")
    parser.add_argument("filename", help="the name of the file to transcribe")
    parser.add_argument(
        "--screenshot-dir", required=False, help="directory to store screenshot files"
    )
    args = parser.parse_args()

    filename = args.filename

    output_filename = filename + ".txt"
    output_path = Path(output_filename)
    if output_path.exists():
        warn(f"output file {output_path} already exists")

    input_file_path = Path(filename)
    if input_file_path.suffix != ".wav":
        raise ValueError(
            f"transcription requires a .wav file, and {input_file_path} doesn't seem to be one"
        )

    screenshot_dir = args.screenshot_dir
    if screenshot_dir is None:
        screenshot_dir = Path(tempfile.mkdtemp())
    else:
        screenshot_dir = Path(args.screenshot_dir)
    logger.debug("will save screenshots to %s", screenshot_dir)

    transcription = transcribe(filename, screenshot_dir)
    print(transcription)
    with open(output_path, "w") as output:
        output.write(transcription)
