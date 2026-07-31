import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ReconJobs from './ReconJobs.jsx';

const { listReconJobs, runReconCsv } = vi.hoisted(() => ({
  listReconJobs: vi.fn(),
  runReconCsv: vi.fn(),
}));

vi.mock('@components/withAuth.jsx', () => ({
  withAuth: (Component) => Component,
}));

vi.mock('@services/apiService.js', () => ({
  api: {
    listReconJobs,
    runReconCsv,
  },
}));

vi.mock('@components/DataTable.jsx', () => ({
  default: Object.assign(
    ({ children }) => <div data-testid="datatable">{children}</div>,
    {
      Header: () => null,
      Body: ({ children }) => <div>{children}</div>,
      Pagination: () => null,
    }
  ),
}));

vi.mock('@components/StatusPill.jsx', () => ({
  StatusPill: ({ status }) => <span>{status}</span>,
}));

describe('<ReconJobs />', () => {
  beforeEach(() => {
    listReconJobs.mockReset();
    runReconCsv.mockReset();
    listReconJobs.mockResolvedValue({ items: [], totalPages: 0 });
    runReconCsv.mockResolvedValue({});
  });

  it('shows a success message after a recon job is submitted', async () => {
    render(<ReconJobs />);

    fireEvent.change(screen.getByLabelText(/from date/i), {
      target: { value: '2026-07-01' },
    });
    fireEvent.change(screen.getByLabelText(/to date/i), {
      target: { value: '2026-07-31' },
    });
    fireEvent.change(screen.getByLabelText(/external trades csv/i), {
      target: { files: [new File(['a,b,c\n1,2,3\n'], 'trades.csv', { type: 'text/csv' })] },
    });

    fireEvent.submit(screen.getByRole('button', { name: /run job/i }).closest('form'));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(/queued successfully/i);
    });
  });
});
