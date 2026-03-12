import React, { useState, useEffect } from 'react';
import { ArrowLeft, Search, Filter } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card, CardContent } from './ui/card';
import { Screen, Expense } from '../App';
import { projectId } from '/utils/supabase/info';
import { toast } from 'sonner';

interface ExpensesListScreenProps {
  onNavigate: (screen: Screen) => void;
  accessToken: string;
  onExpenseSelect: (expense: Expense) => void;
}

export default function ExpensesListScreen({ 
  onNavigate, 
  accessToken, 
  onExpenseSelect 
}: ExpensesListScreenProps) {
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [filteredExpenses, setFilteredExpenses] = useState<Expense[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchExpenses();
  }, []);

  useEffect(() => {
    filterExpenses();
  }, [searchQuery, expenses]);

  const fetchExpenses = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/expenses`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al cargar gastos');
        return;
      }

      setExpenses(data.expenses);
      setFilteredExpenses(data.expenses);
    } catch (error) {
      console.error('Error fetching expenses:', error);
      toast.error('Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
  };

  const filterExpenses = () => {
    if (!searchQuery.trim()) {
      setFilteredExpenses(expenses);
      return;
    }

    const query = searchQuery.toLowerCase();
    const filtered = expenses.filter(
      (expense) =>
        expense.description.toLowerCase().includes(query) ||
        expense.category.toLowerCase().includes(query)
    );
    setFilteredExpenses(filtered);
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
      month: 'long',
      year: 'numeric'
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

  const getTotalAmount = () => {
    return filteredExpenses.reduce((sum, expense) => sum + expense.amount, 0);
  };

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* Header */}
      <div className="border-b px-6 py-6 sticky top-0 z-10 bg-white">
        <div className="flex items-center mb-6">
          <button
            onClick={() => onNavigate('dashboard')}
            className="text-gray-500 hover:text-gray-900 transition-colors"
          >
            <ArrowLeft className="w-5 h-5" strokeWidth={1.5} />
          </button>
          <h1 className="flex-1 text-center text-lg font-light text-gray-900">Gastos</h1>
          <div className="w-5"></div>
        </div>

        {/* Search Bar */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" strokeWidth={1.5} />
          <Input
            type="text"
            placeholder="Buscar..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10 border-gray-200 focus:border-gray-900"
          />
        </div>
      </div>

      {/* Total Summary */}
      <div className="border-b px-6 py-6">
        <p className="text-gray-500 text-sm font-light mb-1">Total</p>
        <p className="text-3xl font-light text-gray-900">
          {formatCurrency(getTotalAmount())}
        </p>
        <p className="text-gray-400 text-xs mt-2">
          {filteredExpenses.length} {filteredExpenses.length === 1 ? 'gasto' : 'gastos'}
        </p>
      </div>

      {/* Expenses List */}
      <div className="flex-1 px-6 py-6">
        {loading ? (
          <div className="space-y-2">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="border border-gray-100 p-4 rounded-lg animate-pulse">
                <div className="h-4 bg-gray-100 rounded w-2/3 mb-2"></div>
                <div className="h-3 bg-gray-100 rounded w-1/3"></div>
              </div>
            ))}
          </div>
        ) : filteredExpenses.length === 0 ? (
          <div className="border border-gray-100 rounded-lg p-12 text-center">
            <Filter className="w-10 h-10 text-gray-300 mx-auto mb-3" strokeWidth={1.5} />
            <p className="text-gray-400 text-sm font-light">
              {searchQuery ? 'No se encontraron gastos' : 'No hay gastos registrados'}
            </p>
          </div>
        ) : (
          <div className="space-y-2">
            {filteredExpenses.map((expense) => (
              <div
                key={expense.id}
                className="border border-gray-100 rounded-lg p-4 hover:border-gray-200 transition-colors cursor-pointer"
                onClick={() => onExpenseSelect(expense)}
              >
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
                  <div className="text-right ml-4">
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

      {/* Add Button */}
      <div className="fixed bottom-6 right-6">
        <Button
          onClick={() => onNavigate('add-expense')}
          className="bg-gray-900 hover:bg-gray-800 rounded-full w-14 h-14 shadow-lg"
        >
          <span className="text-2xl font-light">+</span>
        </Button>
      </div>
    </div>
  );
}