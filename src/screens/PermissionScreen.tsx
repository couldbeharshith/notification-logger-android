import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import NotificationListenerModule from '../modules/NotificationListenerModule';
import { colors, spacing, borderRadius } from '../theme/colors';

interface Props {
  onPermissionGranted: () => void;
}

export const PermissionScreen: React.FC<Props> = ({ onPermissionGranted }) => {
  const openSettings = () => {
    NotificationListenerModule.openNotificationSettings();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Notification Access Required</Text>
      <Text style={styles.description}>
        To capture and log notifications, this app needs notification access permission.
      </Text>
      <Text style={styles.instructions}>
        1. Tap the button below{'\n'}
        2. Find "Notification Logger" in the list{'\n'}
        3. Enable the toggle{'\n'}
        4. Return to this app
      </Text>
      <TouchableOpacity style={styles.button} onPress={openSettings}>
        <Text style={styles.buttonText}>Open Settings</Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background.primary,
    justifyContent: 'center',
    alignItems: 'center',
    padding: spacing.xl,
  },
  title: {
    fontSize: 28,
    color: colors.text.primary,
    marginBottom: spacing.lg,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  description: {
    fontSize: 16,
    color: colors.text.secondary,
    marginBottom: spacing.xl,
    textAlign: 'center',
    lineHeight: 24,
  },
  instructions: {
    fontSize: 14,
    color: colors.text.tertiary,
    marginBottom: spacing.xxl,
    textAlign: 'left',
    lineHeight: 22,
  },
  button: {
    backgroundColor: colors.accent.primary,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
    borderRadius: borderRadius.pill,
  },
  buttonText: {
    color: colors.text.primary,
    fontSize: 16,
    fontWeight: '600',
  },
});
