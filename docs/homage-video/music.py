#!/usr/bin/env python3
"""Synthesizes the soundtrack for the homage video: a slow vi-IV-I-V piano piece
over a pad, timed to the scenes in make_video.py. Everything is generated here,
so there is nothing to license.

    python3 music.py out/music.wav 124
"""
import sys
import wave

import numpy as np
from scipy.signal import butter, fftconvolve, sosfilt

SR = 44100
CLOSING_AT = 114.0      # closing card starts here; it lands on a downbeat
CLOSING_BAR = 33
BAR = CLOSING_AT / CLOSING_BAR
BEAT = BAR / 4

# (bass, pad voicing, arpeggio) per chord, as MIDI notes
CHORDS = {
    "Am": (45, (57, 60, 64), (57, 64, 69, 72, 76, 72, 69, 64)),
    "F": (41, (53, 57, 60), (53, 60, 65, 69, 72, 69, 65, 60)),
    "C": (48, (55, 60, 64), (48, 55, 60, 64, 67, 64, 60, 55)),
    "G": (43, (55, 59, 62), (55, 62, 67, 71, 74, 71, 67, 62)),
}
CYCLE = ["Am", "F", "C", "G"]

# Melody phrases, one per 4-bar cycle: (bar in cycle, beat, length in beats, MIDI)
PHRASE_A = [(0, 0, 2, 76), (0, 2, 1, 74), (0, 3, 1, 72), (1, 0, 3, 72), (1, 3, 1, 69),
            (2, 0, 2, 67), (2, 2, 2, 72), (3, 0, 2, 74), (3, 2, 2, 71)]
PHRASE_B = [(0, 0, 1.5, 76), (0, 1.5, 0.5, 79), (0, 2, 2, 76), (1, 0, 1, 77), (1, 1, 1, 76),
            (1, 2, 2, 72), (2, 0, 2, 76), (2, 2, 1, 74), (2, 3, 1, 72), (3, 0, 4, 74)]


def hz(m):
    return 440.0 * 2 ** ((m - 69) / 12)


def piano(m, dur, vel, bright=1.0):
    """Struck-string tone: inharmonic partials, each decaying faster than the last."""
    f = hz(m)
    n = int((dur + 1.6) * SR)
    t = np.arange(n) / SR
    out = np.zeros(n)
    for k in range(1, 9):
        fk = k * f * np.sqrt(1 + 0.0004 * k * k)
        if fk > SR / 2.2:
            break
        amp = k ** -1.6 * (bright if k > 2 else 1.0)
        out += amp * np.sin(2 * np.pi * fk * t) * np.exp(-t * (0.9 + 0.55 * k))
    env = np.minimum(t / 0.004, 1.0)
    env *= np.where(t > dur, np.exp(-(t - dur) * 6.0), 1.0)
    return vel * out * env


def pad(ms, dur, vel, detune_cents):
    n = int((dur + 2.0) * SR)
    t = np.arange(n) / SR
    out = np.zeros(n)
    for m in ms:
        for c in (-detune_cents, detune_cents):
            f = hz(m) * 2 ** (c / 1200)
            for k in range(1, 6):
                out += (1 / k ** 1.3) * np.sin(2 * np.pi * k * f * t + k)
    env = np.minimum(t / 0.6, 1.0) * np.where(t > dur, np.exp(-(t - dur) * 1.2), 1.0)
    trem = 1 + 0.06 * np.sin(2 * np.pi * 0.23 * t)
    return vel * out * env * trem / (2 * len(ms))


def bass(m, dur, vel):
    n = int((dur + 0.8) * SR)
    t = np.arange(n) / SR
    f = hz(m)
    out = np.sin(2 * np.pi * f * t) + 0.25 * np.sin(4 * np.pi * f * t)
    env = np.minimum(t / 0.03, 1.0) * (0.55 + 0.45 * np.exp(-t * 1.5))
    env *= np.where(t > dur, np.exp(-(t - dur) * 5.0), 1.0)
    return vel * out * env


def add(buf, sig, start, pan=0.0):
    i = int(start * SR)
    j = min(len(buf), i + len(sig))
    if j <= i:
        return
    left, right = np.cos((pan + 1) * np.pi / 4), np.sin((pan + 1) * np.pi / 4)
    buf[i:j, 0] += sig[: j - i] * left
    buf[i:j, 1] += sig[: j - i] * right


def chord_at(bar):
    if bar == CLOSING_BAR:
        return "F"
    if bar > CLOSING_BAR:
        return "C"
    return CYCLE[bar % 4]


def level(bar):
    """Overall dynamics: rise through the data scenes, ease off before the end."""
    return float(np.interp(bar, [0, 4, 10, 18, 26, 30, 33, 36],
                           [0.55, 0.7, 0.85, 0.9, 1.0, 0.8, 0.75, 0.6]))


def render(path, duration):
    buf = np.zeros((int((duration + 4) * SR), 2))
    bars = int(np.ceil(duration / BAR))
    rng = np.random.default_rng(15)
    for b in range(bars):
        start = b * BAR
        name = chord_at(b)
        root, voicing, arp = CHORDS[name]
        lv = level(b)
        final = b >= CLOSING_BAR + 1

        # pad: every bar, held across the final chord
        hold = duration - start if final else BAR
        if b <= CLOSING_BAR + 1:
            for pan, det in ((-0.6, 7), (0.6, -5)):
                add(buf, pad(voicing, hold, 0.16 * lv, det), start, pan)

        # arpeggio from bar 2; in the closing, one rolled chord instead
        if 2 <= b < CLOSING_BAR:
            for i, m in enumerate(arp):
                vel = 0.09 * lv * (1.15 if i % 4 == 0 else 1.0) * rng.uniform(0.9, 1.05)
                add(buf, piano(m, BEAT * 0.9, vel, bright=0.8), start + i * BEAT / 2, 0.25)
        elif b in (CLOSING_BAR, CLOSING_BAR + 1):
            notes = arp[:5]
            for i, m in enumerate(notes):
                d = (duration - start - 0.5) if final else BAR
                add(buf, piano(m, d, 0.11 * lv, bright=0.7), start + i * 0.09, 0.1)

        # bass from bar 6
        if 6 <= b <= CLOSING_BAR + 1:
            add(buf, bass(root, hold if final else BAR * 0.95, 0.16 * lv), start)

        # melody from bar 10 to the end of the answers scene
        if 10 <= b < 30:
            cyc = (b - 10) // 4
            phrase = PHRASE_A if cyc % 2 == 0 else PHRASE_B
            for (cb, beat, length, m) in phrase:
                if cb == (b - 10) % 4:
                    add(buf, piano(m + 12, length * BEAT, 0.11 * lv, bright=0.6),
                        start + beat * BEAT, -0.1)

    # a quiet tick on each 15-minute tile of the title card
    for i in range(0, 96, 8):
        tt = 1.2 + 5.0 * (i / 96)
        click = piano(96, 0.02, 0.02, bright=0.3)[: int(0.15 * SR)]
        add(buf, click, tt, 0.4)

    # room: synthetic stereo reverb
    irn = int(2.8 * SR)
    t = np.arange(irn) / SR
    lp = butter(2, 5000, fs=SR, output="sos")
    wet = np.zeros_like(buf)
    for ch in range(2):
        ir = sosfilt(lp, rng.standard_normal(irn)) * np.exp(-t / 0.9)
        ir /= np.sqrt(np.sum(ir ** 2))
        wet[:, ch] = fftconvolve(buf[:, ch], ir)[: len(buf)]
    mix = buf + 0.35 * wet
    mix = sosfilt(butter(2, 40, btype="highpass", fs=SR, output="sos"), mix, axis=0)

    n = int(duration * SR)
    mix = mix[:n]
    t = np.arange(n) / SR
    fade = np.minimum(t / 2.0, 1.0) * np.clip((duration - t) / 3.5, 0, 1)
    mix *= fade[:, None]
    mix = np.tanh(mix / np.max(np.abs(mix)) * 1.2) / np.tanh(1.2) * 0.89  # peak ~ -1 dBFS
    pcm = (mix * 32767).astype("<i2")
    with wave.open(path, "wb") as w:
        w.setnchannels(2)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(pcm.tobytes())
    return path


if __name__ == "__main__":
    render(sys.argv[1], float(sys.argv[2]))
