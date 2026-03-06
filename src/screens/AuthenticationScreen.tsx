import React, { useEffect } from 'react';
import { View, Text, StyleSheet, ActivityIndicator } from 'react-native';
import BiometricAuthModule from '../modules/BiometricAuthModule';
import { useAuthStore } from '../store/authStore';
import { colors, spacing } from '../theme/colors';

interface Props {
  onAuthenticated: () => void;
}

export const AuthenticationScreen: React.FC<Props> = ({ onAuthenticated }) => {
  useEffect(() => {
    authenticate();
  }, []);

  const authenticate = async () => {
    try {
      const available = await BiometricAuthModule.isBiometricAvailable();
      
      if (available) {
        const result = await BiometricAuthModule.authenticateBiometric({
          title: 'Unlock Notification Logger',
          subtitle: 'Use your fingerprint to access your notifications',
        });
        
        if (result.success) {
          useAuthStore.getState().setAuthenticated(true);
          onAuthenticated();
        }
      }
    } catch (error) {
      console.error('Authentication error:', error);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Notification Logger</Text>
      <ActivityIndicator size="large" color={colors.accent.primary} />
      <Text style={styles.subtitle}>Authenticating...</Text>
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
    fontSize: 32,
    color: colors.text.primary,
    marginBottom: spacing.xl,
    fontWeight: 'bold',
  },
  subtitle: {
    fontSize: 16,
    color: colors.text.secondary,
    marginTop: spacing.lg,
  },
});
