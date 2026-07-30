// TICKET-ADV122 — placeholder shown by <Suspense> while a route chunk loads.
// A skeleton (not a spinner, not null) so the layout doesn't flash to blank.
import React from 'react';

export function PageSkeleton() {
  return (
    <div className="page-skeleton" role="status" aria-label="Loading page">
      <div className="page-skeleton__bar page-skeleton__bar--title" />
      <div className="page-skeleton__bar" />
      <div className="page-skeleton__bar" />
      <div className="page-skeleton__bar page-skeleton__bar--short" />
    </div>
  );
}
