// TICKET-ADV072 — Login page exchanging email/password for a JWT.
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@context/AuthContext.jsx';
import { api } from '@services/apiService.js';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('admin@db.com');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState(null);

  async function submit(e) {
    e.preventDefault();

    try {
      const { token, role } = await api.login(email, password);
      login(token, role);
      navigate('/');
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="mx-auto flex max-w-sm flex-col items-center pt-12">
      <h1 className="font-display text-2xl font-semibold text-ink">ReconX</h1>
      <form
        onSubmit={submit}
        className="mt-6 grid w-full gap-4 rounded-xl border border-line bg-paper p-6 shadow-sm"
      >
        <h2 className="font-display text-lg font-semibold text-ink">Sign in</h2>
        <label className="grid gap-1 text-sm text-ink">
          Email
          <input
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            type="email"
            required
            className="rounded-lg border border-line bg-canvas/40 px-3 py-2 text-sm text-ink focus:border-signal focus:outline-none"
          />
        </label>
        <label className="grid gap-1 text-sm text-ink">
          Password
          <input
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            type="password"
            required
            className="rounded-lg border border-line bg-canvas/40 px-3 py-2 text-sm text-ink focus:border-signal focus:outline-none"
          />
        </label>
        {error && <div role="alert" className="text-sm text-danger">{error}</div>}
        <button
          type="submit"
          className="cursor-pointer rounded-lg bg-signal px-4 py-2 text-sm font-medium text-white transition-opacity hover:opacity-90"
        >
          Sign in
        </button>
      </form>
    </div>
  );
}
