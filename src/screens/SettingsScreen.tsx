import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import NotificationListenerModule from '../modules/NotificationListenerModule';
import { colors, spacing, borderRadius } from '../theme/colors';

export const SettingsScreen: React.FC = () => {
  const [retentionPeriod, setRetentionPeriod] = useState(10);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      const period = await NotificationListenerModule.getRetentionPeriod();
      setRetentionPeriod(period);
    } catch (error) {
      console.error('Error loading settings:', error);
    }
  };

  const updateRetentionPeriod = async (days: number) => {
    try {
      await NotificationListenerModule.setRetentionPeriod(days);
      setRetentionPeriod(days);
    } catch (error) {
      console.error('Error updating retention period:', error);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.header}>Settings</Text>
      
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Data Retention</Text>
        <Text style={styles.sectionDescription}>
          Notifications older than {retentionPeriod} days will be automatically deleted
        </Text>
        
        <View style={styles.optionsContainer}>
          {[7, 10, 30, 90, 365].map((days) => (
            <TouchableOpacity
              key={days}
              style={[
                styles.option,
                retentionPeriod === days && styles.optionActive,
              ]}
              onPress={() => updateRetentionPeriod(days)}
            >
              <Text
                style={[
                  styles.optionText,
                  retentionPeriod === days && styles.optionTextActive,
                ]}
              >
                {days} days
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>About</Text>
        <Text style={styles.aboutText}>
          Notification Logger v1.0.0{'\n'}
          Secure notification capture with encryption
        </Text>
      </View>
    </ScrollView>
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
  section: {
    padding: spacing.lg,
    marginBottom: spacing.lg,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.text.primary,
    marginBottom: spacing.sm,
  },
  sectionDescription: {
    fontSize: 14,
    color: colors.text.secondary,
    marginBottom: spacing.md,
    lineHeight: 20,
  },
  optionsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  option: {
    backgroundColor: colors.background.tertiary,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: borderRadius.pill,
    borderWidth: 1,
    borderColor: colors.glass.border,
  },
  optionActive: {
    backgroundColor: colors.accent.primary,
    borderColor: colors.accent.primary,
  },
  optionText: {
    fontSize: 14,
    color: colors.text.secondary,
  },
  optionTextActive: {
    color: colors.text.primary,
    fontWeight: '600',
  },
  aboutText: {
    fontSize: 14,
    color: colors.text.tertiary,
    lineHeight: 22,
  },
});
