
export function formatTimestamp(value) {
  if (!value) return '-';

  const text = String(value).trim();
  const match = text.match(/^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})(?::\d{2}(?:\.\d+)?)?(Z)?$/);

  if (!match) return text;
  return `${match[1]} ${match[2]}`;
}
