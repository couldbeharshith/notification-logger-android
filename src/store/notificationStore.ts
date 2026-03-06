import { create } from 'zustand';
import type { NotificationRecord, NotificationFilters } from '../types';

interface NotificationState {
  notifications: NotificationRecord[];
  filters: NotificationFilters;
  searchQuery: string;
  setNotifications: (notifications: NotificationRecord[]) => void;
  addNotification: (notification: NotificationRecord) => void;
  setFilters: (filters: NotificationFilters) => void;
  setSearchQuery: (query: string) => void;
  clearFilters: () => void;
}

export const useNotificationStore = create<NotificationState>((set) => ({
  notifications: [],
  filters: {
    sortBy: 'timestamp',
    sortOrder: 'desc',
    limit: 100,
    offset: 0,
  },
  searchQuery: '',
  setNotifications: (notifications) => set({ notifications }),
  addNotification: (notification) =>
    set((state) => ({ notifications: [notification, ...state.notifications] })),
  setFilters: (filters) => set({ filters }),
  setSearchQuery: (query) => set({ searchQuery: query }),
  clearFilters: () =>
    set({
      filters: {
        sortBy: 'timestamp',
        sortOrder: 'desc',
        limit: 100,
        offset: 0,
      },
      searchQuery: '',
    }),
}));
