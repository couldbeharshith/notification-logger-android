import { NativeModules } from 'react-native';
import type { BiometricOptions, AuthResult } from '../types';

interface BiometricAuthModuleInterface {
  isBiometricAvailable(): Promise<boolean>;
  isBiometricEnrolled(): Promise<boolean>;
  authenticateBiometric(options: BiometricOptions): Promise<AuthResult>;
  isAuthenticated(): Promise<boolean>;
  lockApp(): void;
  setAutoLockTimeout(minutes: number): Promise<boolean>;
  getAutoLockTimeout(): Promise<number>;
}

const { BiometricAuthModule } = NativeModules;

export default BiometricAuthModule as BiometricAuthModuleInterface;
