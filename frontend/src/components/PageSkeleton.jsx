// TICKET-ADV122 — placeholder shown by <Suspense> while a route chunk loads.
// A skeleton (not a spinner, not null) so the layout doesn't flash to blank.
import React from 'react';

export function PageSkeleton() {
  return (
    <div className="grid gap-3 py-2" role="status" aria-label="Loading page">
      <div className="h-6 w-2/5 animate-pulse rounded-md bg-line" />
      <div className="h-4 w-full animate-pulse rounded-md bg-line" />
      <div className="h-4 w-full animate-pulse rounded-md bg-line" />
      <div className="h-4 w-3/5 animate-pulse rounded-md bg-line" />
    </div>
  );
}
