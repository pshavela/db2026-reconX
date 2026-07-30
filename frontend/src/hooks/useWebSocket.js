// TICKET-ADV115 — useWebSocket(url) with auto-reconnect (exp backoff up to 5 tries).
import { useCallback, useEffect, useRef, useState } from 'react';

export function useWebSocket(url, { reconnect = true, maxRetries = 5 } = {}) {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('connecting');
  const wsRef = useRef(null);
  const retriesRef = useRef(0);
  const timerRef = useRef(null);
  const stoppedRef = useRef(false);

  const connect = useCallback(() => {
    const ws = new WebSocket(url);
    wsRef.current = ws;

    ws.onopen = () => {
      if (stoppedRef.current) return;
      setStatus('open');
      retriesRef.current = 0;
    };
    ws.onmessage = (e) => {
      if (stoppedRef.current) return;
      try {
        setData(JSON.parse(e.data));
      } catch {
        setData(e.data);
      }
    };
    ws.onerror = () => {
      if (stoppedRef.current) return;
      setStatus('error');
    };
    ws.onclose = () => {
      if (stoppedRef.current) return;
      setStatus('closed');
      if (reconnect && retriesRef.current < maxRetries) {
        const delay = Math.min(30000, 500 * 2 ** retriesRef.current);
        retriesRef.current += 1;
        timerRef.current = setTimeout(connect, delay);
      }
    };
  }, [url, reconnect, maxRetries]);

  useEffect(() => {
    stoppedRef.current = false;
    connect();
    return () => {
      stoppedRef.current = true;
      clearTimeout(timerRef.current);
      wsRef.current?.close();
    };
  }, [connect]);

  const send = useCallback((payload) => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(typeof payload === 'string' ? payload : JSON.stringify(payload));
    }
  }, []);

  return { data, status, send };
}
