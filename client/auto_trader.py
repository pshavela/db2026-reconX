import sys
import time
import json
import datetime
import requests
import random
import traceback

VALID_INSTRUMENT_IDS = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15]
VALID_COUNTERPARTY_IDS = [1,2,3,4,5,6,7,8,9,10]
VALID_SIDES = ["BUY", "SELL"]
VALID_CURRENCIES = ["USD", "EUR", "GBP", "JPY", "CHF"]
MAX_SLEEP_DURATION_MS = 1000

# Use a local RNG instance so global random state is not mutated unexpectedly
RNG = random.Random(23423)
TRADE_COUNTER = 0


def post_json_requests(url, headers, json):
    r = requests.post(url, json=json, headers=headers, timeout=10)
    return r.status_code, getattr(r, 'text', '')


def main():
    global TRADE_COUNTER
    if len(sys.argv) != 3:
        print("Usage: python auto_trader.py <host> <port>", file=sys.stderr)
        sys.exit(1)

    HOST = sys.argv[1]
    PORT = sys.argv[2]

    HEADERS = json.loads(
        post_json_requests(
            url=f"http://{HOST}:{PORT}/api/auth/login", 
            headers={'Content-Type': 'Application/json'}, 
            json={"email":"admin@db.com","password":"admin123"}
            )[1]
    )["token"]

    HEADERS = { "Authorization": f"Bearer {HEADERS}", "Content-Type": "application/json" }

    try:
        while True:
            TRADE_COUNTER += 1
            instrument_id = RNG.choice(VALID_INSTRUMENT_IDS)
            counterparty_id = RNG.choice(VALID_COUNTERPARTY_IDS)
            side = RNG.choice(VALID_SIDES)
            currency = RNG.choice(VALID_CURRENCIES)
            quantity = 1.0 + 100.0 * RNG.random()
            price = 1.0 + 1000.0 * RNG.random()
            trade_date = datetime.datetime.now()

            trade_ref = str(TRADE_COUNTER).zfill(12)
            trade_ref = f'TRD-{trade_ref[:8]}-{trade_ref[8:]}'
            
            payload = {
                "tradeRef": trade_ref,
                "instrumentId": str(instrument_id),
                "counterpartyId": str(counterparty_id),
                "assetClass": "EQUITY",
                "side": str(side),
                "quantity": str(quantity),
                "price": str(price),
                "currency": str(currency),
                "tradeDate": str(trade_date.isoformat()),
            }

            try:
                status, body = post_json_requests(f"http://{HOST}:{PORT}/api/v1/trades", headers=HEADERS, json=payload)
                print(f"trade_ref={trade_ref} status={status}")
            except Exception as e:
                print(f"trade_ref={trade_ref} request failed: {e}", file=sys.stderr)
                # also print traceback for debugging
                traceback.print_exc()

            delay = RNG.randrange(1, MAX_SLEEP_DURATION_MS)
            time.sleep(delay / 1000)
    except KeyboardInterrupt:
        print('\nInterrupted, exiting')


if __name__ == '__main__':
    main()
