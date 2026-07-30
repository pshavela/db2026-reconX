// TICKET-ADV120 — useMemo for portfolio-value calc.
// TICKET-ADV116 — useTradeStream live feed.
import React, { useMemo } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';

function StatCard({ label, value }) {
  return (
    <article className="stat-card">
      <h3>{label}</h3>
      <p>{value}</p>
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

  // Without memo: filter().length is cheap enough that useMemo would just
  // add complexity for no real benefit.
  const matched = trades.filter((t) => t.status === 'MATCHED').length;
  const breaks = trades.filter((t) => ['UNMATCHED', 'DISPUTED'].includes(t.status)).length;

  return (
    <section>
      <h2>Dashboard</h2>
      <div className="stat-grid">
        <StatCard label="Portfolio value (USD)" value={portfolioValue.toLocaleString()} />
        <StatCard label="Trades streamed" value={trades.length} />
        <StatCard label="Matched" value={matched} />
        <StatCard label="Open breaks" value={breaks} />
      </div>
      <div role="status" aria-live="polite">
        SSE: {isConnected ? 'connected' : 'disconnected'}
      </div>
    </section>
  );
}

export default withAuth(Dashboard);
