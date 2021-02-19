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


# Errors with Recording
If having errors on the default mac recorder, run the MacRecorder by changing the 
env variable in Record.kt to MacRecorder.

Try running the command 
```
ffmpeg -i hw:4,0 -t 30 -ac 1 -ar 16k test.wav
```
and check that the recording of the file exists
