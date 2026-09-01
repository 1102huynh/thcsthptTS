import { QueryClient } from '@tanstack/react-query';

// Single shared QueryClient instance, per Tuần 2 Ngày 5. Defaults tuned for
// an internal admin app over a LAN/local backend rather than a public API:
// data doesn't go stale from other users' edits within a few seconds of
// looking at it, so a short staleTime avoids refetching on every tab focus,
// and one retry (not TanStack's default 3) keeps a genuinely-down backend
// from making every screen hang for several seconds before showing an error.
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export default queryClient;
