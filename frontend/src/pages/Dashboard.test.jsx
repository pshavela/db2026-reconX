// TICKET-ADV125 — RTL test: dashboard summary cards, no live backend.
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@context/ThemeContext.jsx';
import { AuthProvider } from '@context/AuthContext.jsx';
import Dashboard from './Dashboard.jsx';

// Dashboard reads live trades via useTradeStream() (SSE), not a prop — mock it
// so the test is synchronous and never opens a real EventSource.
const trades = [
  { id: 1, tradeRef: 'TRD-2026-0001', instrumentSymbol: 'SAP.DE', quantity: 100, price: 250, status: 'MATCHED' },
  { id: 2, tradeRef: 'TRD-2026-0002', instrumentSymbol: 'SAP.DE', quantity: 50, price: 251, status: 'UNMATCHED' },
];
vi.mock('@hooks/useTradeStream.js', () => ({
  useTradeStream: () => ({ trades, isConnected: true }),
}));

// Dashboard's default export is withAuth(Dashboard) — without a logged-in
// user, withAuth redirects to /login instead of rendering the page at all.
function renderWithProviders(ui) {
  sessionStorage.setItem('reconx-token', 'fake.jwt.token');
  sessionStorage.setItem('reconx-role', 'TRADER');
  return render(
    <AuthProvider>
      <ThemeProvider>
        <MemoryRouter>{ui}</MemoryRouter>
      </ThemeProvider>
    </AuthProvider>
  );
}

describe('<Dashboard />', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('shows summary cards', () => {
    renderWithProviders(<Dashboard />);

    expect(screen.getByRole('heading', { name: /portfolio value/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /trades streamed/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /matched/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /open breaks/i })).toBeInTheDocument();
    // 100 * 250 + 50 * 251 = 37550
    expect(screen.getByText(/37,550/)).toBeInTheDocument();
  });
});
