export interface User {
  userId?: number;
  email: string;
  passwordHash?: string;
  password?: string;
  firstName: string;
  lastName: string;
  subscriptionTier?: 'FREE' | 'PREMIUM' | 'ENTERPRISE';
  notificationPreferences?: boolean;
  createdAt?: Date;
}
