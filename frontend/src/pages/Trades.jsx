// TICKET-ADV114 — Compound DataTable.
// TICKET-ADV117 — useDebouncedSearch.
import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import { TradeRow } from '@components/TradeRow.jsx';
import { useDebouncedSearch } from '@hooks/useDebouncedSearch.js';
import { api } from '@services/apiService.js';

const COLUMNS = [
  { key: 'tradeRef', label: 'Ref' },
  { key: 'symbol',   label: 'Symbol' },
  { key: 'qty',      label: 'Qty' },
  { key: 'price',    label: 'Price' },
  { key: 'status',   label: 'Status' },
  { key: 'created',  label: 'Created' },
];

function Trades() {
  const [search, setSearch] = useState('');
  const debounced = useDebouncedSearch(search, 300);
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ items: [], totalPages: 0 });
  const [selectedId, setSelectedId] = useState(null);

  // Reference-stable across renders — onClick prop on <TradeRow> won't change,
  // so ADV119's React.memo equality check on it actually holds.
  const navigate = useNavigate();
  const handleSelect = useCallback((id) => { setSelectedId(id); navigate(`/trades/${id}`); }, [navigate]);

  useEffect(() => {
    let cancelled = false;
    const params = new URLSearchParams({ page: String(page), size: '20' });
    if (debounced) params.set('status', debounced);

    api.listTrades(`?${params.toString()}`)
      .then((res) => { if (!cancelled) setData(res); })
      .catch(() => { if (!cancelled) setData({ items: [], totalPages: 0 }); });

    return () => { cancelled = true; };
  }, [page, debounced]);

  return (
    <section>
      <div className="flex items-center justify-between gap-4">
        <h2 className="font-display text-2xl font-semibold text-ink">Trades</h2>
        <input
          aria-label="Filter by status"
          placeholder="Filter by status (PENDING, MATCHED, …)"
          value={search}
          onChange={(e) => setSearch(e.target.value.toUpperCase())}
          className="w-72 rounded-lg border border-line bg-paper px-3 py-2 text-sm text-ink placeholder:text-slate/70 focus:border-signal focus:outline-none"
        />
      </div>
      <div className="mt-4">
        <DataTable columns={COLUMNS}>
          <DataTable.Header columns={COLUMNS} />
          <DataTable.Body
            rows={data.items}
            render={(t) => <TradeRow trade={t} onClick={handleSelect} />}
          />
          <DataTable.Pagination
            page={page}
            totalPages={Math.max(1, data.totalPages)}
            onChange={setPage}
          />
        </DataTable>
      </div>
      {selectedId != null && (
        <p className="mt-3 text-sm text-slate">Selected trade id: <span className="figures text-ink">{selectedId}</span></p>
      )}
    </section>
  );
}

export default withAuth(Trades);
