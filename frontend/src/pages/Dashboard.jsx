// TICKET-ADV120 — useMemo for portfolio-value calc.
// TICKET-ADV116 — useTradeStream live feed.
import React, { useEffect, useMemo, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';
import { api } from '@services/apiService.js';
import DataTable from '@components/DataTable.jsx';
import { TradeRow } from '@components/TradeRow.jsx';
import { useNavigate } from 'react-router-dom';

const ACCENTS = {
  signal: 'border-l-signal',
  success: 'border-l-success',
  danger: 'border-l-danger',
  slate: 'border-l-slate',
};

function StatCard({ label, value, accent = 'slate' }) {
  return (
    <article className={`rounded-xl border border-line border-l-4 bg-paper p-4 shadow-sm ${ACCENTS[accent]}`}>
      <h3 className="text-xs font-medium uppercase tracking-wide text-slate">{label}</h3>
      <p className="figures mt-2 text-2xl font-semibold text-ink">{value}</p>
    </article>
  );
}

function Dashboard() {
  const { trades, isConnected } = useTradeStream();

  // With memo: a reduce over up to 200 streamed trades is expensive enough
  // to cache, reused as long as `trades` hasn't changed.
  const portfolioValue = useMemo(
    () => trades.reduce((sum, t) => sum + (t.quantity * t.price || 0), 0),
    [trades]
  );

  const [jobCount, setJobCount] = useState(0);

  useEffect(() => {
    let cancelled = false;
    api.listReconJobs('?page=0&size=1')
      .then((res) => { if (!cancelled) setJobCount(res.totalElements); })
      .catch(() => { if (!cancelled) setJobCount(0); });
    return () => { cancelled = true; };
  }, []);

  // Without memo: filter().length is cheap enough that useMemo would just
  // add complexity for no real benefit.
  const matched = trades.filter((t) => t.status === 'MATCHED').length;
  const breaks = trades.filter((t) => ['UNMATCHED', 'DISPUTED'].includes(t.status)).length;

  const navigate = useNavigate();

  const COLUMNS = [
    { key: 'tradeRef', label: 'Ref' },
    { key: 'symbol',   label: 'Symbol' },
    { key: 'qty',      label: 'Qty' },
    { key: 'price',    label: 'Price' },
    { key: 'status',   label: 'Status' },
    { key: 'created',  label: 'Created' },
  ];

  const [fallbackRecent, setFallbackRecent] = useState([]);
  useEffect(() => {
    let cancelled = false;
    // Fetch last 10 trades as a fallback if SSE doesn't provide any yet
    api.listTrades('?page=0&size=10')
      .then((res) => { if (!cancelled && res && Array.isArray(res.items)) setFallbackRecent(res.items); })
      .catch(() => { /* ignore */ });
    return () => { cancelled = true; };
  }, []);

  const recent = trades.length ? trades.slice(0, 10) : fallbackRecent.slice(0, 10);

  return (
    <section>
      <h2 className="font-display text-2xl font-semibold text-ink">Dashboard</h2>
      <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-5">
        <StatCard label="Portfolio value (USD)" value={portfolioValue.toLocaleString()} accent="signal" />
        <StatCard label="Trades streamed" value={trades.length} />
        <StatCard label="Matched" value={matched} accent="success" />
        <StatCard label="Recon jobs" value={jobCount} />
        <StatCard label="Open breaks" value={breaks} accent="danger" />
      </div>
      <div
        role="status"
        aria-live="polite"
        className="mt-4 inline-flex items-center gap-2 text-sm text-slate"
      >
        <span
          className={`h-2 w-2 rounded-full ${isConnected ? 'bg-success' : 'bg-danger'}`}
          aria-hidden="true"
        />
        SSE: {isConnected ? 'connected' : 'disconnected'}
      </div>

      <div className="mt-6">
        <h3 className="font-display text-lg font-semibold text-ink">Latest Trades</h3>
        <div className="mt-3">
          <DataTable columns={COLUMNS}>
            <DataTable.Header columns={COLUMNS} />
            <DataTable.Body rows={recent} render={(t) => <TradeRow trade={t} onClick={(id) => navigate(`/trades/${id}`)} />} />
          </DataTable>
        </div>
      </div>
    </section>
  );
}


export default withAuth(Dashboard);
