// TICKET-ADV119 — <TradeRow>: memoised trade row, only re-renders when the
// fields it actually displays (id, status, price) or onClick change.
import React from 'react';

const STATUS_OPTIONS = ['PENDING', 'MATCHED', 'UNMATCHED', 'DISPUTED', 'CANCELLED'];

// Mirrors the backend's role gate (SecurityConfig): only TRADER/ADMIN may
// PATCH a status, only ADMIN may DELETE — see TradeController.
const CAN_CHANGE_STATUS = new Set(['TRADER', 'ADMIN']);
const CAN_DELETE = new Set(['ADMIN']);

function TradeRowImpl({ trade, onClick, role, onStatusChange, onDelete }) {
  // `display: contents` keeps this wrapper out of the grid layout — DataTable's
  // .data-table__row is `display: grid` over 6 direct children, and a real
  // wrapping element here would break that column alignment (and <tr>/<td>
  // would be invalid HTML outside an actual <table>, which DataTable isn't).
  return (
    <span onClick={() => onClick(trade.id)} style={{ display: 'contents' }}>
      <span>{trade.tradeRef}</span>
      <span>{trade.instrumentSymbol}</span>
      <span>{trade.quantity}</span>
      <span>{trade.price}</span>
      <span className={`status-pill ${trade.status.toLowerCase()}`}>{trade.status}</span>
      <span className="trade-row__actions" onClick={(e) => e.stopPropagation()}>
        {CAN_CHANGE_STATUS.has(role) && (
          <select
            aria-label={`Change status for ${trade.tradeRef}`}
            value={trade.status}
            onChange={(e) => onStatusChange(trade.id, e.target.value)}
          >
            {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        )}
        {CAN_DELETE.has(role) && (
          <button
            type="button"
            className="trade-row__delete"
            onClick={() => onDelete(trade.id)}
          >
            Delete
          </button>
        )}
      </span>
    </span>
  );
}

// Custom equality — only the fields we actually render, not the whole trade object.
function areEqual(prev, next) {
  return prev.trade.id === next.trade.id
      && prev.trade.status === next.trade.status
      && prev.trade.price === next.trade.price
      && prev.onClick === next.onClick
      && prev.role === next.role
      && prev.onStatusChange === next.onStatusChange
      && prev.onDelete === next.onDelete;
}

export const TradeRow = React.memo(TradeRowImpl, areEqual);
