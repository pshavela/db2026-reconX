import React from 'react';
import { statusToneClasses } from '@lib/status.js';

// The recurring visual motif for this app: every status — a trade's match
// state, a recon job's run state — renders as the same "ledger stamp":
// a small dot plus an uppercase monospace label, colored by outcome.
export function StatusPill({ status }) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 font-mono text-xs font-medium uppercase tracking-wide ${statusToneClasses(status)}`}
    >
      <span className="h-1.5 w-1.5 rounded-full bg-current" aria-hidden="true" />
      {status}
    </span>
  );
}
