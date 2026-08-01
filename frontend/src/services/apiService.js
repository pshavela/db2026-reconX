const BASE = '/api';

function authHeaders() {
  const token = sessionStorage.getItem('reconx-token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(method, path, body, headers = {}) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
      ...headers,
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const detail = await res.text().catch(() => '');
    throw new Error(`HTTP ${res.status}: ${detail}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

async function requestFormData(method, path, formData) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      ...authHeaders(),
    },
    body: formData,
  });
  if (!res.ok) {
    const detail = await res.text().catch(() => '');
    throw new Error(`HTTP ${res.status}: ${detail}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  login: (email, password)   => request('POST', '/auth/login', { email, password }),
  listTrades: (params = '')  => request('GET', `/v1/trades${params}`),
  createTrade: (req)         => request('POST', '/v1/trades', req),
  updateStatus: (id, status) => request('PATCH', `/v1/trades/${id}/status`, { status }),
  deleteTrade: (id)          => request('DELETE', `/v1/trades/${id}`),
  runRecon: (req)            => request('POST', '/v1/recon/run', req),
  runReconCsv: (formData)    => requestFormData('POST', '/v1/recon/run-upload-csv', formData),
  listReconJobs: (params = '') => request('GET', `/v1/recon/jobs${params}`),
  reconResults: (jobId)      => request('GET', `/v1/recon/jobs/${jobId}/results`),
  getTrade: (id)              => request('GET', `/v1/trades/${id}`),
  countTrades: (status)      => request('GET', `/v1/trades/count${status ? `?status=${status}` : ''}`),
  listBreaks: (params = '')  => request('GET', `/v1/recon/breaks${params}`),
  audit: (tradeRef)          => request('GET', `/v1/audit/trades/${tradeRef}`),
};
