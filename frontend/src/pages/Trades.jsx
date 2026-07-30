// TICKET-ADV114 — Compound DataTable.
// TICKET-ADV117 — useDebouncedSearch.
import React, { useCallback, useEffect, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import { TradeRow } from '@components/TradeRow.jsx';
import { useDebouncedSearch } from '@hooks/useDebouncedSearch.js';
import { useAuth } from '@context/AuthContext.jsx';
import { api } from '@services/apiService.js';

function Trades() {
  const { user } = useAuth();
  const [search, setSearch] = useState('');
  const debounced = useDebouncedSearch(search, 300);
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ items: [], totalPages: 0 });
  const [selectedId, setSelectedId] = useState(null);
  const [actionError, setActionError] = useState(null);
  // Bumped after a status change / delete to re-run the fetch effect below.
  const [refreshTick, setRefreshTick] = useState(0);

  // Reference-stable across renders — onClick prop on <TradeRow> won't change,
  // so ADV119's React.memo equality check on it actually holds.
  const handleSelect = useCallback((id) => setSelectedId(id), []);

  const handleStatusChange = useCallback(async (id, status) => {
    setActionError(null);
    try {
      await api.updateStatus(id, status);
      setRefreshTick((t) => t + 1);
    } catch (err) {
      setActionError(err.message);
    }
  }, []);

  const handleDelete = useCallback(async (id) => {
    if (!window.confirm('Delete this trade? This cannot be undone.')) return;
    setActionError(null);
    try {
      await api.deleteTrade(id);
      setRefreshTick((t) => t + 1);
    } catch (err) {
      setActionError(err.message);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;
    const params = new URLSearchParams({ page: String(page), size: '20' });
    if (debounced) params.set('status', debounced);

    api.listTrades(`?${params.toString()}`)
      .then((res) => { if (!cancelled) setData(res); })
      .catch(() => { if (!cancelled) setData({ items: [], totalPages: 0 }); });

    return () => { cancelled = true; };
  }, [page, debounced, refreshTick]);

  return (
    <section>
      <h2>Trades</h2>
      <input
        aria-label="Filter by status"
        placeholder="status filter (PENDING/MATCHED/…)"
        value={search}
        onChange={(e) => setSearch(e.target.value.toUpperCase())}
      />
      {actionError && <p role="alert" className="form-error">{actionError}</p>}
      <DataTable>
        <DataTable.Header columns={[
          { key: 'tradeRef', label: 'Ref' },
          { key: 'symbol',   label: 'Symbol' },
          { key: 'qty',      label: 'Qty' },
          { key: 'price',    label: 'Price' },
          { key: 'status',   label: 'Status' },
          { key: 'actions',  label: 'Actions' },
        ]} />
        <DataTable.Body
          rows={data.items}
          render={(t) => (
            <TradeRow
              trade={t}
              onClick={handleSelect}
              role={user?.role}
              onStatusChange={handleStatusChange}
              onDelete={handleDelete}
            />
          )}
        />
        <DataTable.Pagination
          page={page}
          totalPages={Math.max(1, data.totalPages)}
          onChange={setPage}
        />
      </DataTable>
      {selectedId != null && <p>Selected trade id: {selectedId}</p>}
    </section>
  );
}

export default withAuth(Trades);
