// TICKET-ADV114 — Compound <DataTable> with Header / Body / Pagination subcomponents.
// Header and each row are independent CSS grids that must share the same
// column count to stay aligned — that count now comes from the `columns`
// prop on <DataTable> itself (previously hardcoded to 5, which silently
// misaligned the 6-column recon-jobs table).
import React, { createContext, useContext } from 'react';

const DataTableContext = createContext({ sort: null, page: 0, size: 20, columns: [] });

function gridStyle(columns) {
  const templates = columns.map((column) => column.width || 'minmax(0, 1fr)');
  return { gridTemplateColumns: templates.join(' ') };
}

export default function DataTable({ children, columns = [], sort, page = 0, size = 20, onSortChange }) {
  return (
    <DataTableContext.Provider value={{ sort, page, size, onSortChange, columns }}>
      <div className="w-full overflow-x-auto rounded-xl border border-line bg-paper">{children}</div>
    </DataTableContext.Provider>
  );
}

DataTable.Header = function Header({ columns }) {
  const { sort, onSortChange } = useContext(DataTableContext);
  return (
    <div className="grid items-center gap-3 bg-canvas/60" style={gridStyle(columns)} role="row">
      {columns.map((c) => (
        <button
          key={c.key}
          type="button"
          onClick={() => onSortChange && onSortChange(c.key)}
          className={`cursor-pointer px-3 py-2 text-left text-xs font-medium uppercase tracking-wide transition-colors ${
            sort === c.key ? 'text-ink' : 'text-slate hover:text-ink'
          }`}
        >
          {c.label}
        </button>
      ))}
    </div>
  );
};

DataTable.Body = function Body({ rows, render }) {
  const { columns } = useContext(DataTableContext);
  return (
    <div>
      {rows.map((row, i) => (
        <div
          key={row.id ?? row.jobId ?? i}
          role="row"
          className="grid items-center gap-3 border-t border-line px-3 py-2 text-sm transition-colors hover:bg-canvas/60"
          style={gridStyle(columns)}
        >
          {render(row)}
        </div>
      ))}
    </div>
  );
};

DataTable.Pagination = function Pagination({ page, totalPages, onChange }) {
  return (
    <nav aria-label="Pagination" className="flex items-center justify-center gap-3 border-t border-line p-2 text-sm">
      <button
        type="button"
        disabled={page === 0}
        onClick={() => onChange(page - 1)}
        className="cursor-pointer rounded-md px-2 py-1 text-slate transition-colors hover:bg-canvas hover:text-ink disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent"
      >
        ‹
      </button>
      <span className="figures text-slate">{page + 1} / {totalPages}</span>
      <button
        type="button"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
        className="cursor-pointer rounded-md px-2 py-1 text-slate transition-colors hover:bg-canvas hover:text-ink disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent"
      >
        ›
      </button>
    </nav>
  );
};
