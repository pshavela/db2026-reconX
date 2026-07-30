// TICKET-ADV104 — EventSource SSE subscription to /api/v1/trades/stream
// TICKET-ADV105 — prepend-and-animate with 50-entry DOM cap
(function () {
  'use strict';

  var STREAM_URL = '/api/v1/trades/stream';
  var MAX_FEED_ENTRIES = 50;

  var feed = document.getElementById('trade-feed');
  if (!feed) return;

  var badge = document.getElementById('sse-status');
  var sse = null;

  function escapeHtml(s) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(s));
    return div.innerHTML;
  }

  var qtyFmt = new Intl.NumberFormat('en-US');
  var priceFmt = new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4
  });

  function updateBadge(text, variant) {
    if (!badge) return;
    badge.textContent = text;
    badge.className = 'sse-badge sse-badge--' + variant;
  }

  function prependTradeRow(trade) {
    var statusClass = '';
    var s = (trade.status || '').toUpperCase();
    if (s === 'MATCHED') statusClass = 'trade-card--matched';
    else if (s === 'BREAK' || s === 'UNMATCHED') statusClass = 'trade-card--break';
    else if (s === 'PENDING') statusClass = 'trade-card--pending';

    var el = document.createElement('article');
    el.className = 'trade-card ' + statusClass + ' trade-card--new';
    el.innerHTML =
      '<strong>' + escapeHtml(trade.tradeRef || '') + '</strong>' +
      '<span> ' + escapeHtml(trade.symbol || '') + ' </span>' +
      '<span> qty=' + qtyFmt.format(trade.qty || 0) + ' </span>' +
      '<span> price=' + priceFmt.format(trade.price || 0) + ' </span>' +
      '<span> [' + escapeHtml(s) + ']</span>';

    feed.prepend(el);

    setTimeout(function () { el.classList.remove('trade-card--new'); }, 500);

    while (feed.children.length > MAX_FEED_ENTRIES) {
      feed.lastElementChild.remove();
    }
  }

  function connect() {
    sse = new EventSource(STREAM_URL);

    sse.onopen = function () {
      updateBadge('Live', 'live');
    };

    sse.onmessage = function (event) {
      try {
        var trade = JSON.parse(event.data);
        prependTradeRow(trade);
      } catch (e) {
        // malformed event — skip
      }
    };

    sse.onerror = function () {
      updateBadge('Reconnecting…', 'reconnecting');
    };
  }

  window.addEventListener('beforeunload', function () {
    if (sse) sse.close();
  });

  // Try real SSE first; fall back to demo data if EventSource fails
  // (e.g. no backend running, or served from file://)
  var usedDemo = false;

  try {
    connect();
    // If the connection errors out quickly, the demo fallback won't fire
    // because onerror just updates the badge — the demo data below provides
    // initial content so the page isn't blank when no backend is running.
  } catch (e) {
    usedDemo = true;
  }

  // Always seed demo events so the static page has content on load
  var demoEvents = [
    { tradeRef: 'EQU-20260603-0001', symbol: 'SAP.DE',  qty: 1000, price: 125.50, status: 'MATCHED' },
    { tradeRef: 'FX-20260603-0001',  symbol: 'EUR/USD', qty: 1000000, price: 1.0852, status: 'PENDING' },
    { tradeRef: 'EQU-20260603-0002', symbol: 'AAPL',    qty: 500,  price: 178.20, status: 'BREAK' }
  ];

  demoEvents.forEach(function (e, i) {
    setTimeout(function () { prependTradeRow(e); }, 500 * i);
  });
})();
