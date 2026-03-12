import React, { useState } from 'react';
import { ArrowLeft, Mail, Lock } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Screen, User } from '../App';
import { projectId } from '/utils/supabase/info';
import { toast } from 'sonner';

interface LoginScreenProps {
  onNavigate: (screen: Screen) => void;
  onLogin: (token: string, user: User) => void;
}

export default function LoginScreen({ onNavigate, onLogin }: LoginScreenProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !password) {
      toast.error('Por favor completa todos los campos');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/login`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ email, password }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al iniciar sesión');
        return;
      }

      toast.success('¡Inicio de sesión exitoso!');
      onLogin(data.access_token, data.user);
    } catch (error) {
      console.error('Error during login:', error);
      toast.error('Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* Header */}
      <div className="px-6 py-6">
        <button
          onClick={() => onNavigate('welcome')}
          className="flex items-center text-gray-500 hover:text-gray-900 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" strokeWidth={1.5} />
        </button>
      </div>

      {/* Content */}
      <div className="flex-1 flex items-center justify-center px-6 pb-12">
        <div className="w-full max-w-sm space-y-8">
          <div className="space-y-2">
            <h2 className="text-3xl font-light text-gray-900">Iniciar sesión</h2>
            <p className="text-gray-500 font-light">
              Ingresa a tu cuenta
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email" className="text-sm font-normal text-gray-700">Correo</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="tu@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="border-gray-200 focus:border-gray-900"
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="text-sm font-normal text-gray-700">Contraseña</Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="border-gray-200 focus:border-gray-900"
                  disabled={loading}
                />
              </div>
            </div>

            <Button
              type="submit"
              className="w-full bg-gray-900 hover:bg-gray-800 py-6 font-normal"
              disabled={loading}
            >
              {loading ? 'Iniciando sesión...' : 'Iniciar sesión'}
            </Button>

            <div className="text-center text-sm">
              <span className="text-gray-500">¿No tienes cuenta? </span>
              <button
                type="button"
                onClick={() => onNavigate('register')}
                className="text-gray-900 hover:underline font-normal"
              >
                Regístrate
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}