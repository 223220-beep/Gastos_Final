import React from 'react';
import { Wallet } from 'lucide-react';
import { Button } from './ui/button';
import { Screen } from '../App';

interface WelcomeScreenProps {
  onNavigate: (screen: Screen) => void;
}

export default function WelcomeScreen({ onNavigate }: WelcomeScreenProps) {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen px-6 bg-white">
      <div className="text-center space-y-12 max-w-sm">
        <div className="space-y-4">
          <div className="flex justify-center">
            <Wallet className="w-16 h-16 text-gray-900" strokeWidth={1.5} />
          </div>
          
          <div className="space-y-2">
            <h1 className="text-4xl font-light text-gray-900">GastosApp</h1>
            <p className="text-gray-500 text-base font-light">
              Controla tus gastos personales
            </p>
          </div>
        </div>

        <div className="space-y-3 w-full">
          <Button
            onClick={() => onNavigate('login')}
            className="w-full bg-gray-900 text-white hover:bg-gray-800 py-6 text-base font-normal"
          >
            Iniciar sesión
          </Button>
          
          <Button
            onClick={() => onNavigate('register')}
            variant="outline"
            className="w-full border border-gray-300 text-gray-900 hover:bg-gray-50 py-6 text-base font-normal"
          >
            Registrarse
          </Button>
        </div>
      </div>
    </div>
  );
}