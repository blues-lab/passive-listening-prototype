import numpy as np
from mss import mss
from PIL import Image
import webbrowser
import os
import time
from mutagen.wave import WAVE

from PIL import Image
import time
from screeninfo import get_monitors
import easyocr
from difflib import SequenceMatcher


def transcribe(filename="recordings/v1/recording1.wav"):
    url = "file://" + os.path.realpath(filename)
    webbrowser.get("/usr/bin/google-chrome").open(url, new=1)
    time.sleep(2)
    # pass
    reader = easyocr.Reader(["en"])

    audio = WAVE(filename)
    audio_time = int(audio.info.length)
    curr_time = 0
    monitor = get_monitors()[0]
    width = monitor.width
    height = monitor.height
    bounding_box = {
        "top": int(height * 3 / 4),
        "left": int(width / 4),
        "width": int(width / 2),
        "height": int(height * 1 / 4),
    }
    audio_start = time.time()
    images = []
    i = 0
    with mss() as sct:
        while curr_time < audio_time:
            start = time.time()
            i += 1
            img = np.asarray(sct.grab(bounding_box))
            if i % 4 == 0:
                im = Image.fromarray(img).convert("RGB")
                im.save(f"screenshot{curr_time}.jpeg")
            images.append(img)
            time.sleep(1)
            end = time.time()
            curr_time += end - start

    elapsed_time = time.time() - audio_start
    print(
        "Finished reading with samples size: ",
        len(images),
        " time elapsed",
        elapsed_time / 60,
        " ",
        elapsed_time % 60,
    )
    res_text = []
    for image in images:
        read_res = reader.readtext(image)
        read_buffer = ""
        for item in read_res:
            data, text, prob = item
            print(data, text, prob)
            if prob > 0.7:
                read_buffer += " " + text
        read_buffer = read_buffer.strip()
        if read_buffer:
            res_text.append(read_buffer)

    print("data output", res_text)
    print("Computing Dedup")
    # output = os.linesep.join([s for s in output.splitlines() if s])
    phrases = compute_deduped_phrases(res_text)
    print("Completed dedup phase 1 with ", phrases)
    merged_phrases = compute_merged_phrases_deduped(phrases)
    print("Completed dedup phase 2 with ", merged_phrases)
    return ". ".join(merged_phrases)


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
