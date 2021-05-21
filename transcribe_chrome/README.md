Chrome Transcription Service
======================================
This library opens up and instance of google-chrome to run speech to text transcription given a wav file.

## Prerequisites

You'll need Python 3 and [Poetry](https://python-poetry.org/).

## Setup

```shell
poetry install
poetry shell
```

## Running

    python src/TranscriptionServiceMain.py

## Screen capture on macOS

In recent versions of macOS (Catalina, High Sierra, etc.) there may be an issue with screen capture, where the only thing getting captured is the desktop background and not any of the open issues. This is a [known problem](https://github.com/BoboTiG/python-mss/issues/134) that has to do with the Terminal application not having "Screen Recording" permissions. To fix this, go to `System Preferences -> Security & Privacy -> Screen Recording -> Terminal` and make sure the checkbox is checked (indicating the app has permission).
