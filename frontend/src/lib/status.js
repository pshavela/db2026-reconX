// Shared status → badge-style mapping, used anywhere a status string from
// the backend (trade status, recon job status) needs a colored pill.
// Matches by keyword rather than an exact enum, since trade statuses
// (MATCHED/UNMATCHED/DISPUTED/PENDING) and recon job statuses
// (RUNNING/COMPLETED/FAILED/...) come from different backend enums.
const TONES = {
  success: 'bg-success-bg text-success',
  danger: 'bg-danger-bg text-danger',
  warning: 'bg-warning-bg text-warning',
  neutral: 'bg-neutral-bg text-neutral',
};

function toneFor(status) {
  const s = String(status || '').toUpperCase();
  if (/(MATCH|COMPLETE|SUCCESS|DONE)/.test(s) && !/UNMATCH/.test(s)) return 'success';
  if (/(BREAK|DISPUTED|MISSING|FAILED)/.test(s)) return 'danger';
  if (/(PENDING|RUNNING|PROCESSING|QUEUED)/.test(s)) return 'warning';
  return 'neutral';
}

/** Tailwind classes for the colored pill background/text. */
export function statusToneClasses(status) {
  return TONES[toneFor(status)];
}
