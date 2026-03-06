import { create } from 'zustand';

interface AuthState {
  isAuthenticated: boolean;
  lastActivityTime: number;
  setAuthenticated: (value: boolean) => void;
  updateActivity: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  isAuthenticated: false,
  lastActivityTime: Date.now(),
  setAuthenticated: (value) => set({ isAuthenticated: value }),
  updateActivity: () => set({ lastActivityTime: Date.now() }),
}));
