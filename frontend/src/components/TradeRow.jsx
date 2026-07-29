// TICKET-ADV119 — <TradeRow>: memoised trade row, only re-renders when the
// fields it actually displays (id, status, price) or onClick change.
import React from 'react';

function TradeRowImpl({ trade, onClick }) {
  return (
    <tr onClick={() => onClick(trade.id)}>
      <td>{trade.tradeRef}</td>
      <td>{trade.instrumentSymbol}</td>
      <td>{trade.quantity}</td>
      <td>{trade.price}</td>
      <td>
        <span className={`status-pill ${trade.status.toLowerCase()}`}>{trade.status}</span>
      </td>
    </tr>
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
