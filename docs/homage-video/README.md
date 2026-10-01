# MTurk Tracker homage video

A 3-minute, 1080p video covering the demographics survey from 26 March 2015 to
30 September 2026, made when Amazon closed Mechanical Turk.

Scenes: title · calendar of 4,114 days · how many Turkers we met · how long they stayed ·
half-life by era · population estimate · workers available per year · countries ·
US vs India · languages · gender · birth year · US households · answers that changed ·
hours and weekly earnings · thank-you card.

## Data

`merged.sql` defines `all_answers`: the public `demographics.responses` table plus the two
Datastore backups in the `test` dataset (`userAnswers_oct2020`, `UserAnswer_2025MAR20`),
with backup worker IDs hashed the same way as the public table (SHA-256 hex) and one row
per (worker, HIT). The public table alone is missing about 63K answers from 2015–2021
that the backups still hold; merged, the survey has 392,995 answers from 157,255 workers.

## Population estimates

`estimate_population.py` follows Difallah, Filatova & Ipeirotis, *Demographics and
Dynamics of Mechanical Turk Workers* (WSDM 2018), with 30-day periods as capture occasions:

| | Paper (28 periods) | This data, same window | 2015–2026 (140 periods) |
|---|---|---|---|
| Workers seen | 39,461 | 38,753 | 157,255 |
| Chao lower bound | 97,579 | 96,738 | 330,594 |
| Open-population half-life | 404 days | 428 days | 535 / 479 / 255 days (2015–19 / 2020–22 / 2023–26) |
| Equal-catchability active pool | ~12K | 12.2K | — |

The zero-truncated beta-binomial (paper Eq. 4) has its maximum at α → 0 on this data
(the tail of frequent participants is heavier than a single Beta allows), so its N is
unbounded. Finite binomial mixtures fit far better; with 4–6 classes they give 342K–393K
and keep rising as classes are added, so the video shows Chao's bound as the firm number
and the mixtures as a range. Capture histories stay in memory; only aggregates are written.

## Rebuild

```bash
./fetch_data.sh                       # BigQuery -> data/*.csv (aggregates only, no worker IDs)
python3 estimate_population.py        # BigQuery -> data/pop_*.csv
pip install matplotlib pandas numpy scipy   # ffmpeg must be on PATH
INTER_DIR=/path/to/Inter/extras/ttf OUT_DIR=out python3 make_video.py   # video + music.py soundtrack
```

`INTER_DIR` points at the static TTFs from the [Inter](https://github.com/rsms/inter)
release; without it the video falls back to DejaVu Sans. The output is
`out/mturk_tracker_homage.mp4`.

Useful while editing:

```bash
python3 make_video.py stills 4 11.5   # PNG of scene 4 at t=11.5s
python3 make_video.py halflife        # re-render one scene by name
python3 make_video.py music           # regenerate only the soundtrack and re-mux
```

The soundtrack is synthesized by `music.py` (piano, pad and bass in A minor, vi–IV–I–V,
stretched so the closing card lands on the final F → C cadence), so it carries no license.
