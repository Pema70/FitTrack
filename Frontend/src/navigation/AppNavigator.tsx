import React from 'react';
import { useAuthStore } from '../store/authStore';
import AuthStack from './AuthStack';
import MainTabs from './MainTabs';

export default function AppNavigator() {
  const token = useAuthStore((s: any) => s.token);
  return token ? <MainTabs /> : <AuthStack />;
}