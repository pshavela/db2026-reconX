// TICKET-ADV122 — Lazy + Suspense for route-based code splitting
import React, { Suspense, lazy } from 'react';
import { Routes, Route, NavLink, Navigate } from 'react-router-dom';
import { withErrorBoundary } from '@components/withErrorBoundary.jsx';
import { PageSkeleton } from '@components/PageSkeleton.jsx';
import { useTheme } from '@context/ThemeContext.jsx';

const Dashboard = lazy(() => import('@pages/Dashboard.jsx'));
const Trades    = lazy(() => import('@pages/Trades.jsx'));
const AddTrade  = lazy(() => import('@pages/AddTrade.jsx'));
const ReconJobs = lazy(() => import('@pages/ReconJobs.jsx'));
const Login     = lazy(() => import('@pages/Login.jsx'));

const navLinkClass = ({ isActive }) =>
  `border-b-2 px-1 py-2 text-sm font-medium transition-colors ${
    isActive
      ? 'border-signal text-white'
      : 'border-transparent text-white/70 hover:text-white'
  }`;

function App() {
  const { theme, toggle } = useTheme();
  return (
    <div className="min-h-screen bg-canvas text-ink">
      <header className="bg-navy">
        <div className="mx-auto flex max-w-6xl items-center gap-8 px-6 py-4">
          <h1 className="font-display text-xl font-semibold text-white">ReconX</h1>
          <nav className="flex flex-1 items-center gap-6">
            <NavLink to="/" end className={navLinkClass}>Dashboard</NavLink>
            <NavLink to="/trades" className={navLinkClass}>Trades</NavLink>
            <NavLink to="/recon-jobs" className={navLinkClass}>Recon jobs</NavLink>
            <NavLink to="/trades/new" className={navLinkClass}>Add trade</NavLink>
          </nav>
          <button
            type="button"
            onClick={toggle}
            aria-label="Toggle theme"
            className="rounded-lg border border-white/15 px-3 py-1.5 text-sm text-white/80 transition-colors hover:border-white/30 hover:text-white"
          >
            {theme === 'dark' ? '☀️ Light' : '🌙 Dark'}
          </button>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-8">
        <Suspense fallback={<PageSkeleton />}>
          <Routes>
            <Route path="/login"      element={<Login />} />
            <Route path="/"           element={<Dashboard />} />
            <Route path="/trades"     element={<Trades />} />
            <Route path="/trades/new" element={<AddTrade />} />
            <Route path="/recon-jobs" element={<ReconJobs />} />
            <Route path="*"           element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </main>
    </div>
  );
}

export default withErrorBoundary(App);
