import { NativeModules } from 'react-native';
import type { NotificationRecord, NotificationFilters } from '../types';

interface NotificationListenerModuleInterface {
  hasNotificationPermission(): Promise<boolean>;
  openNotificationSettings(): void;
  getNotifications(filters: NotificationFilters): Promise<NotificationRecord[]>;
  getNotificationById(id: string): Promise<NotificationRecord | null>;
  deleteNotifications(ids: string[]): Promise<boolean>;
  deleteAllNotifications(): Promise<boolean>;
  setRetentionPeriod(days: number): Promise<boolean>;
  getRetentionPeriod(): Promise<number>;
  setExcludedApps(packageNames: string[]): Promise<boolean>;
  getExcludedApps(): Promise<string[]>;
}

const { NotificationListenerModule } = NativeModules;

export default NotificationListenerModule as NotificationListenerModuleInterface;
