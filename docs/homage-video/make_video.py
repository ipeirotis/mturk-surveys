#!/usr/bin/env python3
"""Renders "MTurk Tracker, 2015-2026", a ~2 minute homage video, from the
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
W, H = 1920, 1080

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


# Type scale, in points at 1920×1080. The floor (SMALL) is about 1/32 of the frame height,
# so every line stays readable when the video plays on a phone.
SMALL, LABEL, BODY = 24, 26, 30
KICKER, TITLE = 24, 52


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


def chrome(fig, t, kicker, title):
    a = seg(t, 0.1, 0.9)
    txt(fig, 0.05, 0.915, kicker.upper(), size=KICKER, color=INK2, weight="semibold", alpha=a)
    txt(fig, 0.05, 0.835 - 0.01 * (1 - a), title, size=TITLE, weight="bold", alpha=a,
        family=DISPLAY)


def footnote(fig, t, t0, s):
    rise(fig, t, t0, 0.05, 0.035, s, size=SMALL, color=MUTED)


def legend(fig, items, x, y, alpha, dy=0.05, size=LABEL):
    """Swatch + label rows; a (colour, "o") item draws a dot marker instead of a swatch."""
    for i, (c, lab) in enumerate(items):
        yy = y - i * dy
        if isinstance(c, tuple):
            fig.lines.append(matplotlib.lines.Line2D([x + 0.009], [yy + 0.011],
                                                     transform=fig.transFigure, marker="o",
                                                     ms=12, color=c[0], mec=BG, alpha=alpha))
        else:
            fig.patches.append(Rectangle((x, yy), 0.018, 0.026, transform=fig.transFigure,
                                         color=c, alpha=alpha))
        txt(fig, x + 0.027, yy + 0.002, lab, size=size, color=INK, alpha=alpha)


def stat(fig, t, t0, x, y, number, label, size=64, color=INK, label_color=INK2):
    """A headline number with a short caption under it."""
    rise(fig, t, t0, x, y, number, size=size, weight="bold", family=DISPLAY, color=color)
    rise(fig, t, t0 + 0.1, x, y - 0.05, label, size=LABEL, color=label_color)


def lines(fig, t, t0, x, y, rows, size=BODY, color=INK, dy=0.05, **kw):
    for i, r in enumerate(rows):
        rise(fig, t, t0 + 0.1 * i, x, y - i * dy, r, size=size, color=color, **kw)


def style_ax(ax, ygrid=True):
    ax.set_facecolor("none")
    for s in ("top", "right", "left"):
        ax.spines[s].set_visible(False)
    ax.spines["bottom"].set_color(AXIS)
    ax.tick_params(colors=INK2, labelsize=SMALL, length=0, pad=10)
    if ygrid:
        ax.grid(axis="y", color=GRID, lw=1)
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
    ax = fig.add_axes([0.15, y, 0.7, 0.045])
    ax.axis("off")
    ax.set_xlim(0, 96)
    ax.set_ylim(0, 1)
    colors = [BLUE if i < lit else GRID for i in range(96)]
    ax.bar(np.arange(96) + 0.5, 1, width=0.7, color=colors, alpha=alpha, lw=0)


def bars_h(ax, labels, vals, grow, color=BLUE, value_fmt=None, highlight=None, xpad=1.7,
           height=0.64):
    """Horizontal bars with the value printed at each bar's end."""
    style_ax(ax, ygrid=False)
    ax.spines["bottom"].set_visible(False)
    ax.set_xticks([])
    ypos = np.arange(len(vals))
    ax.barh(ypos, vals * grow, height=height, color=color, lw=0)
    ax.set_yticks(ypos)
    ax.set_yticklabels(labels, fontsize=LABEL)
    ax.set_ylim(len(vals) - 0.4, -0.6)
    ax.set_xlim(0, vals.max() * xpad)
    for i, v in enumerate(vals):
        if grow[i] > 0.02:
            hi = highlight is not None and labels[i] == highlight
            lab = (value_fmt or pct)(v) + ("  ← median" if hi else "")
            ax.text(v * grow[i] + vals.max() * 0.03, i, lab, va="center", fontsize=LABEL,
                    color=INK if hi else INK2, alpha=grow[i],
                    weight="semibold" if hi else "regular")


def s_title(fig, t):
    txt(fig, 0.5, 0.74, "AMAZON MECHANICAL TURK  ·  2005 – 2026", size=28, color=INK2,
        weight="medium", ha="center", alpha=seg(t, 0.3, 1.2))
    a = seg(t, 0.9, 2.0)
    txt(fig, 0.5, 0.555 + 0.015 * (1 - a), "MTurk Tracker", size=130, weight="bold",
        ha="center", alpha=a, family=DISPLAY)
    rise(fig, t, 1.7, 0.5, 0.455, "11.5 years of worker demographics", size=44, color=INK2,
         ha="center")
    first = pd.Timestamp(TOT.first_day)
    last = pd.Timestamp(TOT.last_day)
    rise(fig, t, 2.4, 0.5, 0.375,
         f"{first.day} {first:%B %Y}   →   {last.day} {last:%B %Y}", size=34, color=MUTED,
         ha="center")
    day_ticks(fig, t, int(96 * seg(t, 1.2, 6.2, smooth)), y=0.2, alpha=seg(t, 0.8, 1.6))
    rise(fig, t, 3.0, 0.5, 0.13, "a new HIT every 15 minutes: 96 a day", size=30,
         color=INK2, ha="center")


def s_calendar(fig, t):
    chrome(fig, t, "Every 15 minutes, a new HIT", f"{TOT.days:,} days of answers")
    p = seg(t, 1.0, 10.0, smooth)
    now = START + (END + pd.Timedelta(days=1) - START) * p
    now64 = now.to_datetime64()

    ax = fig.add_axes([0.1, 0.06, 0.54, 0.66])
    style_ax(ax, ygrid=False)
    ax.spines["bottom"].set_visible(False)
    X, Y = np.meshgrid(np.arange(53), np.arange(len(YEARS) + 1))
    board = np.ma.masked_invalid(np.where(np.isnan(WEEKS), np.nan, 1.0))
    ax.pcolormesh(X, Y, board, cmap=LinearSegmentedColormap.from_list("b", ["#1e1e1c"] * 2),
                  edgecolors=BG, linewidth=2.5)
    shown = np.where((WEEK_START <= now64) & ~np.isnan(WEEKS), WEEKS, np.nan)
    cmap = LinearSegmentedColormap.from_list("ramp", BLUE_RAMP)
    cmap.set_under(EMPTY)
    cmap.set_bad((0, 0, 0, 0))
    ax.pcolormesh(X, Y, np.ma.masked_invalid(shown), cmap=cmap, vmin=1, vmax=700,
                  edgecolors=BG, linewidth=2.5)
    ax.set_xlim(0, 52)
    ax.set_ylim(len(YEARS), 0)
    ax.set_yticks(np.arange(len(YEARS)) + 0.5)
    ax.set_yticklabels([f"{y}" if y % 2 == 1 or y in (YEARS[0], YEARS[-1]) else ""
                        for y in YEARS])
    mstarts = [(pd.Timestamp(2021, m, 1).dayofyear - 1) / 7 for m in (1, 4, 7, 10)]
    ax.set_xticks(mstarts)
    ax.set_xticklabels(["Jan", "Apr", "Jul", "Oct"])
    ax.xaxis.tick_top()

    i = int(np.searchsorted(DAILY.d.values, now64, side="right")) - 1
    cn = DAILY.cum_n.iloc[i] if i >= 0 else 0
    cw = DAILY.cum_w.iloc[i] if i >= 0 else 0
    a = seg(t, 0.6, 1.4)
    shown_date = min(now, END)
    txt(fig, 0.68, 0.64, f"{shown_date:%b %Y}", size=76, weight="bold", family=DISPLAY, alpha=a)
    txt(fig, 0.68, 0.50, fmt(cn), size=60, weight="semibold", family=DISPLAY, alpha=a)
    txt(fig, 0.68, 0.455, "answers", size=LABEL, color=INK2, alpha=a)
    txt(fig, 0.68, 0.35, fmt(cw), size=60, weight="semibold", family=DISPLAY, alpha=a)
    txt(fig, 0.68, 0.305, "different workers", size=LABEL, color=INK2, alpha=a)

    b = seg(t, 10.2, 11.0)
    if b > 0:
        txt(fig, 0.68, 0.21, "answers per week", size=SMALL, color=INK2, alpha=b)
        lax = fig.add_axes([0.68, 0.16, 0.24, 0.03])
        lax.imshow(np.linspace(0, 1, 256)[None, :], aspect="auto", cmap=cmap, alpha=b)
        lax.axis("off")
        txt(fig, 0.68, 0.145, "0", size=SMALL, color=MUTED, alpha=b, va="top")
        txt(fig, 0.92, 0.145, "700", size=SMALL, color=MUTED, alpha=b, va="top", ha="right")
        legend(fig, [(EMPTY, "survey paused")], 0.68, 0.045, b, size=SMALL)


def s_how_many(fig, t):
    # Phase A: the number, centred. Phase B: it moves left and the breakdown appears.
    m = seg(t, 4.0, 5.0, smooth)
    a0 = seg(t, 0.2, 1.0)
    ha = "center" if m < 0.5 else "left"
    k_alpha = a0 * (1 - 2 * min(m, 0.5)) + max(0, 2 * m - 1)
    txt(fig, 0.5 * (1 - m) + 0.05 * m, 0.74 * (1 - m) + 0.915 * m,
        "SOMEONE ASKED: HOW MANY TURKERS WERE THERE?", size=30 * (1 - m) + KICKER * m,
        color=INK2, weight="semibold", ha=ha, alpha=k_alpha)
    n = TOT.workers * seg(t, 0.6, 3.2, eout)
    x = 0.5 * (1 - m) + 0.05 * m
    y = 0.47 * (1 - m) + 0.66 * m
    txt(fig, x, y, fmt(n), size=170 * (1 - m) + 100 * m, weight="bold", family=DISPLAY, ha=ha,
        alpha=a0)
    txt(fig, x, y - 0.085 * (1 - m) - 0.065 * m, "different workers took the survey",
        size=44 * (1 - m) + BODY * m, color=INK2, ha=ha, alpha=seg(t, 1.6, 2.4))
    txt(fig, x, y - 0.16 * (1 - m) - 0.12 * m,
        f"{fmt(TOT.responses)} answers  ·  {TOT.countries} countries",
        size=34 * (1 - m) + LABEL * m, color=MUTED, ha=ha, alpha=seg(t, 2.4, 3.2))
    if t < 4.5:
        return
    rise(fig, t, 4.8, 0.47, 0.80, "Times each worker answered", size=40, weight="bold",
         family=DISPLAY)
    rise(fig, t, 5.0, 0.47, 0.75, "at most once a month", size=LABEL, color=INK2)
    labels = list(BUCKETS.index)
    vals = BUCKETS.values
    grow = np.array([seg(t, 5.3 + 0.18 * i, 6.5 + 0.18 * i) for i in range(len(vals))])
    ax = fig.add_axes([0.55, 0.06, 0.42, 0.63])
    bars_h(ax, labels, vals, grow, xpad=1.75,
           value_fmt=lambda v: f"{fmt(v)} · {pct(v / vals.sum())}")
    first = pd.Timestamp(TOP.first_day)
    last = pd.Timestamp(TOP.last_day)
    rise(fig, t, 8.6, 0.05, 0.40, "Most loyal Turker", size=BODY, color=INK2)
    rise(fig, t, 8.8, 0.05, 0.32, f"{TOP.cnt} answers", size=64, weight="bold", family=DISPLAY)
    rise(fig, t, 9.0, 0.05, 0.265, f"{first:%b %Y} → {last:%b %Y}", size=BODY, color=INK2)
    once = vals[0] / vals.sum()
    rise(fig, t, 10.4, 0.05, 0.14, f"{pct(once)} answered only once.", size=BODY, color=INK)


def s_loyalty(fig, t):
    chrome(fig, t, "How long did a Turker stay?", "Most came once. Some stayed a decade.")
    p = seg(t, 1.0, 7.5, smooth)
    xmax = SURV_X[-1]
    ax = fig.add_axes([0.09, 0.15, 0.87, 0.58])
    style_ax(ax)
    xr = 0.0001 + p * xmax
    k = SURV_X <= xr
    ax.fill_between(SURV_X[k], 0, SURV.values[k] * 100, color=BLUE, alpha=0.18, lw=0, step="post")
    ax.step(SURV_X[k], SURV.values[k] * 100, where="post", color=BLUE, lw=4)
    ax.set_xlim(0, 11.8)
    ax.set_ylim(0, 105)
    ax.set_yticks([0, 50, 100])
    ax.set_yticklabels(["0%", "50%", "100%"])
    ax.set_xticks(range(0, 12, 2))
    ax.set_xticklabels(["start"] + [f"{i} yr" for i in range(2, 12, 2)])
    total = SPAN.sum()
    notes = [  # (30-day bucket, label, label height in %, alignment)
        (1, f"{pct(SURV.iloc[1])} came back later", 66, "left"),
        (12, f"{pct(SURV.iloc[12])} still there after a year", 47, "left"),
        (61, f"{pct(SURV.iloc[61], 1)} after 5 years", 33, "left"),
        (122, f"{fmt(SURV.iloc[122] * total)} Turkers for 10+ years", 18, "right"),
    ]
    for kk, label, ty, align in notes:
        xk = SURV_X[kk]
        if xr < xk:
            continue
        reached = 1.0 + 6.5 * (xk / xmax)
        a = seg(t, reached, reached + 0.6)
        yk = SURV.iloc[kk] * 100
        ax.plot([xk, xk], [yk + 1.5, ty - 1], color=INK2, lw=1.5, alpha=a * 0.7)
        ax.plot([xk], [yk], "o", ms=13, color=BLUE, mec=BG, mew=2.5, alpha=a)
        ax.text(xk + (-0.08 if align == "right" else 0.08), ty, label, fontsize=BODY, color=INK,
                alpha=a, va="bottom", ha=align)
    footnote(fig, t, 8.5, "Share of workers whose last answer came this long after their first.")


def s_halflife(fig, t):
    chrome(fig, t, "How long did workers stay on MTurk?", "Turkers kept leaving sooner")
    p = seg(t, 1.0, 5.5, smooth)
    gmax = 24
    ax = fig.add_axes([0.09, 0.15, 0.5, 0.55])
    style_ax(ax)
    colors = (BLUE, ORANGE, AQUA)
    gaps = POP_RETURN.index[POP_RETURN.index <= gmax]
    gr = 2 + p * (gmax - 2)
    for c, era in zip(colors, POP_RETURN.columns):
        v = POP_RETURN.loc[gaps, era]
        k = gaps <= gr
        ax.plot(gaps[k], v[k] * 100, color=c, lw=4.5, solid_capstyle="round")
    ax.set_xlim(1.5, gmax + 0.5)
    ax.set_ylim(0, 30)
    ax.set_yticks([0, 10, 20, 30])
    ax.set_yticklabels(["0%", "10%", "20%", "30%"])
    ax.set_xticks([2, 12, 24])
    ax.set_xticklabels(["2", "12", "24 months"])
    txt(fig, 0.05, 0.74, "came back, by months since an answer", size=LABEL, color=INK2,
        alpha=seg(t, 0.6, 1.4))

    rise(fig, t, 2.0, 0.64, 0.68, "Half were gone within", size=BODY, color=INK2)
    for i, (c, row) in enumerate(zip(colors, POP_HALF.itertuples())):
        y = 0.60 - i * 0.145
        a = seg(t, 2.6 + 1.2 * i, 3.4 + 1.2 * i)
        legend(fig, [(c, row.era)], 0.64, y, a)
        months = row.half_life_days / 30.44
        txt(fig, 0.667, y - 0.068, f"{months:.0f} months", size=56, weight="bold",
            family=DISPLAY, alpha=a)
    lines(fig, t, 7.2, 0.64, 0.16, ["Even the regulars", "stopped sticking around."])
    footnote(fig, t, 7.8, "Open-population capture–recapture (Difallah, Filatova & Ipeirotis, "
                          "WSDM 2018)")


def s_population(fig, t):
    chrome(fig, t, "So how many Turkers were there?", "We met 157K. There were far more.")
    ax = fig.add_axes([0.09, 0.15, 0.48, 0.55])
    style_ax(ax)
    kmax = 40
    d = POP_SEEN[POP_SEEN.k <= kmax]
    a1 = seg(t, 1.0, 2.0)
    ax.bar(d.k, d.equal.clip(lower=0.8), width=0.7, color=AXIS, lw=0, alpha=a1)
    pk = 1 + (kmax - 1) * seg(t, 2.8, 5.8, smooth)
    o = d[(d.k <= pk) & (d.observed > 0)]
    ax.plot(o.k, o.observed, "o", ms=10, color=BLUE, mec=BG, mew=1.5)
    ax.set_yscale("log")
    ax.set_ylim(0.8, 3e5)
    ax.set_yticks([1, 100, 10000])
    ax.set_yticklabels(["1", "100", "10K"])
    ax.minorticks_off()
    ax.set_xlim(0, kmax + 1)
    ax.set_xticks([1, 20, 40])
    ax.set_xticklabels(["1", "20", "40 months"])
    txt(fig, 0.05, 0.74, "workers, by months in which they answered", size=LABEL, color=INK2,
        alpha=a1)
    legend(fig, [(AXIS, "if all were alike"), ((BLUE, "o"), "what we saw")], 0.27, 0.62,
           a1, dy=0.055)

    T_ = POP_TOTAL
    mix_lo, mix_hi = T_.mix4_N, T_.mix6_N
    stat(fig, t, 1.5, 0.63, 0.66, fmt(T_.workers), "workers we met", size=56)
    stat(fig, t, 4.0, 0.63, 0.50, f"~{fmt(round(T_.equal_N, -3))}", "if all were alike: too low",
         size=56, color=MUTED, label_color=MUTED)
    stat(fig, t, 6.6, 0.63, 0.33, f"≥ {fmt(round(T_.chao, -3))}",
         "lower bound, any propensities", size=72)
    stat(fig, t, 8.6, 0.63, 0.17,
         f"{round(mix_lo, -4) / 1000:.0f}K – {round(mix_hi, -4) / 1000:.0f}K",
         "skewed-propensity models", size=56)
    footnote(fig, t, 9.0, "Lower bound: Chao (1987).  Models: binomial mixtures, 4–6 classes.")


def s_years(fig, t):
    chrome(fig, t, "Year by year", "Each year, tens of thousands unseen")
    ax = fig.add_axes([0.09, 0.15, 0.56, 0.5])
    style_ax(ax)
    yrs = POP_YEARS.year.values
    grow = np.array([seg(t, 1.0 + 0.25 * i, 2.0 + 0.25 * i) for i in range(len(yrs))])
    ax.bar(yrs - 0.2, POP_YEARS.chao * grow, width=0.38, color=AQUA, lw=0)
    ax.bar(yrs + 0.2, POP_YEARS.workers * grow, width=0.38, color=BLUE, lw=0)
    for i, (y, v) in enumerate(zip(yrs, POP_YEARS.chao)):
        if grow[i] > 0.3 and i % 2 == 1 or (grow[i] > 0.3 and v == POP_YEARS.chao.max()):
            ax.text(y - 0.2, v * grow[i] + 1200, f"{v / 1000:.0f}K", ha="center",
                    fontsize=SMALL, color=INK, alpha=grow[i])
    ax.set_xlim(yrs[0] - 0.6, yrs[-1] + 0.6)
    ax.set_ylim(0, 70000)
    ax.set_yticks([0, 30000, 60000])
    ax.set_yticklabels(["0", "30K", "60K"])
    ax.set_xticks(yrs[::2])
    ax.set_xticklabels([f"{y}" for y in yrs[::2]])
    legend(fig, [(AQUA, "on MTurk, at least"), (BLUE, "answered our survey")], 0.09, 0.715,
           seg(t, 1.0, 1.8), dy=0.055)
    early = POP_YEARS[POP_YEARS.year <= 2022].chao
    late = POP_YEARS[POP_YEARS.year >= 2023].chao
    stat(fig, t, 5.0, 0.70, 0.60, f"{early.min() / 1000:.0f}–{early.max() / 1000:.0f}K",
         "a year, 2015–2022", size=72)
    stat(fig, t, 6.4, 0.70, 0.40, f"{late.min() / 1000:.0f}–{late.max() / 1000:.0f}K",
         "a year, 2023–2026", size=72)
    lines(fig, t, 7.8, 0.70, 0.22, ["The pool roughly", "halved at the end."])
    footnote(fig, t, 8.4, "Chao (1987) lower bound within each calendar year.")


def s_where(fig, t):
    chrome(fig, t, "Where they worked from", "Mostly American, more so every year")
    p = seg(t, 1.0, 6.0, smooth)
    x0, x1 = MONTHLY.x.min() - 1 / 24, MONTHLY.x.max() + 1 / 24
    xr = x0 + p * (x1 - x0)
    ax = fig.add_axes([0.09, 0.15, 0.55, 0.58])
    style_ax(ax, ygrid=False)
    x = MONTHLY.x.values
    us, ind = MONTHLY.us.values, MONTHLY.india.values
    clip = Rectangle((x0, 0), xr - x0, 1, transform=ax.transData)
    layers = ((0, us, BLUE), (us, us + ind, ORANGE), (us + ind, np.ones_like(us), AQUA))
    for lo, hi, c in layers:
        ax.fill_between(x, np.asarray(lo) * 100, hi * 100, color=c, lw=0, step="mid",
                        clip_path=clip)
        line, = ax.step(x, hi * 100, where="mid", color=BG, lw=2)
        line.set_clip_path(clip)
    ax.set_xlim(x0, x1)
    ax.set_ylim(0, 100)
    ax.set_yticks([0, 50, 100])
    ax.set_yticklabels(["0%", "50%", "100%"])
    ax.set_xticks([2016, 2020, 2024])
    ax.text(2016.0, 38, "United States", fontsize=40, color=INK, weight="bold",
            alpha=seg(t, 2.0, 2.8))
    ax.text(2019.3, 74, "India", fontsize=32, color=INK, weight="bold", alpha=seg(t, 2.6, 3.4))
    ax.text(2021.2, 89.5, "Elsewhere", fontsize=LABEL, color=INK, weight="bold",
            alpha=seg(t, 3.0, 3.8))

    rise(fig, t, 2.0, 0.69, 0.70, "Top countries", size=BODY, weight="semibold")
    for i, (c, n) in enumerate(TOP_COUNTRIES.head(7).items()):
        y = 0.63 - i * 0.065
        b = seg(t, 2.4 + 0.25 * i, 3.1 + 0.25 * i)
        txt(fig, 0.69, y, COUNTRY_NAMES.get(c, c), size=LABEL, color=INK, alpha=b)
        txt(fig, 0.965, y, fmt(n), size=LABEL, color=INK2, alpha=b, ha="right")
    rise(fig, t, 5.0, 0.69, 0.63 - 7 * 0.065,
         f"+ {TOT.countries - 7} more countries", size=LABEL, color=MUTED)
    y0, y1 = YEARS[0], YEARS[-1]
    footnote(fig, t, 6.4, f"Share of answers.  India: {pct(CY_IN[y0])} in {y0}, "
                          f"{pct(CY_IN[y1])} in {y1}.")


def s_us_india(fig, t):
    chrome(fig, t, "Two workforces", "US and Indian Turkers differed")
    us, ind = US_IN.loc["US"], US_IN.loc["IN"]
    metrics = [("Women", "female"), ("Married", "married"), ("Bachelor's or more", "college"),
               ("Graduate degree", "graduate"), ("20+ hours a week", "hours20"),
               ("Income under $10K", "income_lt10k"), ("Household of 4+", "household4")]
    ax = fig.add_axes([0.28, 0.11, 0.38, 0.56])
    style_ax(ax, ygrid=False)
    ax.spines["bottom"].set_visible(False)
    ax.set_xticks([])
    ypos = np.arange(len(metrics))
    grow = np.array([seg(t, 1.2 + 0.3 * i, 2.2 + 0.3 * i) for i in range(len(metrics))])
    vu = np.array([us[c] for _, c in metrics])
    vi = np.array([ind[c] for _, c in metrics])
    ax.barh(ypos - 0.2, vu * grow, height=0.38, color=BLUE, lw=0)
    ax.barh(ypos + 0.2, vi * grow, height=0.38, color=ORANGE, lw=0)
    for i in range(len(metrics)):
        if grow[i] > 0.05:
            ax.text(vu[i] * grow[i] + 0.015, i - 0.2, pct(vu[i]), va="center", fontsize=SMALL,
                    color=INK, alpha=grow[i])
            ax.text(vi[i] * grow[i] + 0.015, i + 0.2, pct(vi[i]), va="center", fontsize=SMALL,
                    color=INK, alpha=grow[i])
    ax.set_yticks(ypos)
    ax.set_yticklabels([m for m, _ in metrics], fontsize=LABEL)
    ax.set_ylim(len(metrics) - 0.45, -0.65)
    ax.set_xlim(0, 1.12)
    legend(fig, [(BLUE, f"United States · median age {us.median_age:.0f}"),
                 (ORANGE, f"India · median age {ind.median_age:.0f}")],
           0.28, 0.735, seg(t, 1.0, 1.8), dy=0.05)
    ratio = ind.income_lt10k / us.income_lt10k
    lines(fig, t, 4.6, 0.70, 0.55, ["Indian Turkers were", "more educated and",
                                    "worked more hours,"])
    lines(fig, t, 5.8, 0.70, 0.36, [f"yet {ratio:.0f}× as likely to", "report a household",
                                    "income under $10K."])
    footnote(fig, t, 7.0, "Answers from 2015–2022.")


def s_languages(fig, t):
    total = LANG_COUNTS.n.sum()
    chrome(fig, t, "Languages spoken", f"{LANGS.lang.nunique()} languages, one marketplace")
    tab = LANGS.pivot_table(index="lang", columns="grp", values="n", aggfunc="sum", fill_value=0)
    tab = tab.drop("English").assign(tot=lambda d: d.sum(axis=1)).sort_values("tot",
                                                                            ascending=False)
    top = tab.head(10)
    ax = fig.add_axes([0.18, 0.1, 0.45, 0.56])
    style_ax(ax, ygrid=False)
    ax.spines["bottom"].set_visible(False)
    ax.set_xticks([])
    ypos = np.arange(len(top))
    grow = np.array([seg(t, 1.2 + 0.15 * i, 2.2 + 0.15 * i) for i in range(len(top))])
    left = np.zeros(len(top))
    for grp, c in (("US", BLUE), ("IN", ORANGE), ("other", AQUA)):
        w = top[grp].values / total * 100 * grow
        ax.barh(ypos, w, left=left, height=0.68, color=c, lw=1.5, edgecolor=BG)
        left += w
    for i, v in enumerate(top.tot.values):
        if grow[i] > 0.05:
            ax.text(left[i] + 0.08, i, f"{v / total * 100:.1f}%", va="center", fontsize=SMALL,
                    color=INK2, alpha=grow[i])
    ax.set_yticks(ypos)
    ax.set_yticklabels([LANG_NAMES.get(x, x) for x in top.index], fontsize=LABEL)
    ax.set_ylim(len(top) - 0.4, -0.6)
    ax.set_xlim(0, top.tot.max() / total * 100 * 1.25)
    a = seg(t, 1.0, 1.8)
    for i, (c, lab) in enumerate(((BLUE, "US"), (ORANGE, "India"), (AQUA, "elsewhere"))):
        legend(fig, [(c, lab)], 0.18 + i * 0.11, 0.725, a)

    eng = LANGS[LANGS.lang == "English"].n.sum() / total
    multi = LANG_COUNTS[LANG_COUNTS.k >= 2].n.sum() / total
    rank = list(tab.index).index("Tamil") + 2  # +1 for English, +1 for 1-based
    stat(fig, t, 3.6, 0.70, 0.60, pct(eng), "listed English")
    stat(fig, t, 4.8, 0.70, 0.42, pct(multi), "listed two or more")
    ordinal = {2: "second", 3: "third", 4: "fourth"}.get(rank, f"#{rank}")
    lines(fig, t, 6.4, 0.70, 0.24, [f"Tamil ranked {ordinal},", "ahead of Hindi."])
    footnote(fig, t, 7.2, "Share of answers naming each language besides English.")


def s_gender(fig, t):
    chrome(fig, t, "Who they were", "The gender balance swung both ways")
    p = seg(t, 1.0, 6.5, smooth)
    df = FULL_MONTHS.copy()
    df["roll"] = df.female.rolling(3, center=True, min_periods=1).mean()
    x0, x1 = 2015.2, 2026.8
    xr = x0 + p * (x1 - x0)
    ax = fig.add_axes([0.09, 0.12, 0.87, 0.58])
    style_ax(ax)
    d = df[df.x <= xr]
    ax.axhline(50, color=MUTED, lw=1.5, ls=(0, (4, 4)))
    ax.text(2020.4, 51, "50 / 50", fontsize=SMALL, color=MUTED, ha="center", va="bottom")
    ax.plot(d.x, d.female * 100, color=BLUE, lw=1.5, alpha=0.35)
    ax.plot(d.x, d.roll * 100, color=BLUE, lw=5, solid_capstyle="round")
    ax.set_xlim(x0, x1)
    ax.set_ylim(15, 75)
    ax.set_yticks([20, 40, 60])
    ax.set_yticklabels(["20%", "40%", "60%"])
    ax.set_xticks([2016, 2020, 2024])
    txt(fig, 0.05, 0.74, "women, share of answers", size=LABEL, color=INK2,
        alpha=seg(t, 0.6, 1.4))
    lo = df.loc[df.roll.idxmin()]
    marks = [  # (x, y, label, vertical offset, alignment)
        (df.x.iloc[8], df.roll.iloc[8], f"{YEARS[0]}: {pct(year_mean('female', YEARS[0]))}", 5,
         "left"),
        (lo.x, lo.roll, f"{lo.m:%b %Y}: {pct(lo.roll)}", -5, "center"),
        (df.x.iloc[-4], df.roll.iloc[-4], f"{YEARS[-1]}: {pct(year_mean('female', YEARS[-1]))}",
         6, "right"),
    ]
    for mx, my, label, off, align in marks:
        if xr < mx:
            continue
        reached = 1.0 + 5.5 * (mx - x0) / (x1 - x0)
        a = seg(t, reached, reached + 0.6)
        ax.plot([mx], [my * 100], "o", ms=14, color=BLUE, mec=BG, mew=2.5, alpha=a)
        ax.text(mx, my * 100 + off, label, fontsize=34, color=INK, weight="semibold", alpha=a,
                ha=align, va="bottom" if off > 0 else "top")


def s_age(fig, t):
    chrome(fig, t, "How old they were", "New generations, about the same age")
    pos = (len(YEARS) - 1) * seg(t, 1.2, 9.5, lambda v: clamp(v))
    i0 = int(np.floor(pos))
    i1 = min(i0 + 1, len(YEARS) - 1)
    f = smooth(pos - i0)
    y0, y1 = YEARS[i0], YEARS[i1]
    share = YOB_SHARE[y0] * (1 - f) + YOB_SHARE[y1] * f
    yr = YEARS[int(round(pos))]
    med = YOB_MED[y0] * (1 - f) + YOB_MED[y1] * f
    ax = fig.add_axes([0.09, 0.12, 0.56, 0.56])
    style_ax(ax)
    a = seg(t, 0.6, 1.4)
    ax.bar(YOB_BINS, share * 100, width=0.72, color=BLUE, lw=0, alpha=a)
    top = np.ceil(YOB_MAX * 100 / 10) * 10
    ax.axvline(med, color=INK, lw=2.5, alpha=a * 0.9)
    ax.text(med, top * 1.02, "median", fontsize=SMALL, color=INK, alpha=a, va="bottom",
            ha="center")
    ax.set_xlim(1944, 2007)
    ax.set_ylim(0, top)
    ticks = np.arange(0, top + 0.1, 10)
    ax.set_yticks(ticks)
    ax.set_yticklabels([f"{v:.0f}%" for v in ticks])
    ax.set_xticks([1950, 1970, 1990])
    txt(fig, 0.05, 0.74, "year of birth, share of answers", size=LABEL, color=INK2, alpha=a)
    txt(fig, 0.70, 0.60, str(yr), size=120, weight="bold", family=DISPLAY, alpha=a)
    txt(fig, 0.70, 0.51, f"median age {yr - YOB_MED[yr]}", size=36, color=INK, alpha=a)
    txt(fig, 0.70, 0.46, f"born {YOB_MED[yr]}", size=BODY, color=INK2, alpha=a)
    first, last = YEARS[0], YEARS[-1]
    lines(fig, t, 10.0, 0.70, 0.35,
          [f"Born: {YOB_MED[first]} → {YOB_MED[last]}",
           f"Age: {first - YOB_MED[first]} → {last - YOB_MED[last]}"])
    peak_year = max(YOB_SHARE, key=lambda y: YOB_SHARE[y][YOB_BINS == 1990][0])
    peak = YOB_SHARE[peak_year][YOB_BINS == 1990][0]
    base = YOB_SHARE[first][YOB_BINS == 1990][0]
    lines(fig, t, 11.0, 0.70, 0.19,
          ["“Born in 1990”:", f"{pct(base)} ({first}) → {pct(peak)} ({peak_year})"],
          size=LABEL, color=INK2, dy=0.045)


def s_households(fig, t):
    chrome(fig, t, "At home, in the US", "Middle income, often families")
    for j, (s_, labels, title, tx, axr) in enumerate((
            (INCOME_S, INCOME_LABELS, "Household income", 0.05, [0.17, 0.06, 0.29, 0.6]),
            (SIZE_S, SIZE_LABELS, "Household size", 0.52, [0.65, 0.06, 0.29, 0.6]))):
        rise(fig, t, 0.8 + 0.3 * j, tx, 0.72, title, size=36, weight="semibold")
        grow = np.array([seg(t, 1.3 + 0.3 * j + 0.12 * i, 2.3 + 0.3 * j + 0.12 * i)
                         for i in range(len(s_))])
        bars_h(fig.add_axes(axr), labels, s_.values, grow, highlight=median_label(s_, labels),
               xpad=2.0)


def s_answers(fig, t):
    chrome(fig, t, "Answers that moved", "Some answers changed too fast")
    p = seg(t, 1.0, 6.5, smooth)
    df = FULL_MONTHS.copy()
    df["mar"] = df.married.rolling(3, center=True, min_periods=1).mean()
    df["col"] = df.college.rolling(3, center=True, min_periods=1).mean()
    x0, x1 = 2015.2, 2026.8
    xr = x0 + p * (x1 - x0)
    ax = fig.add_axes([0.09, 0.16, 0.54, 0.48])
    style_ax(ax)
    d = df[df.x <= xr]
    ax.plot(d.x, d.mar * 100, color=BLUE, lw=5, solid_capstyle="round")
    dc = d[d.col.notna()]
    ax.plot(dc.x, dc.col * 100, color=ORANGE, lw=5, solid_capstyle="round")
    ax.set_xlim(x0, x1)
    ax.set_ylim(0, 105)
    ax.set_yticks([0, 50, 100])
    ax.set_yticklabels(["0%", "50%", "100%"])
    ax.set_xticks([2016, 2020, 2024])
    legend(fig, [(BLUE, "married"), (ORANGE, "bachelor's or more")], 0.09, 0.715,
           seg(t, 1.0, 1.8), dy=0.055)
    col0 = df[df.college.notna()]
    col_y1 = int(col0.m.dt.year.iloc[0]) + 1
    y0, y1 = YEARS[0], YEARS[-1]
    stat(fig, t, 3.0, 0.68, 0.64,
         f"{pct(year_mean('married', y0))} → {pct(year_mean('married', y1))}",
         f"married, {y0} → {y1}")
    stat(fig, t, 4.8, 0.68, 0.45,
         f"{pct(year_mean('college', col_y1))} → {pct(year_mean('college', y1))}",
         f"bachelor's+, {col_y1} → {y1}")
    lines(fig, t, 7.6, 0.68, 0.27, ["Shifts this fast deserve", "a close look at who, or",
                                    "what, is answering."])
    footnote(fig, t, 8.5, "Education question added in mid-2017.")


def s_work(fig, t):
    chrome(fig, t, "What the work looked like", "A side income, not a salary")
    for j, (s_, labels, title, tx, axr) in enumerate((
            (HOURS_S, HOURS_LABELS, "Hours a week on MTurk", 0.05, [0.14, 0.08, 0.31, 0.58]),
            (PAY_S, PAY_LABELS, "Earned a week on MTurk", 0.52, [0.64, 0.08, 0.31, 0.58]))):
        rise(fig, t, 0.8 + 0.3 * j, tx, 0.72, title, size=36, weight="semibold")
        grow = np.array([seg(t, 1.3 + 0.3 * j + 0.12 * i, 2.3 + 0.3 * j + 0.12 * i)
                         for i in range(len(s_))])
        bars_h(fig.add_axes(axr), labels, s_.values, grow, highlight=median_label(s_, labels),
               xpad=2.0, height=0.7)
    top = PAY_S.iloc[-2:].sum()
    footnote(fig, t, 5.0, f"Only {pct(top, 1)} earned $200+ a week.  Questions added in 2017.")


def s_closing(fig, t):
    rise(fig, t, 0.4, 0.5, 0.76, f"To the {fmt(TOT.workers)} Turkers in {TOT.countries} countries",
         size=46, color=INK, ha="center")
    rise(fig, t, 1.2, 0.5, 0.68, "who told us who they were, one HIT at a time:",
         size=36, color=INK2, ha="center")
    a = seg(t, 2.2, 3.4)
    txt(fig, 0.5, 0.48 + 0.015 * (1 - a), "Thank you.", size=150, weight="bold", ha="center",
        family=DISPLAY, alpha=a)
    # the day's 96 slots go dark one by one
    lit = 96 - int(96 * seg(t, 3.5, 8.5, smooth))
    day_ticks(fig, t, lit, y=0.32, alpha=seg(t, 2.6, 3.4))
    rise(fig, t, 4.2, 0.5, 0.22, "Amazon Mechanical Turk closed on September 30, 2026.",
         size=BODY, color=INK2, ha="center")
    rise(fig, t, 5.0, 0.5, 0.15, "demographics.mturk-tracker.com", size=BODY, color=INK,
         ha="center")
    rise(fig, t, 5.2, 0.5, 0.095, "BigQuery: mturk-demographics.demographics.responses",
         size=SMALL, color=MUTED, ha="center")


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
