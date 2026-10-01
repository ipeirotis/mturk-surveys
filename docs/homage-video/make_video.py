#!/usr/bin/env python3
"""Renders "MTurk Tracker, 2015-2026", a 3-minute square (1080×1080) homage video, from the
aggregates in ./data (see fetch_data.sh).

    pip install matplotlib pandas numpy      # plus ffmpeg on PATH
    INTER_DIR=/path/to/Inter/extras/ttf python3 make_video.py [scene ...]

Each scene renders in its own process to OUT_DIR/scene_XX.mp4, then the
scenes are joined into OUT_DIR/mturk_tracker_homage.mp4.
"""
import glob
import os
import subprocess
import sys
from multiprocessing import Pool

import matplotlib

matplotlib.use("Agg")
import matplotlib.lines  # noqa: E402
import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import pandas as pd  # noqa: E402
from matplotlib import font_manager  # noqa: E402
from matplotlib.colors import LinearSegmentedColormap  # noqa: E402
from matplotlib.patches import Rectangle  # noqa: E402

import music  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
DATA = os.path.join(HERE, "data")
OUT = os.environ.get("OUT_DIR", os.path.join(HERE, "out"))
FPS = 30
W, H = 1080, 1080

# ---------------------------------------------------------------- style
BG = "#141413"
INK = "#ffffff"
INK2 = "#c3c2b7"
MUTED = "#8a8980"
GRID = "#2b2b28"
AXIS = "#4a4a45"
EMPTY = "#2e2e2a"
BLUE, ORANGE, AQUA = "#3987e5", "#d95926", "#199e70"
BLUE_RAMP = ["#0d366b", "#104281", "#184f95", "#1c5cab", "#256abf", "#2a78d6",
             "#3987e5", "#5598e7", "#6da7ec", "#86b6ef", "#9ec5f4", "#b7d3f6", "#cde2fb"]

FAMILY, DISPLAY = "DejaVu Sans", "DejaVu Sans"
inter_dir = os.environ.get("INTER_DIR")
if inter_dir and os.path.isdir(inter_dir):
    for f in glob.glob(os.path.join(inter_dir, "*.ttf")):
        font_manager.fontManager.addfont(f)
    FAMILY, DISPLAY = "Inter", "Inter Display"

plt.rcParams.update({
    "font.family": FAMILY,
    "figure.facecolor": BG,
    "axes.facecolor": BG,
    "text.color": INK,
    "axes.labelcolor": INK2,
    "xtick.color": INK2,
    "ytick.color": INK2,
})


def clamp(x):
    return max(0.0, min(1.0, x))


def smooth(x):
    x = clamp(x)
    return x * x * (3 - 2 * x)


def eout(x):
    x = clamp(x)
    return 1 - (1 - x) ** 3


def seg(t, a, b, f=eout):
    return f((t - a) / (b - a))


# Type scale, in points on a 1080×1080 frame. A square frame plays almost full width on a
# phone, and the floor (SMALL, about 1/23 of the frame) stays readable there.
SMALL, LABEL, BODY = 34, 38, 44
TITLE = 66
M = 0.06  # side margin


def txt(fig, x, y, s, size=BODY, color=INK, weight="regular", alpha=1.0, ha="left",
        va="baseline", family=None, **kw):
    if alpha <= 0.001:
        return None
    return fig.text(x, y, s, fontsize=size, color=color, fontweight=weight, alpha=clamp(alpha),
                    ha=ha, va=va, family=family or FAMILY, **kw)


def rise(fig, t, t0, x, y, s, dur=0.7, dy=0.012, **kw):
    """Text that fades in while drifting up into place."""
    a = seg(t, t0, t0 + dur)
    return txt(fig, x, y - dy * (1 - a), s, alpha=a, **kw)


def headline(fig, t, *rows):
    """One or two lines of title at the top of the frame."""
    a = seg(t, 0.1, 0.9)
    for i, r in enumerate(rows):
        txt(fig, M, 0.885 - i * 0.085 - 0.01 * (1 - a), r, size=TITLE, weight="bold", alpha=a,
            family=DISPLAY)


def footnote(fig, t, t0, s):
    rise(fig, t, t0, M, 0.035, s, size=SMALL, color=MUTED)


def swatch(fig, x, y, c, alpha, label, size=LABEL):
    fig.patches.append(Rectangle((x, y), 0.036, 0.036, transform=fig.transFigure, color=c,
                                 alpha=alpha))
    txt(fig, x + 0.05, y + 0.004, label, size=size, color=INK, alpha=alpha)


def style_ax(ax, ygrid=True):
    ax.set_facecolor("none")
    for s in ("top", "right", "left"):
        ax.spines[s].set_visible(False)
    ax.spines["bottom"].set_color(AXIS)
    ax.tick_params(colors=INK2, labelsize=SMALL, length=0, pad=10)
    if ygrid:
        ax.grid(axis="y", color=GRID, lw=1.5)
    ax.set_axisbelow(True)


def fmt(n):
    return f"{int(round(n)):,}"


def pct(x, d=0):
    if d == 0 and 0 < x < 0.005:
        return "<1%"
    return f"{100 * x:.{d}f}%"


# ---------------------------------------------------------------- data
def read(name, **kw):
    return pd.read_csv(os.path.join(DATA, name), **kw)


TOT = read("totals.csv").iloc[0]
TOP = read("top_worker.csv").iloc[0]
DAILY = read("daily.csv", parse_dates=["d"])
MONTHLY = read("monthly.csv", parse_dates=["m"])
TASKDIST = read("taskdist.csv")
SPAN = read("span.csv")
CY = read("country_year.csv")
YOB = read("yob_year.csv")
HOURS = read("cat_time_spent_on_mturk.csv")
POP_TOTAL = read("pop_total.csv").iloc[0]
POP_YEARS = read("pop_years.csv")
POP_SEEN = read("pop_times_seen.csv")
POP_HALF = read("pop_halflife.csv")
POP_RETURN = read("pop_return.csv").set_index("gap")
US_IN = read("us_india.csv").set_index("country")
US_HOUSE = read("us_household.csv", keep_default_na=False)
LANGS = read("languages.csv")
LANG_COUNTS = read("lang_counts.csv")
LANG_NAMES = {"Tegulu": "Telugu"}  # spelled that way in the survey form
PAY = read("cat_weekly_income_from_mturk.csv")

START, END = DAILY.d.min(), DAILY.d.max()
YEARS = list(range(START.year, END.year + 1))


def year_frac(ts):
    ts = pd.Timestamp(ts)
    return ts.year + (ts.dayofyear - 1) / (366 if ts.is_leap_year else 365)


MONTHLY["x"] = MONTHLY.m.map(year_frac) + 1 / 24  # centre of month

# Weekly calendar grid: rows = years, cols = week of year
WEEKS = np.full((len(YEARS), 52), np.nan)
WEEK_START = np.empty((len(YEARS), 52), dtype="datetime64[ns]")
for i, y in enumerate(YEARS):
    for w in range(52):
        ws = pd.Timestamp(year=y, month=1, day=1) + pd.Timedelta(days=7 * w)
        WEEK_START[i, w] = ws.to_datetime64()
        we = ws + pd.Timedelta(days=6) if w < 51 else pd.Timestamp(year=y, month=12, day=31)
        if ws.year == y and we >= START and ws <= END:
            WEEKS[i, w] = 0.0
DAILY["row"] = DAILY.d.dt.year - START.year
DAILY["col"] = np.minimum((DAILY.d.dt.dayofyear - 1) // 7, 51)
for (r, c), n in DAILY.groupby(["row", "col"]).n.sum().items():
    WEEKS[r, c] = n
DAILY["cum_n"] = DAILY.n.cumsum()
DAILY["cum_w"] = DAILY.new_w.cumsum()

# Times each worker answered
def bucket(c):
    for lo, hi, lab in ((1, 1, "1×"), (2, 2, "2×"), (3, 5, "3–5×"), (6, 12, "6–12×"),
                        (13, 24, "13–24×"), (25, 48, "25–48×"), (49, 10**6, "49+×")):
        if lo <= c <= hi:
            return lab


TASKDIST["b"] = TASKDIST.cnt_tasks.map(bucket)
BUCKETS = TASKDIST.groupby("b", sort=False).cnt_workers.sum()

# Survival: share of workers whose last answer came >= k*30 days after their first
SPAN = SPAN.set_index("span_bucket").w.reindex(range(0, SPAN.span_bucket.max() + 1), fill_value=0)
SURV = SPAN[::-1].cumsum()[::-1] / SPAN.sum()
SURV_X = np.arange(len(SURV)) * 30 / 365.25

FULL_MONTHS = MONTHLY[MONTHLY.n >= 1000]

# First-time vs returning workers per month
NEW_M = DAILY.groupby(DAILY.d.dt.to_period("M")).new_w.sum()
MONTHLY["new_w"] = MONTHLY.m.dt.to_period("M").map(NEW_M).fillna(0).values
MONTHLY["ret_w"] = MONTHLY.w - MONTHLY.new_w

# Countries
CY_US = CY[CY.country == "US"].set_index("y").n / CY.groupby("y").n.sum()
CY_IN = CY[CY.country == "IN"].set_index("y").n / CY.groupby("y").n.sum()
CW = read("country_year.csv").groupby("country").w.sum()  # upper bound per country, not unique
COUNTRY_NAMES = {"US": "United States", "IN": "India", "GB": "United Kingdom", "CA": "Canada",
                 "BR": "Brazil", "IT": "Italy", "DE": "Germany", "NL": "Netherlands",
                 "ES": "Spain", "FR": "France", "PH": "Philippines", "VE": "Venezuela"}
TOP_COUNTRIES = (CY.groupby("country").n.sum().drop(["ZZ", "?", ""], errors="ignore")
                 .sort_values(ascending=False).head(8))

# Birth years
YOB = YOB[pd.to_numeric(YOB.year_of_birth, errors="coerce").notna()].copy()
YOB["b"] = YOB.year_of_birth.astype(int)
YOB = YOB[(YOB.b >= 1940) & (YOB.b <= 2008)]
YOB_BINS = np.arange(1945, 2007)
YOB_SHARE = {}
YOB_MED = {}
YOB_MAX = 0.0
for y, g in YOB.groupby("y"):
    s = g.groupby("b").n.sum()
    YOB_SHARE[y] = s.reindex(YOB_BINS, fill_value=0).values / s.sum()
    YOB_MAX = max(YOB_MAX, YOB_SHARE[y].max())
    cum = s.sort_index().cumsum() / s.sum()
    YOB_MED[y] = int(cum[cum >= 0.5].index[0])

# Hours and pay (questions added mid-2017)
HOURS_ORDER = ["Less than 1 hour per week", "1-2 hours per week", "2-4 hours per week",
               "4-8 hours per week", "8-20 hours per week", "20-40 hours per week",
               "More than 40 hours per week"]
HOURS_LABELS = ["< 1 h", "1–2 h", "2–4 h", "4–8 h", "8–20 h", "20–40 h", "40+ h"]
PAY_ORDER = ["Less than $1 per week", "$1-$5 per week", "$5-$10 per week", "$10-$20 per week",
             "$20-$50 per week", "$50-$100 per week", "$100-$200 per week",
             "$200-$500 per week", "More than $500 per week"]
PAY_LABELS = ["< $1", "$1–5", "$5–10", "$10–20", "$20–50", "$50–100", "$100–200",
              "$200–500", "$500+"]
HOURS_S = HOURS.groupby("v").n.sum().reindex(HOURS_ORDER)
HOURS_S = HOURS_S / HOURS_S.sum()
PAY_S = PAY.groupby("v").n.sum().reindex(PAY_ORDER)
PAY_N = int(PAY_S.sum())
PAY_S = PAY_S / PAY_S.sum()


INCOME_ORDER = ["Less than $10,000", "$10,000-$14,999", "$15,000-$24,999", "$25,000-$39,999",
                "$40,000-$59,999", "$60,000-$74,999", "$75,000-$99,999", "$100,000 or more"]
INCOME_LABELS = ["< $10K", "$10–15K", "$15–25K", "$25–40K", "$40–60K", "$60–75K", "$75–100K",
                 "$100K+"]
SIZE_ORDER = ["1", "2", "3", "4", "5+"]
SIZE_LABELS = ["1 person", "2", "3", "4", "5 or more"]
_inc = US_HOUSE[US_HOUSE.q == "income"].set_index("v").n.reindex(INCOME_ORDER)
INCOME_S = _inc / _inc.sum()
_size = US_HOUSE[US_HOUSE.q == "size"].assign(v=lambda d: d.v.str.strip()).groupby("v").n.sum()
SIZE_S = _size.reindex(SIZE_ORDER) / _size.reindex(SIZE_ORDER).sum()


def median_label(s, labels):
    return labels[int(np.searchsorted(s.cumsum().values, 0.5))]


def year_mean(col, year):
    g = MONTHLY[MONTHLY.m.dt.year == year]
    return float((g[col] * g.n).sum() / g.n[g[col].notna()].sum())


# ---------------------------------------------------------------- scenes
def day_ticks(fig, t, lit, y=0.2, alpha=1.0):
    """96 small tiles: one day of 15-minute slots."""
    ax = fig.add_axes([M, y, 1 - 2 * M, 0.05])
    ax.axis("off")
    ax.set_xlim(0, 96)
    ax.set_ylim(0, 1)
    colors = [BLUE if i < lit else GRID for i in range(96)]
    ax.bar(np.arange(96) + 0.5, 1, width=0.62, color=colors, alpha=alpha, lw=0)


def bars_h(ax, labels, vals, grow, value_fmt=None, highlight=None, xpad=1.8, height=0.66,
           size=LABEL):
    """Horizontal bars with the value printed at each bar's end."""
    style_ax(ax, ygrid=False)
    ax.spines["bottom"].set_visible(False)
    ax.set_xticks([])
    ypos = np.arange(len(vals))
    ax.barh(ypos, vals * grow, height=height, color=BLUE, lw=0)
    ax.set_yticks(ypos)
    ax.set_yticklabels(labels, fontsize=size, color=INK2)
    ax.set_ylim(len(vals) - 0.4, -0.6)
    ax.set_xlim(0, vals.max() * xpad)
    for i, v in enumerate(vals):
        if grow[i] > 0.02:
            hi = highlight is not None and labels[i] == highlight
            lab = (value_fmt or pct)(v) + ("  ← median" if hi else "")
            ax.text(v * grow[i] + vals.max() * 0.04, i, lab, va="center", fontsize=size,
                    color=INK if hi else INK2, alpha=grow[i],
                    weight="bold" if hi else "regular")


def s_title(fig, t):
    a1 = seg(t, 0.3, 1.2)
    txt(fig, 0.5, 0.83, "AMAZON MECHANICAL TURK", size=SMALL, color=INK2, weight="semibold",
        ha="center", alpha=a1)
    txt(fig, 0.5, 0.785, "2005 – 2026", size=SMALL, color=INK2, ha="center", alpha=a1)
    a = seg(t, 0.9, 2.0)
    for i, w in enumerate(("MTurk", "Tracker")):
        txt(fig, 0.5, 0.575 - i * 0.19 + 0.015 * (1 - a), w, size=160, weight="bold",
            ha="center", alpha=a, family=DISPLAY)
    first = pd.Timestamp(TOT.first_day)
    last = pd.Timestamp(TOT.last_day)
    rise(fig, t, 1.8, 0.5, 0.27, f"{first:%b %Y}  →  {last:%b %Y}", size=48, color=INK2,
         ha="center")
    day_ticks(fig, t, int(96 * seg(t, 1.2, 6.2, smooth)), y=0.15, alpha=seg(t, 0.8, 1.6))
    rise(fig, t, 3.0, 0.5, 0.075, "a new survey HIT every 15 minutes", size=LABEL,
         color=INK2, ha="center")


def s_calendar(fig, t):
    p = seg(t, 1.0, 10.0, smooth)
    now = START + (END + pd.Timedelta(days=1) - START) * p
    now64 = now.to_datetime64()
    a = seg(t, 0.1, 0.9)
    txt(fig, M, 0.885, f"{TOT.days:,} days", size=TITLE, weight="bold", family=DISPLAY, alpha=a)
    txt(fig, 1 - M, 0.885, f"{min(now, END):%b %Y}", size=48, color=INK2, ha="right", alpha=a)

    ax = fig.add_axes([0.17, 0.31, 1 - M - 0.17, 0.5])
    style_ax(ax, ygrid=False)
    ax.spines["bottom"].set_visible(False)
    X, Y = np.meshgrid(np.arange(53), np.arange(len(YEARS) + 1))
    board = np.ma.masked_invalid(np.where(np.isnan(WEEKS), np.nan, 1.0))
    ax.pcolormesh(X, Y, board, cmap=LinearSegmentedColormap.from_list("b", ["#1e1e1c"] * 2),
                  edgecolors=BG, linewidth=1.5)
    shown = np.where((WEEK_START <= now64) & ~np.isnan(WEEKS), WEEKS, np.nan)
    cmap = LinearSegmentedColormap.from_list("ramp", BLUE_RAMP)
    cmap.set_under(EMPTY)
    cmap.set_bad((0, 0, 0, 0))
    ax.pcolormesh(X, Y, np.ma.masked_invalid(shown), cmap=cmap, vmin=1, vmax=700,
                  edgecolors=BG, linewidth=1.5)
    ax.set_xlim(0, 52)
    ax.set_ylim(len(YEARS), 0)
    ax.set_yticks([YEARS.index(y) + 0.5 for y in (2015, 2020, 2025)])
    ax.set_yticklabels(["2015", "2020", "2025"])
    ax.set_xticks([])

    i = int(np.searchsorted(DAILY.d.values, now64, side="right")) - 1
    cn = DAILY.cum_n.iloc[i] if i >= 0 else 0
    cw = DAILY.cum_w.iloc[i] if i >= 0 else 0
    a = seg(t, 0.6, 1.4)
    for x, n, lab in ((M, cn, "answers"), (0.54, cw, "workers")):
        txt(fig, x, 0.165, fmt(n), size=80, weight="bold", family=DISPLAY, alpha=a)
        txt(fig, x, 0.105, lab, size=LABEL, color=INK2, alpha=a)
    b = seg(t, 10.2, 11.0)
    if b > 0:
        swatch(fig, M, 0.03, EMPTY, b, "survey paused", size=SMALL)


def s_how_many(fig, t):
    # Phase A: the number, centred. Phase B: it moves to the top and the breakdown appears.
    m = seg(t, 4.0, 5.0, smooth)
    a0 = seg(t, 0.2, 1.0)
    gone = 1 - seg(t, 3.6, 4.0, smooth)  # lines that leave before the number moves
    ha = "center" if m < 0.5 else "left"
    x = 0.5 * (1 - m) + M * m
    txt(fig, 0.5, 0.70, "HOW MANY TURKERS?", size=44, color=INK2, weight="semibold",
        ha="center", alpha=a0 * gone)
    n = TOT.workers * seg(t, 0.6, 3.2, eout)
    y = 0.47 * (1 - m) + 0.84 * m
    txt(fig, x, y, fmt(n), size=160 * (1 - m) + 110 * m, weight="bold", family=DISPLAY, ha=ha,
        alpha=a0)
    txt(fig, x, y - 0.1 * (1 - m) - 0.07 * m, "Turkers took the survey",
        size=52 * (1 - m) + BODY * m, color=INK2, ha=ha, alpha=seg(t, 1.6, 2.4))
    txt(fig, 0.5, 0.29, f"{fmt(TOT.responses)} answers · {TOT.countries} countries",
        size=LABEL, color=MUTED, ha="center", alpha=seg(t, 2.4, 3.2) * gone)
    if t < 5.0:
        return
    rise(fig, t, 4.8, M, 0.66, "How often each answered", size=BODY, weight="semibold")
    c = TASKDIST.set_index("cnt_tasks").cnt_workers
    vals = np.array([c.get(1, 0), c.get(2, 0), c[(c.index >= 3) & (c.index <= 5)].sum(),
                     c[c.index >= 6].sum()])
    labels = ["once", "twice", "3–5×", "6+×"]
    grow = np.array([seg(t, 5.3 + 0.2 * i, 6.5 + 0.2 * i) for i in range(len(vals))])
    ax = fig.add_axes([0.22, 0.22, 1 - M - 0.22, 0.38])
    ax.set_alpha(0)
    bars_h(ax, labels, vals, grow,
           value_fmt=lambda v: pct(v / vals.sum()), xpad=1.4, size=44)
    for lab in ax.get_yticklabels():
        lab.set_alpha(seg(t, 5.0, 5.8))
    first = pd.Timestamp(TOP.first_day)
    last = pd.Timestamp(TOP.last_day)
    rise(fig, t, 8.6, M, 0.11, f"Most loyal: {TOP.cnt} answers", size=BODY, weight="semibold")
    rise(fig, t, 8.8, M, 0.055, f"{first:%b %Y} → {last:%b %Y}", size=LABEL, color=INK2)


def s_loyalty(fig, t):
    headline(fig, t, "Most came once.", "Some stayed 10 years.")
    p = seg(t, 1.0, 7.5, smooth)
    xmax = SURV_X[-1]
    ax = fig.add_axes([0.15, 0.15, 1 - M - 0.15, 0.52])
    style_ax(ax)
    xr = 0.0001 + p * xmax
    k = SURV_X <= xr
    ax.fill_between(SURV_X[k], 0, SURV.values[k] * 100, color=BLUE, alpha=0.2, lw=0, step="post")
    ax.step(SURV_X[k], SURV.values[k] * 100, where="post", color=BLUE, lw=5)
    ax.set_xlim(0, 11.8)
    ax.set_ylim(0, 105)
    ax.set_yticks([0, 50, 100])
    ax.set_yticklabels(["0%", "50%", "100%"])
    ax.set_xticks([0, 5, 10])
    ax.set_xticklabels(["start", "5 yrs", "10 yrs"])
    total = SPAN.sum()
    notes = [  # (30-day bucket, label, label height in %, alignment)
        (1, f"{pct(SURV.iloc[1])} came back", 70, "left"),
        (12, f"{pct(SURV.iloc[12])} after a year", 45, "left"),
        (122, f"{fmt(SURV.iloc[122] * total)} Turkers, 10+ yrs", 20, "right"),
    ]
    for kk, label, ty, align in notes:
        xk = SURV_X[kk]
        if xr < xk:
            continue
        reached = 1.0 + 6.5 * (xk / xmax)
        a = seg(t, reached, reached + 0.6)
        yk = SURV.iloc[kk] * 100
        ax.plot([xk, xk], [yk + 2, ty - 1.5], color=INK2, lw=2, alpha=a * 0.7)
        ax.plot([xk], [yk], "o", ms=16, color=BLUE, mec=BG, mew=3, alpha=a)
        ax.text(xk + (-0.12 if align == "right" else 0.12), ty, label, fontsize=BODY, color=INK,
                alpha=a, va="bottom", ha=align)


def s_halflife(fig, t):
    headline(fig, t, "Turkers left", "sooner and sooner")
    rise(fig, t, 1.2, M, 0.68, "half were gone within", size=BODY, color=INK2)
    for i, (c, row) in enumerate(zip((BLUE, ORANGE, AQUA), POP_HALF.itertuples())):
        y = 0.58 - i * 0.19
        a = seg(t, 2.0 + 1.6 * i, 2.8 + 1.6 * i)
        swatch(fig, M, y, c, a, row.era, size=LABEL)
        months = row.half_life_days / 30.44
        txt(fig, M, y - 0.105, f"{months:.0f} months", size=96, weight="bold", family=DISPLAY,
            alpha=a)
    footnote(fig, t, 8.0, "Capture–recapture, open-population model")


def s_population(fig, t):
    headline(fig, t, "How many Turkers", "were there?")
    T_ = POP_TOTAL
    scale = 400_000
    width = 1 - 2 * M
    rows = [  # (label, value, shown as, colour, start time)
        ("we met", T_.workers, fmt(T_.workers), BLUE, 1.0),
        ("on MTurk, at least", round(T_.chao, -3), f"{fmt(round(T_.chao, -3))}", AQUA, 4.0),
    ]
    for i, (lab, v, shown, c, t0) in enumerate(rows):
        y = 0.56 - i * 0.22
        g = seg(t, t0, t0 + 1.5, smooth)
        rise(fig, t, t0, M, y + 0.075, lab, size=BODY, color=INK2)
        fig.patches.append(Rectangle((M, y - 0.045), width * v / scale * g, 0.11,
                                     transform=fig.transFigure, color=c, lw=0))
        txt(fig, M + 0.02, y - 0.012, shown, size=60, weight="bold", family=DISPLAY,
            alpha=seg(t, t0 + 1.0, t0 + 1.6))
    a = seg(t, 7.5, 8.5)
    lo, hi = round(T_.mix4_N, -4), round(T_.mix6_N, -4)
    y = 0.34
    fig.patches.append(Rectangle((M + width * T_.chao / scale, y - 0.045),
                                 width * (hi - T_.chao) / scale * a, 0.11,
                                 transform=fig.transFigure, color=AQUA, alpha=0.4, lw=0))
    rise(fig, t, 7.5, M, 0.15, f"models: {lo / 1000:.0f}K – {hi / 1000:.0f}K", size=BODY,
         color=INK)
    footnote(fig, t, 9.0, "Lower bound: Chao (1987)")


def s_years(fig, t):
    headline(fig, t, "Workers on MTurk", "each year, at least")
    ax = fig.add_axes([0.15, 0.29, 1 - M - 0.15, 0.4])
    style_ax(ax)
    yrs = POP_YEARS.year.values
    grow = np.array([seg(t, 1.0 + 0.25 * i, 2.0 + 0.25 * i) for i in range(len(yrs))])
    ax.bar(yrs, POP_YEARS.chao * grow, width=0.7, color=AQUA, lw=0)
    ax.set_xlim(yrs[0] - 0.6, yrs[-1] + 0.6)
    ax.set_ylim(0, 66000)
    ax.set_yticks([0, 30000, 60000])
    ax.set_yticklabels(["0", "30K", "60K"])
    ax.set_xticks([2015, 2020, 2025])
    early = POP_YEARS[POP_YEARS.year <= 2022].chao
    late = POP_YEARS[POP_YEARS.year >= 2023].chao
    for x, t0, rng, lab in ((M, 5.0, early, "a year, 2015–22"), (0.54, 6.4, late,
                                                                "a year, 2023–26")):
        rise(fig, t, t0, x, 0.13, f"{rng.min() / 1000:.0f}–{rng.max() / 1000:.0f}K", size=80,
             weight="bold", family=DISPLAY)
        rise(fig, t, t0 + 0.1, x, 0.075, lab, size=LABEL, color=INK2)


def s_where(fig, t):
    headline(fig, t, "Mostly American,", "more so every year")
    p = seg(t, 1.0, 6.0, smooth)
    x0, x1 = MONTHLY.x.min() - 1 / 24, MONTHLY.x.max() + 1 / 24
    xr = x0 + p * (x1 - x0)
    ax = fig.add_axes([0.15, 0.3, 1 - M - 0.15, 0.4])
    style_ax(ax, ygrid=False)
    x = MONTHLY.x.values
    us, ind = MONTHLY.us.values, MONTHLY.india.values
    clip = Rectangle((x0, 0), xr - x0, 1, transform=ax.transData)
    layers = ((0, us, BLUE), (us, us + ind, ORANGE), (us + ind, np.ones_like(us), AQUA))
    for lo, hi, c in layers:
        ax.fill_between(x, np.asarray(lo) * 100, hi * 100, color=c, lw=0, step="mid",
                        clip_path=clip)
    ax.set_xlim(x0, x1)
    ax.set_ylim(0, 100)
    ax.set_yticks([0, 50, 100])
    ax.set_yticklabels(["0%", "50%", "100%"])
    ax.set_xticks([2016, 2020, 2024])
    a = seg(t, 1.5, 2.3)
    for i, (c, lab) in enumerate(((BLUE, "US"), (ORANGE, "India"), (AQUA, "other"))):
        swatch(fig, M + i * 0.3, 0.17, c, a, lab, size=BODY)
    y0, y1 = YEARS[0], YEARS[-1]
    rise(fig, t, 6.4, M, 0.06, f"India: {pct(CY_IN[y0])} → {pct(CY_IN[y1])} of answers",
         size=BODY)


def s_us_india(fig, t):
    headline(fig, t, "US vs India:", "two workforces")
    us, ind = US_IN.loc["US"], US_IN.loc["IN"]
    metrics = [("Women", "female"), ("Degree", "college"), ("20+ h/week", "hours20"),
               ("Under $10K", "income_lt10k")]
    ax = fig.add_axes([0.4, 0.12, 1 - M - 0.4, 0.5])
    style_ax(ax, ygrid=False)
    ax.spines["bottom"].set_visible(False)
    ax.set_xticks([])
    ypos = np.arange(len(metrics))
    grow = np.array([seg(t, 1.2 + 0.4 * i, 2.2 + 0.4 * i) for i in range(len(metrics))])
    vu = np.array([us[c] for _, c in metrics])
    vi = np.array([ind[c] for _, c in metrics])
    ax.barh(ypos - 0.21, vu * grow, height=0.4, color=BLUE, lw=0)
    ax.barh(ypos + 0.21, vi * grow, height=0.4, color=ORANGE, lw=0)
    for i in range(len(metrics)):
        if grow[i] > 0.05:
            for v, off in ((vu[i], -0.21), (vi[i], 0.21)):
                ax.text(v * grow[i] + 0.02, i + off, pct(v), va="center", fontsize=SMALL,
                        color=INK, alpha=grow[i])
    ax.set_yticks(ypos)
    ax.set_yticklabels([m for m, _ in metrics], fontsize=LABEL, color=INK2)
    ax.set_ylim(len(metrics) - 0.45, -0.65)
    ax.set_xlim(0, 1.2)
    a = seg(t, 1.0, 1.8)
    swatch(fig, M, 0.68, BLUE, a, "US", size=BODY)
    swatch(fig, 0.36, 0.68, ORANGE, a, "India", size=BODY)
    footnote(fig, t, 6.0, "answers from 2015–2022")


def s_languages(fig, t):
    total = LANG_COUNTS.n.sum()
    headline(fig, t, f"{LANGS.lang.nunique()} languages,", "one marketplace")
    tab = LANGS.pivot_table(index="lang", columns="grp", values="n", aggfunc="sum", fill_value=0)
    tab = tab.drop("English").assign(tot=lambda d: d.sum(axis=1)).sort_values("tot",
                                                                            ascending=False)
    top = tab.head(6)
    ax = fig.add_axes([0.4, 0.17, 1 - M - 0.4, 0.44])
    style_ax(ax, ygrid=False)
    ax.spines["bottom"].set_visible(False)
    ax.set_xticks([])
    ypos = np.arange(len(top))
    grow = np.array([seg(t, 1.2 + 0.2 * i, 2.2 + 0.2 * i) for i in range(len(top))])
    left = np.zeros(len(top))
    for grp, c in (("US", BLUE), ("IN", ORANGE), ("other", AQUA)):
        w = top[grp].values / total * 100 * grow
        ax.barh(ypos, w, left=left, height=0.7, color=c, lw=2, edgecolor=BG)
        left += w
    for i, v in enumerate(top.tot.values):
        if grow[i] > 0.05:
            ax.text(left[i] + 0.1, i, f"{v / total * 100:.0f}%", va="center", fontsize=SMALL,
                    color=INK2, alpha=grow[i])
    ax.set_yticks(ypos)
    ax.set_yticklabels([LANG_NAMES.get(x, x) for x in top.index], fontsize=BODY, color=INK2)
    ax.set_ylim(len(top) - 0.4, -0.6)
    ax.set_xlim(0, top.tot.max() / total * 100 * 1.25)
    a = seg(t, 1.0, 1.8)
    for i, (c, lab) in enumerate(((BLUE, "US"), (ORANGE, "India"), (AQUA, "other"))):
        swatch(fig, M + i * 0.3, 0.68, c, a, lab, size=LABEL)
    rise(fig, t, 6.0, M, 0.06, "Tamil ranked third, above Hindi", size=BODY)


def s_gender(fig, t):
    headline(fig, t, "The gender balance", "swung both ways")
    p = seg(t, 1.0, 6.5, smooth)
    df = FULL_MONTHS.copy()
    df["roll"] = df.female.rolling(3, center=True, min_periods=1).mean()
    x0, x1 = 2015.2, 2026.8
    xr = x0 + p * (x1 - x0)
    ax = fig.add_axes([0.15, 0.12, 1 - M - 0.15, 0.5])
    style_ax(ax)
    d = df[df.x <= xr]
    ax.axhline(50, color=MUTED, lw=2, ls=(0, (4, 4)))
    ax.plot(d.x, d.roll * 100, color=BLUE, lw=6, solid_capstyle="round")
    ax.set_xlim(x0, x1)
    ax.set_ylim(3, 75)
    ax.set_yticks([20, 50])
    ax.set_yticklabels(["20%", "50%"])
    ax.set_xticks([2016, 2020, 2024])
    txt(fig, M, 0.68, "women, share of answers", size=LABEL, color=INK2, alpha=seg(t, 0.6, 1.4))
    lo = df.loc[df.roll.idxmin()]
    marks = [  # (x, y, label, vertical offset, alignment)
        (df.x.iloc[8], df.roll.iloc[8], pct(year_mean("female", YEARS[0])), 6, "center"),
        (lo.x, lo.roll, pct(lo.roll), -4, "center"),
        (df.x.iloc[-4], df.roll.iloc[-4], pct(year_mean("female", YEARS[-1])), 6, "right"),
    ]
    for mx, my, label, off, align in marks:
        if xr < mx:
            continue
        reached = 1.0 + 5.5 * (mx - x0) / (x1 - x0)
        a = seg(t, reached, reached + 0.6)
        ax.plot([mx], [my * 100], "o", ms=18, color=BLUE, mec=BG, mew=3, alpha=a)
        ax.text(mx, my * 100 + off, label, fontsize=60, color=INK, weight="bold", alpha=a,
                ha=align, va="bottom" if off > 0 else "top", family=DISPLAY)


def s_age(fig, t):
    headline(fig, t, "New generations,", "same median age")
    pos = (len(YEARS) - 1) * seg(t, 1.2, 9.5, lambda v: clamp(v))
    i0 = int(np.floor(pos))
    i1 = min(i0 + 1, len(YEARS) - 1)
    f = smooth(pos - i0)
    y0, y1 = YEARS[i0], YEARS[i1]
    share = YOB_SHARE[y0] * (1 - f) + YOB_SHARE[y1] * f
    yr = YEARS[int(round(pos))]
    med = YOB_MED[y0] * (1 - f) + YOB_MED[y1] * f
    a = seg(t, 0.6, 1.4)
    txt(fig, M, 0.62, str(yr), size=90, weight="bold", family=DISPLAY, alpha=a)
    txt(fig, 1 - M, 0.62, f"median age {yr - YOB_MED[yr]}", size=BODY, ha="right", alpha=a)
    ax = fig.add_axes([0.15, 0.2, 1 - M - 0.15, 0.36])
    style_ax(ax)
    ax.bar(YOB_BINS, share * 100, width=0.75, color=BLUE, lw=0, alpha=a)
    top = np.ceil(YOB_MAX * 100 / 10) * 10
    ax.axvline(med, color=INK, lw=3, alpha=a * 0.9)
    ax.set_xlim(1944, 2007)
    ax.set_ylim(0, top)
    ax.set_yticks([0, 10, 20])
    ax.set_yticklabels(["0%", "10%", "20%"])
    ax.set_xticks([1950, 1970, 1990])
    first, last = YEARS[0], YEARS[-1]
    rise(fig, t, 10.0, M, 0.06,
         f"born {YOB_MED[first]} → {YOB_MED[last]}, age {first - YOB_MED[first]} → "
         f"{last - YOB_MED[last]}", size=BODY)


def s_households(fig, t):
    headline(fig, t, "Middle-income", "households")
    rise(fig, t, 0.6, M, 0.68, "US workers, household income", size=LABEL, color=INK2)
    grow = np.array([seg(t, 1.3 + 0.15 * i, 2.3 + 0.15 * i) for i in range(len(INCOME_S))])
    bars_h(fig.add_axes([0.27, 0.06, 1 - M - 0.27, 0.57]), INCOME_LABELS, INCOME_S.values, grow,
           highlight=median_label(INCOME_S, INCOME_LABELS), xpad=2.1, size=SMALL)


def s_answers(fig, t):
    headline(fig, t, "Some answers", "changed too fast")
    p = seg(t, 1.0, 6.5, smooth)
    df = FULL_MONTHS.copy()
    df["mar"] = df.married.rolling(3, center=True, min_periods=1).mean()
    df["col"] = df.college.rolling(3, center=True, min_periods=1).mean()
    x0, x1 = 2015.2, 2026.8
    xr = x0 + p * (x1 - x0)
    ax = fig.add_axes([0.15, 0.3, 1 - M - 0.15, 0.32])
    style_ax(ax)
    d = df[df.x <= xr]
    ax.plot(d.x, d.mar * 100, color=BLUE, lw=6, solid_capstyle="round")
    dc = d[d.col.notna()]
    ax.plot(dc.x, dc.col * 100, color=ORANGE, lw=6, solid_capstyle="round")
    ax.set_xlim(x0, x1)
    ax.set_ylim(0, 105)
    ax.set_yticks([0, 50, 100])
    ax.set_yticklabels(["0%", "50%", "100%"])
    ax.set_xticks([2016, 2020, 2024])
    col0 = df[df.college.notna()]
    col_y1 = int(col0.m.dt.year.iloc[0]) + 1
    y0, y1 = YEARS[0], YEARS[-1]
    for x, c, lab, t0, v0, v1 in (
            (M, BLUE, "married", 3.0, year_mean("married", y0), year_mean("married", y1)),
            (0.54, ORANGE, "degree", 4.8, year_mean("college", col_y1),
             year_mean("college", y1))):
        a = seg(t, 1.0, 1.8)
        swatch(fig, x, 0.68, c, a, lab, size=BODY)
        rise(fig, t, t0, x, 0.1, f"{pct(v0)} → {pct(v1)}", size=56, weight="bold",
             family=DISPLAY)


def s_work(fig, t):
    headline(fig, t, "A side income,", "not a salary")
    rise(fig, t, 0.6, M, 0.68, "earned on MTurk per week", size=LABEL, color=INK2)
    grow = np.array([seg(t, 1.3 + 0.12 * i, 2.3 + 0.12 * i) for i in range(len(PAY_S))])
    bars_h(fig.add_axes([0.27, 0.06, 1 - M - 0.27, 0.57]), PAY_LABELS, PAY_S.values, grow,
           highlight=median_label(PAY_S, PAY_LABELS), xpad=2.1, size=SMALL)


def s_closing(fig, t):
    rise(fig, t, 0.4, 0.5, 0.86, f"To the {fmt(TOT.workers)} Turkers", size=52, ha="center")
    rise(fig, t, 1.2, 0.5, 0.785, f"in {TOT.countries} countries:", size=52, ha="center",
         color=INK2)
    a = seg(t, 2.2, 3.4)
    for i, w in enumerate(("Thank", "you.")):
        txt(fig, 0.5, 0.57 - i * 0.19 + 0.015 * (1 - a), w, size=160, weight="bold",
            ha="center", family=DISPLAY, alpha=a)
    lit = 96 - int(96 * seg(t, 3.5, 8.5, smooth))
    day_ticks(fig, t, lit, y=0.24, alpha=seg(t, 2.6, 3.4))
    rise(fig, t, 4.2, 0.5, 0.15, "MTurk closed September 30, 2026", size=40, color=INK2,
         ha="center")
    rise(fig, t, 5.0, 0.5, 0.07, "demographics.mturk-tracker.com", size=LABEL, ha="center")


SCENES = [
    ("title", 7, s_title),
    ("calendar", 13, s_calendar),
    ("how_many", 14, s_how_many),
    ("loyalty", 11, s_loyalty),
    ("halflife", 12, s_halflife),
    ("population", 15, s_population),
    ("years", 11, s_years),
    ("where", 12, s_where),
    ("us_india", 12, s_us_india),
    ("languages", 11, s_languages),
    ("gender", 10, s_gender),
    ("age", 13, s_age),
    ("households", 10, s_households),
    ("answers", 12, s_answers),
    ("work", 10, s_work),
    ("closing", 10, s_closing),
]
FADE = 0.5


def render_scene(idx):
    name, dur, fn = SCENES[idx]
    path = os.path.join(OUT, f"scene_{idx:02d}_{name}.mp4")
    fig = plt.figure(figsize=(W / 100, H / 100), dpi=100)
    cmd = ["ffmpeg", "-y", "-loglevel", "error", "-f", "rawvideo", "-pix_fmt", "rgba",
           "-s", f"{W}x{H}", "-r", str(FPS), "-i", "-", "-c:v", "libx264", "-preset", "slow",
           "-crf", "18", "-pix_fmt", "yuv420p", "-r", str(FPS), path]
    proc = subprocess.Popen(cmd, stdin=subprocess.PIPE)
    frames = int(round(dur * FPS))
    for k in range(frames):
        t = k / FPS
        fig.clf()
        fn(fig, t)
        fade_out = 0.0 if idx == len(SCENES) - 1 else seg(t, dur - FADE, dur, smooth)
        cover = 1 - min(seg(t, 0, FADE, smooth), 1 - fade_out)  # last card holds to the end
        if cover > 0.001:
            fig.patches.append(Rectangle((0, 0), 1, 1, transform=fig.transFigure, color=BG,
                                         alpha=cover, zorder=1000))
        fig.canvas.draw()
        proc.stdin.write(fig.canvas.buffer_rgba().tobytes())
    proc.stdin.close()
    proc.wait()
    plt.close(fig)
    return path


def still(idx, t, path):
    name, dur, fn = SCENES[idx]
    fig = plt.figure(figsize=(W / 100, H / 100), dpi=100)
    fn(fig, t)
    fig.savefig(path, dpi=100, facecolor=BG)
    plt.close(fig)


def main():
    os.makedirs(OUT, exist_ok=True)
    args = sys.argv[1:]
    if args and args[0] == "stills":
        # stills <scene-index> <t> ... : quick frame checks
        for i in range(1, len(args), 2):
            idx, t = int(args[i]), float(args[i + 1])
            still(idx, t, os.path.join(OUT, f"still_{idx:02d}_{t:05.1f}.png"))
        return
    if args == ["music"]:  # redo the soundtrack only, over the existing scenes
        print(add_music())
        return
    wanted = [i for i, s in enumerate(SCENES) if not args or s[0] in args]
    with Pool(min(len(wanted), os.cpu_count() or 2)) as pool:
        paths = pool.map(render_scene, wanted)
    if args:
        print("\n".join(paths))
        return
    lst = os.path.join(OUT, "scenes.txt")
    with open(lst, "w") as fh:
        fh.writelines(f"file '{os.path.basename(p)}'\n" for p in paths)
    print(add_music())


def add_music():
    lst = os.path.join(OUT, "scenes.txt")
    silent = os.path.join(OUT, "video_only.mp4")
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-f", "concat", "-safe", "0", "-i", lst,
                    "-c", "copy", silent], check=True)
    total = sum(s[1] for s in SCENES)
    wav = music.render(os.path.join(OUT, "music.wav"), total, total - SCENES[-1][1])
    final = os.path.join(OUT, "mturk_tracker_homage.mp4")
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", silent, "-i", wav,
                    "-map", "0:v", "-map", "1:a", "-c:v", "copy", "-c:a", "aac", "-b:a", "192k",
                    "-af", "loudnorm=I=-16:TP=-1.5:LRA=11", "-ar", "48000", "-shortest",
                    "-movflags", "+faststart", final], check=True)
    return final


if __name__ == "__main__":
    main()
