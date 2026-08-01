// Trade detail page — fetch a single trade and render full details
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';

function Trade() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [trade, setTrade] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    api.getTrade(id)
      .then((t) => { if (!cancelled) setTrade(t); })
      .catch((err) => { if (!cancelled) setError(err.message); })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [id]);

  if (loading) {
    return <p className="text-slate">Loading trade...</p>;
  }

  if (error) {
    const notFound = error.includes('HTTP 404');
    return (
      <section>
        <div className="flex items-center justify-between gap-4">
          <h2 className="font-display text-2xl font-semibold text-ink">Trade</h2>
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="rounded-lg border border-line bg-paper px-3 py-1 text-sm text-ink"
          >
            Back
          </button>
        </div>
        <div className="mt-4 rounded-xl border border-line bg-paper p-6">
          <p className="text-danger">{notFound ? 'Trade not found' : error}</p>
        </div>
      </section>
    );
  }

  return (
    <section>
      <div className="flex items-center justify-between gap-4">
        <h2 className="font-display text-2xl font-semibold text-ink">Trade {trade.id}</h2>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="rounded-lg border border-line bg-paper px-3 py-1 text-sm text-ink cursor-pointer"
        >
          Back
        </button>
      </div>

      <div className="mt-4 grid gap-3 rounded-xl border border-line bg-paper p-6">
        {Object.entries(trade).map(([key, value]) => (
          <div key={key} className="grid grid-cols-3 gap-2 py-1">
            <div className="text-slate capitalize">{key}</div>
            <div className="col-span-2 figures text-ink break-words">{String(value)}</div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default withAuth(Trade);
