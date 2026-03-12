import React, { useState } from 'react';
import { ArrowLeft, DollarSign, FileText, Tag, Calendar, Mic, MicOff } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Screen } from '../App';
import { projectId } from '/utils/supabase/info';
import { toast } from 'sonner';

interface AddExpenseScreenProps {
  onNavigate: (screen: Screen) => void;
  accessToken: string;
}

const categories = [
  'Alimentos',
  'Transporte',
  'Entretenimiento',
  'Salud',
  'Servicios',
  'Otros',
];

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

export default function AddExpenseScreen({ onNavigate, accessToken }: AddExpenseScreenProps) {
  const [description, setDescription] = useState('');
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [recognition, setRecognition] = useState<SpeechRecognition | null>(null);

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
      setDescription(transcript);
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

  const handleSubmit = async (e: React.FormEvent) => {
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
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/expenses`,
        {
          method: 'POST',
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
        toast.error(data.error || 'Error al crear gasto');
        return;
      }

      toast.success('¡Gasto creado exitosamente!');
      onNavigate('dashboard');
    } catch (error) {
      console.error('Error creating expense:', error);
      toast.error('Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* Header */}
      <div className="border-b px-6 py-6">
        <div className="flex items-center">
          <button
            onClick={() => onNavigate('dashboard')}
            className="text-gray-500 hover:text-gray-900 transition-colors"
          >
            <ArrowLeft className="w-5 h-5" strokeWidth={1.5} />
          </button>
          <h1 className="flex-1 text-center text-lg font-light text-gray-900">Nuevo Gasto</h1>
          <div className="w-5"></div>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 px-6 py-8">
        <form onSubmit={handleSubmit} className="space-y-6 max-w-sm mx-auto">
          <div className="space-y-2">
            <Label htmlFor="description" className="text-sm font-normal text-gray-700">Descripción</Label>
            <div className="relative">
              <Input
                id="description"
                type="text"
                placeholder="ej. Comida en restaurante"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="border-gray-200 focus:border-gray-900 pr-12"
                disabled={loading}
              />
              <button
                type="button"
                className={`absolute right-3 top-1/2 -translate-y-1/2 transition-colors ${
                  isListening 
                    ? 'text-red-500 animate-pulse' 
                    : 'text-gray-400 hover:text-gray-900'
                }`}
                onClick={isListening ? stopVoiceRecognition : startVoiceRecognition}
                disabled={loading}
              >
                {isListening ? (
                  <MicOff className="w-5 h-5" strokeWidth={1.5} />
                ) : (
                  <Mic className="w-5 h-5" strokeWidth={1.5} />
                )}
              </button>
            </div>
            {isListening && (
              <p className="text-xs text-gray-500 font-light flex items-center gap-1.5">
                <span className="w-2 h-2 bg-red-500 rounded-full animate-pulse"></span>
                Escuchando... habla ahora
              </p>
            )}
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

          <div className="pt-4">
            <Button
              type="submit"
              className="w-full bg-gray-900 hover:bg-gray-800 py-6 font-normal"
              disabled={loading}
            >
              {loading ? 'Guardando...' : 'Guardar'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}