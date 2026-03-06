import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, RefreshControl } from 'react-native';
import NotificationListenerModule from '../modules/NotificationListenerModule';
import { useNotificationStore } from '../store/notificationStore';
import { colors, spacing, borderRadius } from '../theme/colors';
import type { NotificationRecord } from '../types';

export const NotificationListScreen: React.FC = () => {
  const { notifications, setNotifications, filters } = useNotificationStore();
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    loadNotifications();
  }, [filters]);

  const loadNotifications = async () => {
    try {
      const data = await NotificationListenerModule.getNotifications(filters);
      setNotifications(data);
    } catch (error) {
      console.error('Error loading notifications:', error);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadNotifications();
    setRefreshing(false);
  };

  const renderNotificationCard = ({ item }: { item: NotificationRecord }) => {
    const priorityColor = 
      item.priority >= 1 ? colors.priority.high :
      item.priority === 0 ? colors.priority.normal :
      colors.priority.low;

    return (
      <TouchableOpacity style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.appName}>{item.appName}</Text>
          <Text style={styles.timestamp}>
            {new Date(item.timestamp).toLocaleTimeString()}
          </Text>
        </View>
        <View style={[styles.priorityIndicator, { backgroundColor: priorityColor }]} />
        {item.title && <Text style={styles.title}>{item.title}</Text>}
        {item.text && <Text style={styles.text} numberOfLines={2}>{item.text}</Text>}
      </TouchableOpacity>
    );
  };

  const renderEmpty = () => (
    <View style={styles.emptyContainer}>
      <Text style={styles.emptyText}>No notifications yet</Text>
      <Text style={styles.emptySubtext}>
        Notifications will appear here as they arrive
      </Text>
    </View>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Notifications</Text>
      <FlatList
        data={notifications}
        renderItem={renderNotificationCard}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={renderEmpty}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={colors.accent.primary}
          />
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background.primary,
  },
  header: {
    fontSize: 32,
    fontWeight: 'bold',
    color: colors.text.primary,
    padding: spacing.lg,
    paddingTop: spacing.xxl,
  },
  listContent: {
    padding: spacing.md,
  },
  card: {
    backgroundColor: colors.background.tertiary,
    borderRadius: borderRadius.lg,
    padding: spacing.md,
    marginBottom: spacing.md,
    borderWidth: 1,
    borderColor: colors.glass.border,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  appName: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.text.secondary,
  },
  timestamp: {
    fontSize: 12,
    color: colors.text.tertiary,
  },
  priorityIndicator: {
    position: 'absolute',
    right: 0,
    top: 0,
    width: 4,
    height: '100%',
    borderTopRightRadius: borderRadius.lg,
    borderBottomRightRadius: borderRadius.lg,
  },
  title: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.text.primary,
    marginBottom: spacing.xs,
  },
  text: {
    fontSize: 14,
    color: colors.text.secondary,
    lineHeight: 20,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: spacing.xxl * 2,
  },
  emptyText: {
    fontSize: 20,
    fontWeight: '600',
    color: colors.text.secondary,
    marginBottom: spacing.sm,
  },
  emptySubtext: {
    fontSize: 14,
    color: colors.text.tertiary,
    textAlign: 'center',
  },
});
