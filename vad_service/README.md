Voice activity detection (VAD) service
======================================

This microservice is responsible for deciding whether an audio clip has speech in it.

To decide this, it leverages the [py-webrtcvad](https://github.com/wiseman/py-webrtcvad) library.

It exists as a standalone service, because py-webrtcvad works well without any special setup, whereas there doesn't seem to be anything in Java that could integrate more directly with our Kotlin code.

[Jitsi's webrtcvad wrapper](https://github.com/jitsi/jitsi-webrtc-vad-wrapper) may work, but requires a bit more investigation.


## Prerequisites

You'll need Python 3 and [Poetry](https://python-poetry.org/).

## Setup

```shell
poetry install
poetry shell
```

## Running

    python src/VadServiceMain.py
