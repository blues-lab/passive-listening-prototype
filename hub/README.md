Main service
============

## Prerequisites

The hub currently serves the dashboard, a web page helping display the recordings.
It expects the static files to be on the local filesystem, at the location specified by `DASHBOARD_PATH`.
Everything (but the dashboard) will still work if these are absent.

## Building

See instructions one level up


## Running

See instructions one level up


## Usage

Some arguments are required. Run with `--help` or no args to see them documented.


# Errors with recording
If having errors on the default (Java) recorder, run the FfmpegRecorder by changing the 
variable in Record.kt to FfmpegRecorder.

Additionally, if your preferred microphone isn't the preferred one,
try running the command 
```
ffmpeg -i hw:4,0 -t 30 -ac 1 -ar 16k test.wav
```
and check that the recording of the file exists

If not, you might need to play around with the `-i` parameter.
