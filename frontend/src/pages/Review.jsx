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

  const [modalOpen, setModalOpen] = useState(false);
  const [modalBreak, setModalBreak] = useState(null);
  const [resolutionNote, setResolutionNote] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState(null);

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

  const fetchOpen = useCallback((page = openPage) => {
    let cancelled = false;
    api.listBreaks(`?status=OPEN&page=${page}&size=10`)
      .then((res) => { if (!cancelled) setOpenData(res); })
      .catch(() => { if (!cancelled) setOpenData({ items: [], totalPages: 0 }); });
    return () => { cancelled = true; };
  }, [openPage]);

  const fetchResolved = useCallback((page = resolvedPage) => {
    let cancelled = false;
    api.listBreaks(`?status=RESOLVED&page=${page}&size=10`)
      .then((res) => { if (!cancelled) setResolvedData(res); })
      .catch(() => { if (!cancelled) setResolvedData({ items: [], totalPages: 0 }); });
    return () => { cancelled = true; };
  }, [resolvedPage]);

  useEffect(() => fetchOpen(openPage), [openPage, fetchOpen]);
  useEffect(() => fetchResolved(resolvedPage), [resolvedPage, fetchResolved]);

  const toggleSelect = useCallback((id) => {
    setOpenSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  }, []);

  const openResolveModal = useCallback((b) => {
    setModalBreak(b);
    setResolutionNote('');
    setModalError(null);
    setModalOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    setModalOpen(false);
    setModalBreak(null);
    setResolutionNote('');
    setModalError(null);
  }, []);

  const submitResolution = useCallback(async () => {
    if (!modalBreak) return;
    setSubmitting(true);
    setModalError(null);
    try {
      await api.resolveBreak(modalBreak.id, { note: resolutionNote });
      // refresh both tables
      fetchOpen(openPage);
      fetchResolved(resolvedPage);
      closeModal();
    } catch (err) {
      setModalError(err.message);
    } finally {
      setSubmitting(false);
    }
  }, [modalBreak, resolutionNote, fetchOpen, fetchResolved, openPage, resolvedPage, closeModal]);

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
                  <span className="text-slate cursor-pointer" onClick={() => openResolveModal(b)}>{b.tradeId}</span>
                  <span className="text-slate cursor-pointer" onClick={() => openResolveModal(b)}>{b.discrepancyType}</span>
                  <span className="figures text-ink cursor-pointer" onClick={() => openResolveModal(b)}>{formatTimestamp(b.detectedAt)}</span>
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
                  <span className="text-slate">{b.resolvedAt ? formatTimestamp(b.resolvedAt) : '—'}</span>
                  <span className="text-slate break-words italic">{b.resolutionNote || ''}</span>
                </>
              )}
            />
            <DataTable.Pagination page={resolvedPage} totalPages={Math.max(1, resolvedData.totalPages)} onChange={setResolvedPage} />
          </DataTable>
        </div>
      </div>

      {/* Modal overlay for resolving a break */}
      {modalOpen && modalBreak && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/50" onClick={closeModal} />
          <div className="relative z-10 w-full max-w-lg rounded-lg bg-paper p-6 shadow-lg">
            <h3 className="text-lg font-semibold text-ink">Resolve Break {modalBreak.id}</h3>
            <p className="text-sm text-slate mt-2">Trade: {modalBreak.tradeId} - Discrepancy: {modalBreak.discrepancyType}</p>
            <label className="mt-4 grid gap-1 text-sm text-ink">
              Resolution Note
              <textarea
                value={resolutionNote}
                onChange={(e) => setResolutionNote(e.target.value)}
                className="min-h-[6rem] rounded-lg border border-line bg-canvas/40 px-3 py-2 text-sm text-ink focus:border-signal focus:outline-none"
              />
            </label>
            {modalError && <div className="text-sm text-danger mt-2">{modalError}</div>}
            <div className="mt-4 flex items-center justify-end gap-3">
              <button type="button" onClick={closeModal} className="rounded-lg cursor-pointer border border-line px-3 py-1 text-sm">Cancel</button>
              <button
                type="button"
                onClick={submitResolution}
                disabled={submitting}
                className="rounded-lg cursor-pointer bg-signal px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
              >
                {submitting ? 'Submitting…' : 'Resolve'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default withAuth(Review);
