// TICKET-ADV106 — Sortable, resizable, frozen-header data table
(function () {
  'use strict';

  var table = document.getElementById('trades-table');
  var tbody = document.getElementById('trades-tbody');
  if (!table || !tbody) return;

  var rows = [];

  var qtyFmt = new Intl.NumberFormat('en-US');
  var priceFmt = new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4
  });

  function escapeHtml(s) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(s));
    return div.innerHTML;
  }

  function renderRows() {
    tbody.innerHTML = rows.map(function (r) {
      var statusClass = '';
      var s = (r.status || '').toUpperCase();
      if (s === 'MATCHED') statusClass = 'status--matched';
      else if (s === 'BREAK' || s === 'UNMATCHED') statusClass = 'status--break';
      else if (s === 'PENDING') statusClass = 'status--pending';

      return '<tr>' +
        '<td>' + escapeHtml(r.tradeRef || '') + '</td>' +
        '<td>' + escapeHtml(r.type || '') + '</td>' +
        '<td>' + escapeHtml(r.symbol || '') + '</td>' +
        '<td>' + escapeHtml(r.side || '') + '</td>' +
        '<td>' + qtyFmt.format(r.quantity || 0) + '</td>' +
        '<td>' + priceFmt.format(r.price || 0) + '</td>' +
        '<td class="' + statusClass + '">' + escapeHtml(s) + '</td>' +
        '</tr>';
    }).join('');
  }

  // ---------- Sortable columns ----------
  table.querySelectorAll('thead th').forEach(function (th) {
    th.addEventListener('click', function (e) {
      if (e.target.classList.contains('resize-handle')) return;

      var col = th.dataset.col;
      var type = th.dataset.type || 'string';
      var dir = th.getAttribute('aria-sort') === 'ascending' ? 'descending' : 'ascending';

      table.querySelectorAll('thead th').forEach(function (o) {
        o.removeAttribute('aria-sort');
      });
      th.setAttribute('aria-sort', dir);

      var mult = dir === 'ascending' ? 1 : -1;
      rows.sort(function (a, b) {
        var av = a[col], bv = b[col];
        if (av == null) av = '';
        if (bv == null) bv = '';
        if (type === 'number') return (Number(av) - Number(bv)) * mult;
        return String(av).localeCompare(String(bv)) * mult;
      });
      renderRows();
    });
  });

  // ---------- Resizable columns ----------
  table.querySelectorAll('.resize-handle').forEach(function (handle) {
    handle.addEventListener('mousedown', function (e) {
      e.preventDefault();
      e.stopPropagation();
      var th = handle.closest('th');
      var startX = e.clientX;
      var startWidth = th.offsetWidth;

      function onMove(ev) {
        th.style.width = (startWidth + ev.clientX - startX) + 'px';
      }
      function onUp() {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
      }
      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
    });
  });

  // ---------- Data loading ----------
  // Try fetching from backend API; fall back to demo data
  var demoData = [
    { tradeRef: 'EQU-20260603-0001', type: 'EQUITY',     symbol: 'SAP.DE',   side: 'BUY',  quantity: 1000,    price: 125.50,  status: 'MATCHED' },
    { tradeRef: 'EQU-20260603-0002', type: 'EQUITY',     symbol: 'AAPL',     side: 'SELL', quantity: 500,     price: 178.20,  status: 'BREAK' },
    { tradeRef: 'FX-20260603-0001',  type: 'FX',         symbol: 'EUR/USD',  side: 'BUY',  quantity: 1000000, price: 1.0852,  status: 'PENDING' },
    { tradeRef: 'BND-20260603-0001', type: 'BOND',       symbol: 'DE10Y',    side: 'BUY',  quantity: 50000,   price: 98.75,   status: 'MATCHED' },
    { tradeRef: 'DRV-20260603-0001', type: 'DERIVATIVE',  symbol: 'DAX-C-18000', side: 'BUY', quantity: 100, price: 342.00,  status: 'MATCHED' },
    { tradeRef: 'EQU-20260603-0003', type: 'EQUITY',     symbol: 'DBK.DE',   side: 'BUY',  quantity: 2000,    price: 15.83,   status: 'MATCHED' },
    { tradeRef: 'FX-20260603-0002',  type: 'FX',         symbol: 'GBP/USD',  side: 'SELL', quantity: 500000,  price: 1.2715,  status: 'PENDING' },
    { tradeRef: 'EQU-20260603-0004', type: 'EQUITY',     symbol: 'MSFT',     side: 'BUY',  quantity: 300,     price: 415.60,  status: 'BREAK' },
    { tradeRef: 'BND-20260603-0002', type: 'BOND',       symbol: 'US10Y',    side: 'SELL', quantity: 100000,  price: 96.125,  status: 'MATCHED' },
    { tradeRef: 'EQU-20260603-0005', type: 'EQUITY',     symbol: 'NVDA',     side: 'BUY',  quantity: 150,     price: 1024.50, status: 'MATCHED' },
    { tradeRef: 'DRV-20260603-0002', type: 'DERIVATIVE',  symbol: 'SPX-P-5400', side: 'SELL', quantity: 50,  price: 78.25,   status: 'PENDING' },
    { tradeRef: 'EQU-20260603-0006', type: 'EQUITY',     symbol: 'SIE.DE',   side: 'SELL', quantity: 800,     price: 172.40,  status: 'MATCHED' }
  ];

  // Load demo data immediately so the page works without a backend
  rows = demoData;
  renderRows();

  // If a backend is available, replace with live data
  if (window.location.protocol !== 'file:') {
    fetch('/api/v1/trades?size=200')
      .then(function (r) { return r.json(); })
      .then(function (data) {
        rows = data.content || data;
        renderRows();
      })
      .catch(function () {
        // keep demo data
      });
  }
})();
