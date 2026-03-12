import React, { useState } from 'react';
import { ArrowLeft, Edit2, Trash2, DollarSign, FileText, Tag, Calendar } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from './ui/alert-dialog';
import { Screen, Expense } from '../App';
import { projectId } from '/utils/supabase/info';
import { toast } from 'sonner';

interface ExpenseDetailScreenProps {
  onNavigate: (screen: Screen) => void;
  accessToken: string;
  expense: Expense;
}

const categories = [
  'Alimentos',
  'Transporte',
  'Entretenimiento',
  'Salud',
  'Servicios',
  'Otros',
];

export default function ExpenseDetailScreen({ 
  onNavigate, 
  accessToken, 
  expense 
}: ExpenseDetailScreenProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [description, setDescription] = useState(expense.description);
  const [amount, setAmount] = useState(expense.amount.toString());
  const [category, setCategory] = useState(expense.category);
  const [date, setDate] = useState(new Date(expense.date).toISOString().split('T')[0]);
  const [loading, setLoading] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!description || !amount || !category || !date) {
      toast.error('Por favor completa todos los campos');
      return;
    }

    const amountNumber = parseFloat(amount);
    if (isNaN(amountNumber) || amountNumber <= 0) {
      toast.error('El monto debe ser mayor a cero');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/expenses/${expense.id}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`,
          },
          body: JSON.stringify({
            description,
            amount: amountNumber,
            category,
            date: new Date(date).toISOString(),
          }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al actualizar gasto');
        return;
      }

      toast.success('¡Gasto actualizado exitosamente!');
      onNavigate('expenses-list');
    } catch (error) {
      console.error('Error updating expense:', error);
      toast.error('Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    setLoading(true);

    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/expenses/${expense.id}`,
        {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al eliminar gasto');
        return;
      }

      toast.success('¡Gasto eliminado exitosamente!');
      onNavigate('expenses-list');
    } catch (error) {
      console.error('Error deleting expense:', error);
      toast.error('Error al conectar con el servidor');
    } finally {
      setLoading(false);
      setShowDeleteDialog(false);
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
      month: 'long',
      year: 'numeric'
    });
  };

  const getCategoryColor = (category: string) => {
    const colors: { [key: string]: string } = {
      'Alimentos': 'bg-green-100 text-green-700 border-green-200',
      'Transporte': 'bg-blue-100 text-blue-700 border-blue-200',
      'Entretenimiento': 'bg-purple-100 text-purple-700 border-purple-200',
      'Salud': 'bg-red-100 text-red-700 border-red-200',
      'Servicios': 'bg-yellow-100 text-yellow-700 border-yellow-200',
      'Otros': 'bg-gray-100 text-gray-700 border-gray-200',
    };
    return colors[category] || 'bg-gray-100 text-gray-700 border-gray-200';
  };

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* Header */}
      <div className="border-b px-6 py-6">
        <div className="flex items-center">
          <button
            onClick={() => onNavigate('expenses-list')}
            className="text-gray-500 hover:text-gray-900 transition-colors"
          >
            <ArrowLeft className="w-5 h-5" strokeWidth={1.5} />
          </button>
          <h1 className="flex-1 text-center text-lg font-light text-gray-900">Detalle</h1>
          <div className="w-5"></div>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 px-6 py-8">
        {!isEditing ? (
          /* View Mode */
          <div className="space-y-8 max-w-sm mx-auto">
            <div className="space-y-6">
              <div>
                <p className="text-xs text-gray-400 mb-2 uppercase tracking-wide">Descripción</p>
                <p className="text-xl font-light text-gray-900">{expense.description}</p>
              </div>

              <div>
                <p className="text-xs text-gray-400 mb-2 uppercase tracking-wide">Monto</p>
                <p className="text-3xl font-light text-gray-900">
                  {formatCurrency(expense.amount)}
                </p>
              </div>

              <div>
                <p className="text-xs text-gray-400 mb-2 uppercase tracking-wide">Categoría</p>
                <p className="text-base font-normal text-gray-600">{expense.category}</p>
              </div>

              <div>
                <p className="text-xs text-gray-400 mb-2 uppercase tracking-wide">Fecha</p>
                <p className="text-base font-normal text-gray-600">{formatDate(expense.date)}</p>
              </div>
            </div>

            <div className="space-y-3 pt-4">
              <Button
                onClick={() => setIsEditing(true)}
                className="w-full bg-gray-900 hover:bg-gray-800 py-6 font-normal"
              >
                Editar
              </Button>

              <Button
                onClick={() => setShowDeleteDialog(true)}
                variant="outline"
                className="w-full border-gray-200 text-red-600 hover:bg-red-50 hover:border-red-200 py-6 font-normal"
              >
                Eliminar
              </Button>
            </div>
          </div>
        ) : (
          /* Edit Mode */
          <form onSubmit={handleUpdate} className="space-y-6 max-w-sm mx-auto">
            <div className="space-y-2">
              <Label htmlFor="description" className="text-sm font-normal text-gray-700">Descripción</Label>
              <Input
                id="description"
                type="text"
                placeholder="ej. Comida en restaurante"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="border-gray-200 focus:border-gray-900"
                disabled={loading}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="amount" className="text-sm font-normal text-gray-700">Monto</Label>
              <Input
                id="amount"
                type="number"
                step="0.01"
                min="0"
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="border-gray-200 focus:border-gray-900"
                disabled={loading}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="category" className="text-sm font-normal text-gray-700">Categoría</Label>
              <Select
                value={category}
                onValueChange={setCategory}
                disabled={loading}
              >
                <SelectTrigger id="category" className="border-gray-200">
                  <SelectValue placeholder="Selecciona una categoría" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((cat) => (
                    <SelectItem key={cat} value={cat}>
                      {cat}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="date" className="text-sm font-normal text-gray-700">Fecha</Label>
              <Input
                id="date"
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="border-gray-200 focus:border-gray-900"
                disabled={loading}
              />
            </div>

            <div className="pt-4 space-y-3">
              <Button
                type="submit"
                className="w-full bg-gray-900 hover:bg-gray-800 py-6 font-normal"
                disabled={loading}
              >
                {loading ? 'Guardando...' : 'Guardar'}
              </Button>

              <Button
                type="button"
                variant="outline"
                className="w-full border-gray-200 font-normal"
                onClick={() => setIsEditing(false)}
                disabled={loading}
              >
                Cancelar
              </Button>
            </div>
          </form>
        )}
      </div>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle className="font-light">¿Eliminar gasto?</AlertDialogTitle>
            <AlertDialogDescription className="font-light">
              Esta acción no se puede deshacer.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel className="font-normal">Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-red-600 hover:bg-red-700 font-normal"
            >
              Eliminar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}