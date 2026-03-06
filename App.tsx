import React, { useEffect, useState } from 'react';
import { SafeAreaView, StatusBar, StyleSheet } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { AuthenticationScreen } from './src/screens/AuthenticationScreen';
import { PermissionScreen } from './src/screens/PermissionScreen';
import { MainNavigator } from './src/navigation/MainNavigator';
import NotificationListenerModule from './src/modules/NotificationListenerModule';
import BiometricAuthModule from './src/modules/BiometricAuthModule';
import { useAuthStore } from './src/store/authStore';
import { colors } from './src/theme/colors';

type AppState = 'loading' | 'auth' | 'permission' | 'main';

function App(): React.JSX.Element {
  const [appState, setAppState] = useState<AppState>('loading');
  const { isAuthenticated } = useAuthStore();

  useEffect(() => {
    checkInitialState();
  }, []);

  const checkInitialState = async () => {
    try {
      // Check if already authenticated
      const authenticated = await BiometricAuthModule.isAuthenticated();
      if (authenticated) {
        useAuthStore.getState().setAuthenticated(true);
        await checkPermission();
      } else {
        setAppState('auth');
      }
    } catch (error) {
      console.error('Error checking initial state:', error);
      setAppState('auth');
    }
  };

  const checkPermission = async () => {
    try {
      const hasPermission = await NotificationListenerModule.hasNotificationPermission();
      setAppState(hasPermission ? 'main' : 'permission');
    } catch (error) {
      console.error('Error checking permission:', error);
      setAppState('permission');
    }
  };

  const handleAuthenticated = async () => {
    await checkPermission();
  };

  const handlePermissionGranted = async () => {
    const hasPermission = await NotificationListenerModule.hasNotificationPermission();
    if (hasPermission) {
      setAppState('main');
    }
  };

  const renderScreen = () => {
    switch (appState) {
      case 'auth':
        return <AuthenticationScreen onAuthenticated={handleAuthenticated} />;
      case 'permission':
        return <PermissionScreen onPermissionGranted={handlePermissionGranted} />;
      case 'main':
        return (
          <NavigationContainer>
            <MainNavigator />
          </NavigationContainer>
        );
      default:
        return null;
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={colors.background.primary} />
      {renderScreen()}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background.primary,
  },
});

export default App;
