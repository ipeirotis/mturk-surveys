#!/usr/bin/env python3
"""Population estimates for the homage video, following Difallah, Filatova &
Ipeirotis, "Demographics and Dynamics of Mechanical Turk Workers" (WSDM 2018).

Capture occasions are 30-day periods starting 2015-03-26 (a worker can take the
survey once every 30 days). Per-worker capture histories are pulled from
BigQuery into memory only; everything written to data/ is aggregate.

  * Open-population model (paper Sec. 4.2): log(n_a n_b / m_ab) = log N_b + λ t,
    fit by OLS; half-life = ln 2 / λ.
  * Chao's lower bound (paper Sec. 4.3.2): S + (n-1)/n · f1² / (2 f2). Valid under
    any heterogeneity in the propensity to participate.
  * Zero-truncated beta-binomial super-population model (paper Eq. 4).
  * Finite mixtures of binomials (the paper's Pledger-style check), 2-6 classes.

    python3 estimate_population.py          # writes data/pop_*.csv
"""
import io
import os
import subprocess

import numpy as np
import pandas as pd
from scipy import sparse
from scipy.optimize import minimize
from scipy.special import betaln, gammaln

HERE = os.path.dirname(os.path.abspath(__file__))
DATA = os.path.join(HERE, "data")
START = pd.Timestamp("2015-03-26")
ERAS = [("2015–2019", "2015-03-26", "2019-12-31"), ("2020–2022", "2020-01-01", "2022-12-31"),
        ("2023–2026", "2023-01-01", "2026-09-30")]


def load_captures():
    sql = open(os.path.join(HERE, "merged.sql")).read() + """
        SELECT FARM_FINGERPRINT(worker_id) w, DATE_DIFF(DATE(date), DATE '2015-03-26', DAY) day
        FROM all_answers GROUP BY 1, 2"""
    out = subprocess.run(["bq", "query", "--nouse_legacy_sql", "--format=csv", "--quiet",
                          "--max_rows=2000000"], input=sql, capture_output=True, text=True,
                         check=True).stdout
    caps = pd.read_csv(io.StringIO(out))
    caps["occ"] = caps.day // 30
    return caps


def occ_of(date):
    return (pd.Timestamp(date) - START).days // 30


# ---------------------------------------------------------------- closed-population models
def log_binom(k, n):
    return gammaln(n + 1) - gammaln(k + 1) - gammaln(n - k + 1)


def chao(freq, n):
    f1, f2, s = freq.get(1, 0), freq.get(2, 0), sum(freq.values())
    return s + (n - 1) / n * f1 * f1 / (2 * f2)


def beta_binomial(freq, n):
    k = np.array(sorted(freq))
    f = np.array([freq[x] for x in k], float)

    def logp(kk, a, b):
        return log_binom(kk, n) + betaln(kk + a, n - kk + b) - betaln(a, b)

    def nll(x):
        a, b = np.exp(x)
        p0 = np.exp(logp(0, a, b))
        return np.inf if p0 >= 1 else -(f * (logp(k, a, b) - np.log1p(-p0))).sum()

    best = min((minimize(nll, np.log([a0, b0]), method="Nelder-Mead",
                         options=dict(maxiter=20000, xatol=1e-9, fatol=1e-9))
                for a0 in (0.05, 0.3, 1.0) for b0 in (5, 20, 100)), key=lambda r: r.fun)
    a, b = np.exp(best.x)
    p0 = np.exp(logp(0, a, b))
    return dict(alpha=a, beta=b, N=f.sum() / (1 - p0) if p0 < 1 else np.inf)


def binomial_mixture(freq, n, classes, starts=8, seed=0):
    k = np.array(sorted(freq))
    f = np.array([freq[x] for x in k], float)
    rng = np.random.default_rng(seed)

    def unpack(x):
        w = np.exp(np.r_[0, x[:classes - 1]])
        return w / w.sum(), 1 / (1 + np.exp(-x[classes - 1:]))

    def nll(x):
        w, p = unpack(x)
        lk = np.exp(log_binom(k[:, None], n) + k[:, None] * np.log(p) +
                    (n - k[:, None]) * np.log1p(-p)) @ w
        p0 = (w * (1 - p) ** n).sum()
        return -(f * (np.log(lk) - np.log1p(-p0))).sum()

    best = None
    for _ in range(starts):
        p0 = np.sort(rng.uniform(0.002, 0.5, classes))
        x0 = np.r_[rng.normal(0, 1, classes - 1), np.log(p0 / (1 - p0))]
        r = minimize(nll, x0, method="Nelder-Mead", options=dict(maxiter=40000))
        r = minimize(nll, r.x, method="BFGS")
        if best is None or r.fun < best.fun:
            best = r
    w, p = unpack(best.x)
    p0 = (w * (1 - p) ** n).sum()
    return dict(N=f.sum() / (1 - p0), aic=2 * best.fun + 2 * (2 * classes - 1))


def equal_catchability(freq, n):
    """Zero-truncated binomial: what you'd conclude if every worker were alike."""
    k = np.array(sorted(freq))
    f = np.array([freq[x] for x in k], float)
    kbar = (k * f).sum() / f.sum()
    p = minimize(lambda q: (q[0] * n / (1 - (1 - q[0]) ** n) - kbar) ** 2, [kbar / n],
                 bounds=[(1e-6, 0.999)]).x[0]
    return dict(p=p, N=f.sum() / (1 - (1 - p) ** n))


def year_window(caps, year):
    """Capture frequencies within one calendar year, with 30-day occasions counted from the
    year's first survey day (the last occasion absorbs the leftover days)."""
    first = max(pd.Timestamp(f"{year}-01-01"), START)
    d0 = (first - START).days
    d1 = (pd.Timestamp(f"{year}-12-31") - START).days
    w = caps[(caps.day >= d0) & (caps.day <= d1)]
    span = w.day.max() - d0 + 1
    n_occ = max(1, span // 30)
    occ = np.minimum((w.day - d0) // 30, n_occ - 1)
    freq = w.assign(o=occ).groupby("w").o.nunique().value_counts().to_dict()
    return freq, int(occ.nunique())


def window(caps, o0, o1):
    w = caps[(caps.occ >= o0) & (caps.occ <= o1)]
    n = w.occ.nunique()
    freq = w.groupby("w").occ.nunique().value_counts().to_dict()
    return freq, n


# ---------------------------------------------------------------- open-population model
def open_population(caps, o0, o1, min_gap=2):
    ids, wi = np.unique(caps.w.values, return_inverse=True)
    P = caps.occ.max() + 1
    X = sparse.csr_matrix((np.ones(len(caps)), (wi, caps.occ.values)), shape=(len(ids), P))
    X.data[:] = 1
    n = np.asarray(X.sum(0)).ravel()
    M = (X.T @ X).toarray()
    rows = [(a, b, (b - a) * 30, np.log(n[a] * n[b] / M[a, b]), M[a, b] / n[a])
            for a in range(o0, o1 + 1) for b in range(a + min_gap, o1 + 1)
            if n[a] >= 500 and n[b] >= 500 and M[a, b] >= 20]
    d = pd.DataFrame(rows, columns=["a", "b", "t", "y", "back"])
    bs = sorted(d.b.unique())
    D = np.zeros((len(d), len(bs) + 1))
    for j, b in enumerate(bs):
        D[d.b.values == b, j] = 1
    D[:, -1] = d.t.values
    coef, *_ = np.linalg.lstsq(D, d.y.values, rcond=None)
    lam = coef[-1]
    back = d.assign(gap=d.b - d.a).groupby("gap").back.median()
    return dict(half_life=np.log(2) / lam, Nd=np.median(np.exp(coef[:-1])), back=back)


def main():
    caps = load_captures()
    last = caps.occ.max()
    os.makedirs(DATA, exist_ok=True)

    # Validation against the paper's 28 periods (March 2015 - July 2017)
    freq, n = window(caps, 0, 27)
    op = open_population(caps, 0, 27)
    val = dict(workers=sum(freq.values()), chao=chao(freq, n), half_life=op["half_life"],
               active_equal_catchability=op["Nd"])
    pd.DataFrame([val]).to_csv(os.path.join(DATA, "pop_validation.csv"), index=False)
    print("validation (paper: 39,461 workers, Chao 97,579, half-life 404 d, ~12K):", val)

    # Whole 2015-2026 span
    freq, n = window(caps, 0, last)
    eq = equal_catchability(freq, n)
    total = dict(occasions=n, workers=sum(freq.values()), chao=chao(freq, n), equal_N=eq["N"],
                 beta_binomial_N=beta_binomial(freq, n)["N"])
    for c in (3, 4, 5, 6):
        r = binomial_mixture(freq, n, c)
        total[f"mix{c}_N"], total[f"mix{c}_aic"] = r["N"], r["aic"]
    pd.DataFrame([total]).to_csv(os.path.join(DATA, "pop_total.csv"), index=False)
    print("total:", total)

    # Times seen, with what equal catchability would predict
    kmax = max(freq)
    ks = np.arange(1, kmax + 1)
    expected = eq["N"] * np.exp(log_binom(ks, n) + ks * np.log(eq["p"]) +
                                (n - ks) * np.log1p(-eq["p"]))
    pd.DataFrame(dict(k=ks, observed=[freq.get(k, 0) for k in ks], equal=expected)).to_csv(
        os.path.join(DATA, "pop_times_seen.csv"), index=False)

    # Per calendar year: workers seen and Chao's lower bound on workers available
    rows = []
    for y in range(2015, 2027):
        freq, n = year_window(caps, y)
        rows.append(dict(year=y, occasions=n, workers=sum(freq.values()), chao=chao(freq, n)))
    pd.DataFrame(rows).to_csv(os.path.join(DATA, "pop_years.csv"), index=False)
    print(pd.DataFrame(rows))

    # Half-life by era, and the share of a period's workers seen again t periods later
    rows, backs = [], []
    for name, a, b in ERAS:
        op = open_population(caps, max(0, occ_of(a)), min(last, occ_of(b)))
        rows.append(dict(era=name, half_life_days=op["half_life"],
                         active_equal_catchability=op["Nd"]))
        backs.append(op["back"].rename(name))
    pd.DataFrame(rows).to_csv(os.path.join(DATA, "pop_halflife.csv"), index=False)
    pd.concat(backs, axis=1).rename_axis("gap").to_csv(os.path.join(DATA, "pop_return.csv"))
    print(pd.DataFrame(rows))


if __name__ == "__main__":
    main()
