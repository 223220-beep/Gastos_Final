import React, { useState, useEffect } from 'react';
import { Plus, List, UserCircle, TrendingUp, Calendar } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Screen, User, Expense } from '../App';
import { projectId } from '/utils/supabase/info';
import { toast } from 'sonner';

interface DashboardScreenProps {
  onNavigate: (screen: Screen) => void;
  user: User;
  accessToken: string;
}

export default function DashboardScreen({ onNavigate, user, accessToken }: DashboardScreenProps) {
  const [totalMonth, setTotalMonth] = useState(0);
  const [recentExpenses, setRecentExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSummary();
  }, []);

  const fetchSummary = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/expenses/summary`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al cargar resumen');
        return;
      }

      setTotalMonth(data.totalMonth);
      setRecentExpenses(data.recentExpenses);
    } catch (error) {
      console.error('Error fetching summary:', error);
      toast.error('Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-MX', { 
      day: 'numeric', 
      month: 'short' 
    });
  };

  const getCategoryColor = (category: string) => {
    const colors: { [key: string]: string } = {
      'Alimentos': 'bg-green-100 text-green-700',
      'Transporte': 'bg-blue-100 text-blue-700',
      'Entretenimiento': 'bg-purple-100 text-purple-700',
      'Salud': 'bg-red-100 text-red-700',
      'Servicios': 'bg-yellow-100 text-yellow-700',
      'Otros': 'bg-gray-100 text-gray-700',
    };
    return colors[category] || 'bg-gray-100 text-gray-700';
  };

  return (
    <div className="min-h-screen bg-white pb-20">
      {/* Header */}
      <div className="px-6 py-8 border-b">
        <div className="flex justify-between items-start mb-8">
          <div>
            <p className="text-gray-500 text-sm font-light mb-1">Hola,</p>
            <h1 className="text-2xl font-light text-gray-900">{user.name}</h1>
          </div>
          <button
            onClick={() => onNavigate('profile')}
            className="text-gray-500 hover:text-gray-900 transition-colors"
          >
            <UserCircle className="w-7 h-7" strokeWidth={1.5} />
          </button>
        </div>

        {/* Total Card */}
        <div className="border border-gray-200 rounded-lg p-6">
          <p className="text-gray-500 text-sm font-light mb-2">Total del mes</p>
          <p className="text-4xl font-light text-gray-900">
            {loading ? '...' : formatCurrency(totalMonth)}
          </p>
        </div>
      </div>

      {/* Content */}
      <div className="px-6 py-6 space-y-8">
        {/* Quick Actions */}
        <div className="grid grid-cols-2 gap-3">
          <Button
            onClick={() => onNavigate('add-expense')}
            className="bg-gray-900 hover:bg-gray-800 h-20 flex-col gap-1.5 font-normal"
          >
            <Plus className="w-5 h-5" strokeWidth={1.5} />
            <span className="text-sm">Agregar</span>
          </Button>
          
          <Button
            onClick={() => onNavigate('expenses-list')}
            variant="outline"
            className="h-20 flex-col gap-1.5 border-gray-200 font-normal"
          >
            <List className="w-5 h-5" strokeWidth={1.5} />
            <span className="text-sm">Ver Todos</span>
          </Button>
        </div>

        {/* Recent Expenses */}
        <div>
          <h2 className="text-lg font-light text-gray-900 mb-4">Recientes</h2>
          
          {loading ? (
            <div className="space-y-2">
              {[1, 2, 3].map((i) => (
                <div key={i} className="border border-gray-100 p-4 rounded-lg animate-pulse">
                  <div className="h-4 bg-gray-100 rounded w-1/3 mb-2"></div>
                  <div className="h-3 bg-gray-100 rounded w-1/4"></div>
                </div>
              ))}
            </div>
          ) : recentExpenses.length === 0 ? (
            <div className="border border-gray-100 rounded-lg p-12 text-center">
              <Calendar className="w-10 h-10 text-gray-300 mx-auto mb-3" strokeWidth={1.5} />
              <p className="text-gray-400 text-sm font-light">No hay gastos registrados</p>
            </div>
          ) : (
            <div className="space-y-2">
              {recentExpenses.map((expense) => (
                <div key={expense.id} className="border border-gray-100 rounded-lg p-4 hover:border-gray-200 transition-colors">
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <h3 className="font-normal text-gray-900 mb-1.5 text-sm">
                        {expense.description}
                      </h3>
                      <div className="flex items-center gap-2 text-xs text-gray-400">
                        <span>{expense.category}</span>
                        <span>•</span>
                        <span>{formatDate(expense.date)}</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-base font-normal text-gray-900">
                        {formatCurrency(expense.amount)}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}