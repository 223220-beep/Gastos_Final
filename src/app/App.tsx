import React, { useState, useEffect } from 'react';
import { createClient } from '@supabase/supabase-js';
import { projectId, publicAnonKey } from '/utils/supabase/info';
import { Toaster } from './components/ui/sonner';
import WelcomeScreen from './components/WelcomeScreen';
import LoginScreen from './components/LoginScreen';
import RegisterScreen from './components/RegisterScreen';
import DashboardScreen from './components/DashboardScreen';
import ExpensesListScreen from './components/ExpensesListScreen';
import AddExpenseScreen from './components/AddExpenseScreen';
import ExpenseDetailScreen from './components/ExpenseDetailScreen';
import ProfileScreen from './components/ProfileScreen';

// Create Supabase client
const supabase = createClient(
  `https://${projectId}.supabase.co`,
  publicAnonKey
);

export type Screen = 
  | 'welcome' 
  | 'login' 
  | 'register' 
  | 'dashboard' 
  | 'expenses-list' 
  | 'add-expense' 
  | 'expense-detail'
  | 'profile';

export interface User {
  id: string;
  email: string;
  name: string;
}

export interface Expense {
  id: string;
  description: string;
  amount: number;
  category: string;
  date: string;
  userId: string;
}

function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>('welcome');
  const [user, setUser] = useState<User | null>(null);
  const [accessToken, setAccessToken] = useState<string>('');
  const [selectedExpense, setSelectedExpense] = useState<Expense | null>(null);

  // Check for existing session on mount
  useEffect(() => {
    checkSession();
  }, []);

  const checkSession = async () => {
    try {
      const { data, error } = await supabase.auth.getSession();
      if (data.session && !error) {
        setAccessToken(data.session.access_token);
        setUser({
          id: data.session.user.id,
          email: data.session.user.email || '',
          name: data.session.user.user_metadata.name || ''
        });
        setCurrentScreen('dashboard');
      }
    } catch (error) {
      console.error('Error checking session:', error);
    }
  };

  const handleLogin = (token: string, userData: User) => {
    setAccessToken(token);
    setUser(userData);
    setCurrentScreen('dashboard');
  };

  const handleLogout = async () => {
    await supabase.auth.signOut();
    setAccessToken('');
    setUser(null);
    setCurrentScreen('welcome');
  };

  const handleExpenseSelect = (expense: Expense) => {
    setSelectedExpense(expense);
    setCurrentScreen('expense-detail');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Toaster position="top-center" />
      {currentScreen === 'welcome' && (
        <WelcomeScreen onNavigate={setCurrentScreen} />
      )}
      
      {currentScreen === 'login' && (
        <LoginScreen 
          onNavigate={setCurrentScreen}
          onLogin={handleLogin}
        />
      )}
      
      {currentScreen === 'register' && (
        <RegisterScreen 
          onNavigate={setCurrentScreen}
        />
      )}
      
      {currentScreen === 'dashboard' && user && (
        <DashboardScreen 
          onNavigate={setCurrentScreen}
          user={user}
          accessToken={accessToken}
        />
      )}
      
      {currentScreen === 'expenses-list' && (
        <ExpensesListScreen 
          onNavigate={setCurrentScreen}
          accessToken={accessToken}
          onExpenseSelect={handleExpenseSelect}
        />
      )}
      
      {currentScreen === 'add-expense' && (
        <AddExpenseScreen 
          onNavigate={setCurrentScreen}
          accessToken={accessToken}
        />
      )}
      
      {currentScreen === 'expense-detail' && selectedExpense && (
        <ExpenseDetailScreen 
          onNavigate={setCurrentScreen}
          accessToken={accessToken}
          expense={selectedExpense}
        />
      )}
      
      {currentScreen === 'profile' && user && (
        <ProfileScreen 
          onNavigate={setCurrentScreen}
          user={user}
          onLogout={handleLogout}
        />
      )}
    </div>
  );
}

export default App;