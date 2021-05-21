"""
This script captures a portion of your screen and saves it as an image.
It's useful for testing, because the same code is used in the main program for its screen capture component.
"""

import numpy as np
from mss import mss
from PIL import Image
from screeninfo import get_monitors
import time

if __name__ == "__main__":
    monitor = get_monitors()[0]
    width = monitor.width
    height = monitor.height
    bounding_box = {
        "top": int(height * 3 / 4),
        "left": int(width / 4),
        "width": int(width / 2),
        "height": int(height * 1 / 4),
    }
    images = []
    i = 0
    with mss() as sct:
        start = time.time()
        img = np.asarray(sct.grab(bounding_box))
        im = Image.fromarray(img).convert("RGB")
        im.save(f"screenshot{i}.jpg")
        images.append(img)