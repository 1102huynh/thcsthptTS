import { create } from 'zustand';

// UI-only state (never server data - TanStack Query owns that once it lands
// in a later week). Currently just the AppShell sidebar: the desktop (>= lg)
// sidebar is fixed/always-visible, so this flag only tracks the mobile
// shadcn Sheet drawer's open state - starts closed, toggled by the single
// menu button in Navbar.
export const useUiStore = create((set) => ({
  sidebarOpen: false,
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
  setSidebarOpen: (open) => set({ sidebarOpen: open }),
}));

export default useUiStore;
