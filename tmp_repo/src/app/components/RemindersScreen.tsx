import React, { useState, useEffect } from 'react';
import { ArrowLeft, Plus, Bell, Calendar, Trash2, CheckCircle2, Circle, AlertCircle, Mic, MicOff } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Screen } from '../App';
import { projectId } from '/utils/supabase/info';
import { toast } from 'sonner';

interface RemindersScreenProps {
  onNavigate: (screen: Screen) => void;
  accessToken: string;
}

interface Reminder {
  id: string;
  description: string;
  amount: number;
  category: string;
  reminderDate: string;
  completed: boolean;
  completedAt?: string;
  createdAt: string;
  userId: string;
}

const CATEGORIES = ['Alimentos', 'Transporte', 'Entretenimiento', 'Salud', 'Servicios', 'Otros'];

// Declarar la interfaz para el reconocimiento de voz
interface SpeechRecognitionEvent extends Event {
  results: SpeechRecognitionResultList;
  resultIndex: number;
}

interface SpeechRecognitionResultList {
  length: number;
  item(index: number): SpeechRecognitionResult;
  [index: number]: SpeechRecognitionResult;
}

interface SpeechRecognitionResult {
  length: number;
  item(index: number): SpeechRecognitionAlternative;
  [index: number]: SpeechRecognitionAlternative;
  isFinal: boolean;
}

interface SpeechRecognitionAlternative {
  transcript: string;
  confidence: number;
}

interface SpeechRecognition extends EventTarget {
  continuous: boolean;
  interimResults: boolean;
  lang: string;
  start(): void;
  stop(): void;
  abort(): void;
  onresult: ((event: SpeechRecognitionEvent) => void) | null;
  onerror: ((event: Event) => void) | null;
  onend: (() => void) | null;
}

declare global {
  interface Window {
    SpeechRecognition: new () => SpeechRecognition;
    webkitSpeechRecognition: new () => SpeechRecognition;
  }
}

export default function RemindersScreen({ onNavigate, accessToken }: RemindersScreenProps) {
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    description: '',
    amount: '',
    category: 'Alimentos',
    reminderDate: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [recognition, setRecognition] = useState<SpeechRecognition | null>(null);

  useEffect(() => {
    fetchReminders();
  }, []);

  // Inicializar reconocimiento de voz
  const startVoiceRecognition = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      toast.error('Tu navegador no soporta reconocimiento de voz');
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.continuous = false;
    recognition.interimResults = false;
    recognition.lang = 'es-MX';

    recognition.onresult = (event: SpeechRecognitionEvent) => {
      const transcript = event.results[0][0].transcript;
      setFormData({ ...formData, description: transcript });
      setIsListening(false);
    };

    recognition.onerror = (event: Event) => {
      console.error('Error en reconocimiento de voz:', event);
      toast.error('Error al reconocer voz. Intenta de nuevo');
      setIsListening(false);
    };

    recognition.onend = () => {
      setIsListening(false);
    };

    try {
      recognition.start();
      setIsListening(true);
      setRecognition(recognition);
      toast.success('Escuchando... habla ahora');
    } catch (error) {
      console.error('Error starting recognition:', error);
      toast.error('Error al iniciar reconocimiento de voz');
      setIsListening(false);
    }
  };

  const stopVoiceRecognition = () => {
    if (recognition) {
      recognition.stop();
      setIsListening(false);
    }
  };

  const fetchReminders = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/reminders`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al cargar recordatorios');
        return;
      }

      setReminders(data.reminders);
    } catch (error) {
      console.error('Error fetching reminders:', error);
      toast.error('Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.description || !formData.amount || !formData.category || !formData.reminderDate) {
      toast.error('Todos los campos son obligatorios');
      return;
    }

    if (parseFloat(formData.amount) <= 0) {
      toast.error('El monto debe ser mayor a cero');
      return;
    }

    setSubmitting(true);

    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/reminders`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`,
          },
          body: JSON.stringify(formData),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al crear recordatorio');
        return;
      }

      toast.success('Recordatorio creado exitosamente');
      setFormData({
        description: '',
        amount: '',
        category: 'Alimentos',
        reminderDate: '',
      });
      setShowForm(false);
      fetchReminders();
    } catch (error) {
      console.error('Error creating reminder:', error);
      toast.error('Error al conectar con el servidor');
    } finally {
      setSubmitting(false);
    }
  };

  const toggleComplete = async (reminder: Reminder) => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/reminders/${reminder.id}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`,
          },
          body: JSON.stringify({ completed: !reminder.completed }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al actualizar recordatorio');
        return;
      }

      toast.success(reminder.completed ? 'Recordatorio reactivado' : 'Recordatorio completado');
      fetchReminders();
    } catch (error) {
      console.error('Error updating reminder:', error);
      toast.error('Error al conectar con el servidor');
    }
  };

  const deleteReminder = async (id: string) => {
    if (!confirm('¿Estás seguro de eliminar este recordatorio?')) {
      return;
    }

    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/reminders/${id}`,
        {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al eliminar recordatorio');
        return;
      }

      toast.success('Recordatorio eliminado');
      fetchReminders();
    } catch (error) {
      console.error('Error deleting reminder:', error);
      toast.error('Error al conectar con el servidor');
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

  const isOverdue = (dateString: string, completed: boolean) => {
    if (completed) return false;
    return new Date(dateString) < new Date();
  };

  const isToday = (dateString: string) => {
    const today = new Date();
    const date = new Date(dateString);
    return date.toDateString() === today.toDateString();
  };

  const pendingReminders = reminders.filter(r => !r.completed);
  const completedReminders = reminders.filter(r => r.completed);

  return (
    <div className="min-h-screen bg-white pb-20">
      {/* Header */}
      <div className="px-6 py-6 border-b sticky top-0 bg-white z-10">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => onNavigate('dashboard')}
              className="text-gray-500 hover:text-gray-900 transition-colors"
            >
              <ArrowLeft className="w-6 h-6" strokeWidth={1.5} />
            </button>
            <div>
              <h1 className="text-xl font-light text-gray-900">Recordatorios</h1>
              <p className="text-sm text-gray-400 font-light">Gastos futuros programados</p>
            </div>
          </div>
          
          <button
            onClick={() => setShowForm(!showForm)}
            className={`p-2 rounded-lg transition-colors ${
              showForm ? 'bg-gray-100 text-gray-900' : 'text-gray-500 hover:text-gray-900'
            }`}
          >
            <Plus className="w-5 h-5" strokeWidth={1.5} />
          </button>
        </div>
      </div>

      {/* Add Reminder Form */}
      {showForm && (
        <div className="px-6 py-6 border-b bg-gray-50">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="description" className="text-sm font-light text-gray-700">
                Descripción
              </Label>
              <div className="relative">
                <Input
                  id="description"
                  type="text"
                  placeholder="Ej: Pago de luz"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="mt-1.5 border-gray-200 font-light pr-12"
                />
                <button
                  type="button"
                  className={`absolute right-3 top-1/2 -translate-y-1/2 transition-colors ${
                    isListening 
                      ? 'text-red-500 animate-pulse' 
                      : 'text-gray-400 hover:text-gray-900'
                  }`}
                  onClick={isListening ? stopVoiceRecognition : startVoiceRecognition}
                  disabled={submitting}
                >
                  {isListening ? (
                    <MicOff className="w-5 h-5" strokeWidth={1.5} />
                  ) : (
                    <Mic className="w-5 h-5" strokeWidth={1.5} />
                  )}
                </button>
              </div>
              {isListening && (
                <p className="text-xs text-gray-500 font-light flex items-center gap-1.5 mt-2">
                  <span className="w-2 h-2 bg-red-500 rounded-full animate-pulse"></span>
                  Escuchando... habla ahora
                </p>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="amount" className="text-sm font-light text-gray-700">
                  Monto
                </Label>
                <Input
                  id="amount"
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  value={formData.amount}
                  onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                  className="mt-1.5 border-gray-200 font-light"
                />
              </div>

              <div>
                <Label htmlFor="reminderDate" className="text-sm font-light text-gray-700">
                  Fecha
                </Label>
                <Input
                  id="reminderDate"
                  type="date"
                  value={formData.reminderDate}
                  onChange={(e) => setFormData({ ...formData, reminderDate: e.target.value })}
                  className="mt-1.5 border-gray-200 font-light"
                  min={new Date().toISOString().split('T')[0]}
                />
              </div>
            </div>

            <div>
              <Label htmlFor="category" className="text-sm font-light text-gray-700">
                Categoría
              </Label>
              <select
                id="category"
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                className="mt-1.5 w-full px-3 py-2 border border-gray-200 rounded-lg text-sm font-light focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
              >
                {CATEGORIES.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>

            <div className="flex gap-3 pt-2">
              <Button
                type="submit"
                disabled={submitting}
                className="flex-1 bg-gray-900 hover:bg-gray-800 font-normal"
              >
                {submitting ? 'Guardando...' : 'Guardar'}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => setShowForm(false)}
                className="border-gray-200 font-normal"
              >
                Cancelar
              </Button>
            </div>
          </form>
        </div>
      )}

      {/* Content */}
      <div className="px-6 py-6 space-y-6">
        {loading ? (
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="border border-gray-100 p-4 rounded-lg animate-pulse">
                <div className="h-4 bg-gray-100 rounded w-2/3 mb-2"></div>
                <div className="h-3 bg-gray-100 rounded w-1/3"></div>
              </div>
            ))}
          </div>
        ) : (
          <>
            {/* Pending Reminders */}
            {pendingReminders.length > 0 && (
              <div>
                <h2 className="text-base font-normal text-gray-900 mb-3">
                  Pendientes ({pendingReminders.length})
                </h2>
                <div className="space-y-2">
                  {pendingReminders.map((reminder) => (
                    <div
                      key={reminder.id}
                      className={`border rounded-lg p-4 ${
                        isOverdue(reminder.reminderDate, reminder.completed)
                          ? 'border-red-200 bg-red-50'
                          : isToday(reminder.reminderDate)
                          ? 'border-blue-200 bg-blue-50'
                          : 'border-gray-100'
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <button
                          onClick={() => toggleComplete(reminder)}
                          className="mt-0.5 text-gray-400 hover:text-gray-900 transition-colors"
                        >
                          <Circle className="w-5 h-5" strokeWidth={1.5} />
                        </button>
                        
                        <div className="flex-1 min-w-0">
                          <div className="flex items-start justify-between gap-3">
                            <div className="flex-1">
                              <h3 className="font-normal text-gray-900 text-sm mb-1">
                                {reminder.description}
                              </h3>
                              <div className="flex items-center gap-2 text-xs text-gray-500">
                                <span>{reminder.category}</span>
                                <span>•</span>
                                <span>{formatDate(reminder.reminderDate)}</span>
                              </div>
                              {isOverdue(reminder.reminderDate, reminder.completed) && (
                                <div className="flex items-center gap-1 mt-2 text-xs text-red-600">
                                  <AlertCircle className="w-3 h-3" />
                                  <span>Vencido</span>
                                </div>
                              )}
                              {isToday(reminder.reminderDate) && (
                                <div className="flex items-center gap-1 mt-2 text-xs text-blue-600">
                                  <Bell className="w-3 h-3" />
                                  <span>Hoy</span>
                                </div>
                              )}
                            </div>
                            <div className="text-right">
                              <p className="text-base font-normal text-gray-900">
                                {formatCurrency(reminder.amount)}
                              </p>
                            </div>
                          </div>
                        </div>

                        <button
                          onClick={() => deleteReminder(reminder.id)}
                          className="text-gray-400 hover:text-red-600 transition-colors"
                        >
                          <Trash2 className="w-4 h-4" strokeWidth={1.5} />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Completed Reminders */}
            {completedReminders.length > 0 && (
              <div>
                <h2 className="text-base font-normal text-gray-900 mb-3">
                  Completados ({completedReminders.length})
                </h2>
                <div className="space-y-2">
                  {completedReminders.map((reminder) => (
                    <div
                      key={reminder.id}
                      className="border border-gray-100 rounded-lg p-4 bg-gray-50"
                    >
                      <div className="flex items-start gap-3">
                        <button
                          onClick={() => toggleComplete(reminder)}
                          className="mt-0.5 text-green-500 hover:text-gray-400 transition-colors"
                        >
                          <CheckCircle2 className="w-5 h-5" strokeWidth={1.5} />
                        </button>
                        
                        <div className="flex-1 min-w-0">
                          <div className="flex items-start justify-between gap-3">
                            <div className="flex-1">
                              <h3 className="font-normal text-gray-500 text-sm mb-1 line-through">
                                {reminder.description}
                              </h3>
                              <div className="flex items-center gap-2 text-xs text-gray-400">
                                <span>{reminder.category}</span>
                                <span>•</span>
                                <span>{formatDate(reminder.reminderDate)}</span>
                              </div>
                            </div>
                            <div className="text-right">
                              <p className="text-base font-normal text-gray-500 line-through">
                                {formatCurrency(reminder.amount)}
                              </p>
                            </div>
                          </div>
                        </div>

                        <button
                          onClick={() => deleteReminder(reminder.id)}
                          className="text-gray-400 hover:text-red-600 transition-colors"
                        >
                          <Trash2 className="w-4 h-4" strokeWidth={1.5} />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Empty State */}
            {reminders.length === 0 && (
              <div className="border border-gray-100 rounded-lg p-12 text-center">
                <Bell className="w-10 h-10 text-gray-300 mx-auto mb-3" strokeWidth={1.5} />
                <p className="text-gray-400 text-sm font-light mb-1">No hay recordatorios</p>
                <p className="text-gray-400 text-xs font-light">
                  Crea uno para recordar tus gastos futuros
                </p>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}