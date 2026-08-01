// TICKET-ADV122 — Lazy + Suspense for route-based code splitting
import React, { Suspense, lazy } from 'react';
import { Routes, Route, NavLink, Navigate, useLocation } from 'react-router-dom';
import { withErrorBoundary } from '@components/withErrorBoundary.jsx';
import { PageSkeleton } from '@components/PageSkeleton.jsx';
import { useTheme } from '@context/ThemeContext.jsx';
import { useAuth } from '@context/AuthContext.jsx';

const Dashboard = lazy(() => import('@pages/Dashboard.jsx'));
const Trades    = lazy(() => import('@pages/Trades.jsx'));
const Trade     = lazy(() => import('@pages/Trade.jsx'));
const AddTrade  = lazy(() => import('@pages/AddTrade.jsx'));
const ReconJobs = lazy(() => import('@pages/ReconJobs.jsx'));
const Login     = lazy(() => import('@pages/Login.jsx'));

const navLinkClass = ({ isActive }) =>
  `border-b-2 px-1 py-2 text-sm font-medium transition-colors ${
    isActive
      ? 'border-signal text-white'
      : 'border-transparent text-white/70 hover:text-white'
  }`;

const sideLinkClass = ({ isActive }) =>
  `block rounded-md px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-white/10 text-white' : 'text-white/70 hover:bg-white/5 hover:text-white'
  }`;

function App() {
  const { theme, toggle } = useTheme();
  const { user } = useAuth();
  const username = user?.username || (user?.role ? user.role : 'Guest');
  const location = useLocation();

  // Minimal layout for login route only (no sidebar/header)
  if (location.pathname && location.pathname.startsWith('/login')) {
    return (
      <div className="min-h-screen bg-canvas text-ink">
        <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
          <Suspense fallback={<PageSkeleton />}>
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </Suspense>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-canvas text-ink flex">
      <aside className="w-56 bg-navy text-white flex-shrink-0">
        <div className="px-4 py-6">
          <div className="flex flex-row items-center">
            <h1 className="font-display text-xl font-semibold text-ink">Recon</h1>
            <img src="/tiger_icon.png" alt="image" className="h-8 w-8" />
          </div>
          <nav className="mt-6 flex flex-col gap-2">
            <NavLink to="/" end className={sideLinkClass}>Dashboard</NavLink>
            <NavLink to="/trades" end className={sideLinkClass}>Trades</NavLink>
            <NavLink to="/recon-jobs" className={sideLinkClass}>Recon jobs</NavLink>
            <NavLink to="/trades/new" className={sideLinkClass}>Add trade</NavLink>
          </nav>
        </div>
      </aside>

      <div className="flex-1">
        <header className="bg-transparent">
          <div className="mx-auto flex max-w-7xl items-center justify-end gap-4 px-4 py-4 sm:px-6 lg:px-8">
            <div className="flex items-center gap-3">
              <button
                type="button"
                aria-label="User"
                className="flex items-center gap-2 rounded-lg border border-white/10 bg-paper px-3 py-1 text-sm text-ink"
              >
                <span className="text-lg">👤</span>
                <span className="hidden sm:inline">{username}</span>
              </button>
              <button
                type="button"
                onClick={toggle}
                aria-label="Toggle theme"
                className="rounded-lg cursor-pointer border border-gray-200 px-3 py-1.5 text-sm text-ink/90 transition-colors hover:border-white/30 hover:text-ink"
              >
                {theme === 'dark' ? '☀️ Light' : '🌙 Dark'}
              </button>
            </div>
          </div>
        </header>

        <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
          <Suspense fallback={<PageSkeleton />}>
            <Routes>
              <Route path="/login"      element={<Login />} />
              <Route path="/"           element={<Dashboard />} />
              <Route path="/trades"     element={<Trades />} />
              <Route path="/trades/:id" element={<Trade />} />
              <Route path="/trades/new" element={<AddTrade />} />
              <Route path="/recon-jobs" element={<ReconJobs />} />
              <Route path="*"           element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </main>
      </div>
    </div>
  );
}

export default withErrorBoundary(App);
