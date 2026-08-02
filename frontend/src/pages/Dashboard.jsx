// TICKET-ADV120 — useMemo for portfolio-value calc.
// TICKET-ADV116 — useTradeStream live feed.
import React, { useEffect, useMemo, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';
import { api } from '@services/apiService.js';
import DataTable from '@components/DataTable.jsx';
import { TradeRow } from '@components/TradeRow.jsx';
import TradeVolumeChart from '@components/TradeVolumeChart.jsx';
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

  const [totalTradesCount, setTotalTradesCount] = useState(null);
  const [pendingCount, setPendingCount] = useState(null);
  const [processedCount, setProcessedCount] = useState(null);

  useEffect(() => {
    let cancelled = false;
    api.listReconJobs('?page=0&size=1')
      .then((res) => { if (!cancelled) setJobCount(res.totalElements); })
      .catch(() => { if (!cancelled) setJobCount(0); });

    // Fetch total, pending, matched and breaks counts using the count endpoint
    Promise.all([
      api.countTrades(),
      api.countTrades('PENDING'),
      api.countTrades('MATCHED'),
      api.countTrades('BREAK'),
    ]).then(([totalRes, pendingRes, matchedRes, breakRes]) => {
      if (cancelled) return;
      const parse = (r) => (r && (r.count ?? r.total ?? r.totalElements ?? Number(r))) || 0;
      const total = parse(totalRes);
      const pending = parse(pendingRes);
      const matched = parse(matchedRes);
      const breaks = parse(breakRes);
      setTotalTradesCount(total);
      setPendingCount(pending);
      setProcessedCount(Math.max(0, total - pending));
      setMatchedCount(matched);
      setBreaksCount(breaks);
    }).catch(() => {
      /* ignore */
    });

    return () => { cancelled = true; };
  }, []);

  // Without memo: filter().length is cheap enough that useMemo would just
  // add complexity for no real benefit.
  // const matched = trades.filter((t) => t.status === 'MATCHED').length;
  // const breaks = trades.filter((t) => ['UNMATCHED', 'DISPUTED'].includes(t.status)).length;

  // Fallback counts computed from SSE for when server counts are unavailable
  const matchedFallback = trades.filter((t) => t.status === 'MATCHED').length;
  const breaksFallback = trades.filter((t) => ['UNMATCHED', 'DISPUTED'].includes(t.status)).length;

  const [matchedCount, setMatchedCount] = useState(null);
  const [breaksCount, setBreaksCount] = useState(null);

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
  const [tradeVolumeData, setTradeVolumeData] = useState(null);

  useEffect(() => {
    let cancelled = false;
    // Fetch last 10 trades as a fallback if SSE doesn't provide any yet
    api.listTrades('?page=0&size=10')
      .then((res) => { if (!cancelled && res && Array.isArray(res.items)) setFallbackRecent(res.items); })
      .catch(() => { /* ignore */ });
    return () => { cancelled = true; };
  }, []);

  // Fetch trade volume for current year up to today when entering dashboard
  useEffect(() => {
    let cancelled = false;
    try {
      const today = new Date();
      const from = `${today.getFullYear()}-01-01`;
      const to = today.toISOString().slice(0, 10);
      api.tradeVolume(from, to)
        .then((res) => {
          if (cancelled) return;
          if (!Array.isArray(res)) {
            setTradeVolumeData(null);
            return;
          }
          const series = [{ id: 'trades', data: res.map((r) => ({ x: r.tradeDate, y: Number(r.volume) })) }];
          // sort by date
          series[0].data.sort((a, b) => a.x.localeCompare(b.x));
          setTradeVolumeData(series);
        })
        .catch(() => { if (!cancelled) setTradeVolumeData(null); });
    } catch (e) {
      setTradeVolumeData(null);
    }
    return () => { cancelled = true; };
  }, []);

  const recent = trades.length ? trades.slice(0, 10) : fallbackRecent.slice(0, 10);

  return (
    <section>
      <h2 className="font-display text-2xl font-semibold text-ink">Dashboard</h2>
      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="lg:col-span-1">
          <div className="grid grid-cols-2 gap-3 min-h-[320px]">
            <StatCard label="Total trades" value={totalTradesCount ?? '0'} />
            <StatCard label="Trades processed" value={processedCount ?? '0'} />
            <StatCard label="Matched" value={matchedCount ?? matchedFallback} accent="success" />
            <StatCard label="Open breaks" value={breaksCount ?? breaksFallback} accent="danger" />
            <StatCard label="Recon jobs" value={jobCount} accent="neutral" />
            <div />
          </div>
        </div>

        <div className="lg:col-span-2">
          <TradeVolumeChart data={tradeVolumeData ?? undefined} height={320} />
        </div>
      </div>

      <div className="mt-10">
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
