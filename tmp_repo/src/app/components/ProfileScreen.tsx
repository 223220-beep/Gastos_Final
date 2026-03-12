import React from 'react';
import { ArrowLeft, User, Mail, LogOut, Shield } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Screen } from '../App';
import { User as UserType } from '../App';

interface ProfileScreenProps {
  onNavigate: (screen: Screen) => void;
  user: UserType;
  onLogout: () => void;
}

export default function ProfileScreen({ onNavigate, user, onLogout }: ProfileScreenProps) {
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
          <h1 className="flex-1 text-center text-lg font-light text-gray-900">Perfil</h1>
          <div className="w-5"></div>
        </div>
      </div>

      {/* Profile Content */}
      <div className="flex-1 px-6 py-8 space-y-8">
        {/* User Avatar Section */}
        <div className="flex flex-col items-center text-center max-w-sm mx-auto">
          <div className="border border-gray-200 p-6 rounded-full mb-4">
            <User className="w-12 h-12 text-gray-400" strokeWidth={1.5} />
          </div>
          <h2 className="text-xl font-light text-gray-900 mb-1">{user.name}</h2>
          <p className="text-gray-500 text-sm font-light">{user.email}</p>
        </div>

        {/* User Info Card */}
        <div className="max-w-sm mx-auto space-y-6">
          <div className="space-y-4">
            <div className="border-b border-gray-100 pb-4">
              <p className="text-xs text-gray-400 mb-1 uppercase tracking-wide">Nombre</p>
              <p className="font-normal text-gray-900">{user.name}</p>
            </div>

            <div className="border-b border-gray-100 pb-4">
              <p className="text-xs text-gray-400 mb-1 uppercase tracking-wide">Correo</p>
              <p className="font-normal text-gray-900">{user.email}</p>
            </div>

            <div className="pb-4">
              <p className="text-xs text-gray-400 mb-1 uppercase tracking-wide">ID</p>
              <p className="font-mono text-xs text-gray-500">{user.id}</p>
            </div>
          </div>
        </div>

        {/* Logout Button */}
        <div className="max-w-sm mx-auto pt-4">
          <Button
            onClick={onLogout}
            variant="outline"
            className="w-full border-gray-200 text-red-600 hover:bg-red-50 hover:border-red-200 py-6 font-normal"
          >
            Cerrar sesión
          </Button>
        </div>
      </div>
    </div>
  );
}