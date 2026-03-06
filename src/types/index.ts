export interface NotificationRecord {
  id: string;
  timestamp: number;
  packageName: string;
  appName: string;
  title: string | null;
  text: string | null;
  subText: string | null;
  bigText: string | null;
  infoText: string | null;
  summaryText: string | null;
  tickerText: string | null;
  notificationId: number;
  tag: string | null;
  channelId: string | null;
  groupKey: string | null;
  sortKey: string | null;
  color: number | null;
  smallIcon: string | null;
  largeIcon: string | null;
  priority: number;
  category: string | null;
  visibility: number;
  isOngoing: boolean;
  isGroupSummary: boolean;
  isClearable: boolean;
}

export interface NotificationFilters {
  packageNames?: string[];
  startDate?: number;
  endDate?: number;
  searchQuery?: string;
  priorities?: number[];
  sortBy?: 'timestamp' | 'appName' | 'priority';
  sortOrder?: 'asc' | 'desc';
  limit?: number;
  offset?: number;
}

export interface BiometricOptions {
  title: string;
  subtitle: string;
}

export interface AuthResult {
  success: boolean;
  error?: string;
}
