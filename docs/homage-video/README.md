# MTurk Tracker homage video

A 2-minute, 1080p video covering the demographics survey from 26 March 2015 to
29 September 2026, made when Amazon closed Mechanical Turk.

Scenes: title · calendar of 4,113 days · how many Turkers (138,462 workers, answers per
worker) · how long they stayed · capture–recapture estimate of the active pool ·
countries · gender · birth year · answers that changed (married, college) ·
hours and weekly earnings · thank-you card.

## Rebuild

```bash
./fetch_data.sh                       # BigQuery -> data/*.csv (aggregates only, no worker IDs)
pip install matplotlib pandas numpy   # ffmpeg must be on PATH
INTER_DIR=/path/to/Inter/extras/ttf OUT_DIR=out python3 make_video.py
```

`INTER_DIR` points at the static TTFs from the [Inter](https://github.com/rsms/inter)
release; without it the video falls back to DejaVu Sans. The output is
`out/mturk_tracker_homage.mp4`.

Useful while editing:

```bash
python3 make_video.py stills 4 11.5   # PNG of scene 4 at t=11.5s
python3 make_video.py pool            # re-render one scene by name
```
