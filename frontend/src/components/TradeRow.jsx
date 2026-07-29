// TICKET-ADV119 — <TradeRow>: memoised trade row, only re-renders when the
// fields it actually displays (id, status, price) or onClick change.
import React from 'react';

function TradeRowImpl({ trade, onClick }) {
  // `display: contents` keeps this wrapper out of the grid layout — DataTable's
  // .data-table__row is `display: grid` over 5 direct children, and a real
  // wrapping element here would break that column alignment (and <tr>/<td>
  // would be invalid HTML outside an actual <table>, which DataTable isn't).
  return (
    <span onClick={() => onClick(trade.id)} style={{ display: 'contents' }}>
      <span>{trade.tradeRef}</span>
      <span>{trade.instrumentSymbol}</span>
      <span>{trade.quantity}</span>
      <span>{trade.price}</span>
      <span className={`status-pill ${trade.status.toLowerCase()}`}>{trade.status}</span>
    </span>
  );
}

// Custom equality — only the fields we actually render, not the whole trade object.
function areEqual(prev, next) {
  return prev.trade.id === next.trade.id
      && prev.trade.status === next.trade.status
      && prev.trade.price === next.trade.price
      && prev.onClick === next.onClick;
}

export const TradeRow = React.memo(TradeRowImpl, areEqual);
