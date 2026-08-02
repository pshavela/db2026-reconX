#!/usr/bin/env python3
"""Generate internal and external trades CSVs.

Usage: python generator.py T B D M
  T: total number of trades to generate (int)
  B: percentage of trades to deviate in external file (int 0-100)
  D: maximum deviation percent for price/quantity (int 0-100)
  M: percentage of trades missing in external file (int 0-100)

Outputs (in the same directory): internal_trades.csv and external_trades.csv

Behavior:
- Internal file contains T unique trades.
- External file starts from internal, removes M% of trades (random), and for B% of the original
  trades (selected from remaining) alters quantity and price by up to ±D%.

Deterministic: uses a fixed RNG seed for reproducible outputs.
"""

from __future__ import annotations
import csv
import sys
import random
import string
from datetime import date, timedelta
from typing import List, Dict, Tuple

# Configuration / constants
SEED = 2345235
CURRENCIES = ["USD", "EUR", "GBP", "JPY", "CHF"]
SIDES = ["BUY", "SELL"]
INSTRUMENT_MIN, INSTRUMENT_MAX = 1, 15
COUNTERPARTY_MIN, COUNTERPARTY_MAX = 1, 10
QUANTITY_MIN, QUANTITY_MAX = 1.0, 100.0
PRICE_MIN, PRICE_MAX = 10.0, 500.0
START_DATE = date(2026, 1, 1)
END_DATE = date(2026, 6, 30)

INTERNAL_FILE = "internal_trades.csv"
EXTERNAL_FILE = "external_trades.csv"

# Helpers
rng = random.Random(SEED)


def random_date(rng: random.Random) -> date:
    total_days = (END_DATE - START_DATE).days + 1
    return START_DATE + timedelta(days=rng.randrange(total_days))


def gen_trade_ref(prefix_letters: str, trade_date: date, counter: int) -> str:
    """Return tradeRef like AAA-YYYYMMDD-NNNN. counter is integer used for the last 4 digits."""
    date_part = trade_date.strftime("%Y%m%d")
    return f"{prefix_letters}-{date_part}-{counter:04d}"


def random_letters(rng: random.Random, length: int = 3) -> str:
    return ''.join(rng.choice(string.ascii_uppercase) for _ in range(length))


def format_float(v: float) -> str:
    # keep reasonable precision
    return f"{v:.4f}" if (v % 1) != 0 else f"{v:.0f}"


def make_internal_trades(T: int) -> List[Dict[str, str]]:
    trades: List[Dict[str, str]] = []
    used_refs = set()
    counter = 1
    while len(trades) < T:
        tdate = random_date(rng)
        letters = random_letters(rng, 3)
        trade_ref = gen_trade_ref(letters, tdate, counter)
        # ensure uniqueness; if collision, bump counter and retry
        if trade_ref in used_refs:
            counter += 1
            continue
        used_refs.add(trade_ref)

        instrument_id = rng.randint(INSTRUMENT_MIN, INSTRUMENT_MAX)
        counterparty_id = rng.randint(COUNTERPARTY_MIN, COUNTERPARTY_MAX)
        quantity = round(rng.uniform(QUANTITY_MIN, QUANTITY_MAX), 4)
        price = round(rng.uniform(PRICE_MIN, PRICE_MAX), 4)
        currency = rng.choice(CURRENCIES)
        side = rng.choice(SIDES)

        trades.append({
            "tradeRef": trade_ref,
            "instrumentId": str(instrument_id),
            "counterpartyId": str(counterparty_id),
            "quantity": format_float(quantity),
            "price": format_float(price),
            "currency": currency,
            "side": side,
            "tradeDate": tdate.isoformat(),
        })

        counter += 1
    return trades


def write_csv(path: str, rows: List[Dict[str, str]]) -> None:
    if not rows:
        return
    fieldnames = ["tradeRef", "instrumentId", "counterpartyId", "quantity", "price", "currency", "side", "tradeDate"]
    with open(path, "w", newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for r in rows:
            writer.writerow(r)


def make_external_from_internal(internal: List[Dict[str, str]], B_pct: float, D_pct: float, M_pct: float) -> List[Dict[str, str]]:
    T = len(internal)
    # determine missing count
    missing_count = int(round(M_pct / 100.0 * T))
    indices = list(range(T))
    rng.shuffle(indices)
    missing_indices = set(indices[:missing_count])

    # remaining indices
    remaining_indices = [i for i in indices if i not in missing_indices]

    # determine deviation count (based on B% of T)
    deviate_count = int(round(B_pct / 100.0 * T))
    # choose deviating indices from remaining
    rng.shuffle(remaining_indices)
    deviating_indices = set(remaining_indices[:min(deviate_count, len(remaining_indices))])

    external: List[Dict[str, str]] = []
    for i, row in enumerate(internal):
        if i in missing_indices:
            # skip - missing in external
            continue
        new_row = row.copy()
        if i in deviating_indices:
            # apply deviation up to ±D_pct percent to quantity and price
            def dev_value(val_str: str) -> str:
                try:
                    val = float(val_str)
                except Exception:
                    return val_str
                pct = rng.uniform(-D_pct, D_pct) / 100.0
                new_val = val * (1.0 + pct)
                # keep 4 decimals
                return format_float(round(new_val, 4))
            new_row["quantity"] = dev_value(row["quantity"])
            new_row["price"] = dev_value(row["price"])
        external.append(new_row)
    return external


def parse_args(argv: List[str]) -> Tuple[int, float, float, float]:
    if len(argv) != 5:
        print("Usage: python generator.py T B D M\n  T: count, B: % deviating, D: % max deviation, M: % missing")
        sys.exit(1)
    try:
        T = int(argv[1])
        B = float(argv[2])
        D = float(argv[3])
        M = float(argv[4])
    except Exception as e:
        print("Invalid arguments:", e)
        sys.exit(1)
    if T <= 0:
        print("T must be > 0")
        sys.exit(1)
    for name, v in (("B", B), ("D", D), ("M", M)):
        if v < 0 or v > 100:
            print(f"{name} must be between 0 and 100")
            sys.exit(1)
    return T, B, D, M


def main(argv: List[str]) -> None:
    T, B, D, M = parse_args(argv)

    internal = make_internal_trades(T)
    write_csv(INTERNAL_FILE, internal)
    print(f"Wrote {len(internal)} trades to {INTERNAL_FILE}")

    external = make_external_from_internal(internal, B, D, M)
    write_csv(EXTERNAL_FILE, external)
    print(f"Wrote {len(external)} trades to {EXTERNAL_FILE} (missing ~{int(round(M/100*T))}, deviated ~{int(round(B/100*T))})")


if __name__ == "__main__":
    main(sys.argv)
