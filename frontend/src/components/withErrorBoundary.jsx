// TICKET-ADV113 — withErrorBoundary HOC: wraps a component in an error boundary.
import React from 'react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error) {
    return { error };
  }

  componentDidCatch(error, info) {
    // In real prod we'd ship this to Sentry / a browser-side logger.
    // eslint-disable-next-line no-console
    console.error('ErrorBoundary caught', error, info);
  }

  render() {
    if (this.state.error) {
      return (
        <div role="alert" className="rounded-xl border-l-4 border-l-danger border-line bg-paper p-6 shadow-sm">
          <h2 className="font-display text-lg font-semibold text-ink">Something went wrong</h2>
          <pre className="mt-2 overflow-x-auto text-sm text-slate">{String(this.state.error.message || this.state.error)}</pre>
          <button
            onClick={() => this.setState({ error: null })}
            className="mt-4 cursor-pointer rounded-lg bg-signal px-4 py-2 text-sm font-medium text-white transition-opacity hover:opacity-90"
          >
            Try again
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

export function withErrorBoundary(Component) {
  function WithErrorBoundary(props) {
    return (
      <ErrorBoundary>
        <Component {...props} />
      </ErrorBoundary>
    );
  }
  WithErrorBoundary.displayName = `withErrorBoundary(${Component.displayName || Component.name || 'Component'})`;
  return WithErrorBoundary;
}
