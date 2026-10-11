import React from 'react';

/** Runs an API call on mount and whenever `deps` change. */
export function useApi(fetcher, deps = []) {
  const [state, setState] = React.useState({ data: null, loading: true, error: null });
  const [nonce, setNonce] = React.useState(0);
  const fetcherRef = React.useRef(fetcher);
  fetcherRef.current = fetcher;

  React.useEffect(() => {
    let cancelled = false;
    setState((current) => ({ ...current, loading: true, error: null }));

    Promise.resolve()
      .then(() => fetcherRef.current())
      .then((data) => {
        if (!cancelled) setState({ data, loading: false, error: null });
      })
      .catch((error) => {
        if (!cancelled) setState({ data: null, loading: false, error });
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, nonce]);

  return {
    ...state,
    reload: React.useCallback(() => setNonce((n) => n + 1), []),
    setData: React.useCallback((data) => setState({ data, loading: false, error: null }), []),
  };
}
