// TICKET-ADV119 — <TradeRow>: memoised trade row, only re-renders when the
// fields it actually displays (id, status, price) or onClick change.
import React from 'react';
import { StatusPill } from '@components/StatusPill.jsx';

function TradeRowImpl({ trade, onClick }) {
  // `contents` (Tailwind's display: contents) keeps this wrapper out of the
  // grid layout — DataTable's row is `display: grid` over direct children,
  // and a real wrapping element here would break that column alignment.
  return (
    <span
      onClick={() => onClick(trade.id)}
      className="contents cursor-pointer"
    >
      <span className="text-ink">{trade.tradeRef}</span>
      <span className="text-slate">{trade.instrumentSymbol}</span>
      <span className="figures text-ink">{trade.quantity}</span>
      <span className="figures text-ink">{trade.price}</span>
      <StatusPill status={trade.status} />
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
