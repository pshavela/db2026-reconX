// Review page — shows OPEN and RESOLVED breaks in two stacked tables
import React, { useEffect, useState, useCallback } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import { api } from '@services/apiService.js';
import { formatTimestamp } from '@/lib/utils';

function Review() {
  const [openPage, setOpenPage] = useState(0);
  const [openData, setOpenData] = useState({ items: [], totalPages: 0 });
  const [openSelected, setOpenSelected] = useState(new Set());

  const [resolvedPage, setResolvedPage] = useState(0);
  const [resolvedData, setResolvedData] = useState({ items: [], totalPages: 0 });

  const COLUMNS_OPEN = [
    { key: 'id', label: 'ID' },
    { key: 'tradeId', label: 'Trade' },
    { key: 'discrepancyType', label: 'Type' },
    { key: 'detectedAt', label: 'Detected' },
  ];

  const COLUMNS_RESOLVED = [
    { key: 'id', label: 'ID' },
    { key: 'tradeId', label: 'Trade' },
    { key: 'discrepancyType', label: 'Type' },
    { key: 'resolvedAt', label: 'Resolved' },
    { key: 'resolutionNote', label: 'Note' },
  ]

  useEffect(() => {
    let cancelled = false;
    api.listBreaks(`?status=OPEN&page=${openPage}&size=10`)
      .then((res) => { if (!cancelled) setOpenData(res); })
      .catch(() => { if (!cancelled) setOpenData({ items: [], totalPages: 0 }); });
    return () => { cancelled = true; };
  }, [openPage]);

  useEffect(() => {
    let cancelled = false;
    api.listBreaks(`?status=RESOLVED&page=${resolvedPage}&size=10`)
      .then((res) => { if (!cancelled) setResolvedData(res); })
      .catch(() => { if (!cancelled) setResolvedData({ items: [], totalPages: 0 }); });
    return () => { cancelled = true; };
  }, [resolvedPage]);

  const toggleSelect = useCallback((id) => {
    setOpenSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  }, []);

  return (
    <section>
      <h2 className="font-display text-2xl font-semibold text-ink">Review</h2>

      <div className="mt-6">
        <h3 className="font-display text-lg font-semibold text-ink">Open Breaks</h3>
        <div className="mt-3">
          <DataTable columns={COLUMNS_OPEN}>
            <DataTable.Header columns={COLUMNS_OPEN} />
            <DataTable.Body
              rows={openData.items}
              render={(b) => (
                <>
                  <span className="figures text-ink">{b.id}</span>
                  <span className="text-slate">{b.tradeId}</span>
                  <span className="text-slate">{b.discrepancyType}</span>
                  <span className="figures text-ink">{formatTimestamp(b.detectedAt)}</span>
                </>
              )}
            />
            <DataTable.Pagination page={openPage} totalPages={Math.max(1, openData.totalPages)} onChange={setOpenPage} />
          </DataTable>
        </div>
      </div>

      <div className="mt-6">
        <h3 className="font-display text-lg font-semibold text-ink">Resolved Breaks</h3>
        <div className="mt-3">
          <DataTable columns={COLUMNS_RESOLVED}>
            <DataTable.Header columns={COLUMNS_RESOLVED} />
            <DataTable.Body
              rows={resolvedData.items}
              render={(b) => (
                <>
                  <span className="figures text-ink">{b.id}</span>
                  <span className="text-slate">{b.tradeId}</span>
                  <span className="text-slate">{b.discrepancyType}</span>
                  <span className="figures text-ink">{formatTimestamp(b.detectedAt)}</span>
                  <span className="text-slate">{b.resolvedAt ? formatTimestamp(b.resolvedAt) : '—'}</span>
                  <span className="text-slate break-words">{b.resolutionNote || ''}</span>
                </>
              )}
            />
            <DataTable.Pagination page={resolvedPage} totalPages={Math.max(1, resolvedData.totalPages)} onChange={setResolvedPage} />
          </DataTable>
        </div>
      </div>
    </section>
  );
}

export default withAuth(Review);
