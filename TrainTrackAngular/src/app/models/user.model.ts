export interface User {
  userId?: number;
  id?: number;
  email: string;
  passwordHash?: string;
  password?: string;
  firstName: string;
  lastName: string;
  bio?: string;
  profileImageUrl?: string;
  emailVerified?: boolean;
  subscriptionTier?: 'FREE' | 'PREMIUM' | 'ENTERPRISE';
  notificationPreferences?: boolean;
  createdAt?: Date;
}