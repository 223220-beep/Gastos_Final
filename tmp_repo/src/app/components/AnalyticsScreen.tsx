import React, { useState, useEffect } from 'react';
import { ArrowLeft, TrendingUp, TrendingDown, BarChart3, PieChart as PieChartIcon } from 'lucide-react';
import { LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { Screen } from '../App';
import { projectId } from '/utils/supabase/info';
import { toast } from 'sonner';

interface AnalyticsScreenProps {
  onNavigate: (screen: Screen) => void;
  accessToken: string;
}

interface MonthlyData {
  month: string;
  total: number;
  count: number;
}

interface CategoryData {
  name: string;
  value: number;
}

interface AnalyticsData {
  monthlyData: MonthlyData[];
  categoryData: CategoryData[];
  highestMonth: MonthlyData;
  currentMonthTotal: number;
  previousMonthTotal: number;
  percentageChange: number;
}

const COLORS = ['#10b981', '#3b82f6', '#8b5cf6', '#ef4444', '#eab308', '#6b7280'];

const CATEGORY_COLORS: { [key: string]: string } = {
  'Alimentos': '#10b981',
  'Transporte': '#3b82f6',
  'Entretenimiento': '#8b5cf6',
  'Salud': '#ef4444',
  'Servicios': '#eab308',
  'Otros': '#6b7280',
};

export default function AnalyticsScreen({ onNavigate, accessToken }: AnalyticsScreenProps) {
  const [analytics, setAnalytics] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [chartType, setChartType] = useState<'line' | 'bar'>('line');

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-aa79c66d/expenses/analytics`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );

      const data = await response.json();

      if (!response.ok) {
        toast.error(data.error || 'Error al cargar estadísticas');
        return;
      }

      setAnalytics(data);
    } catch (error) {
      console.error('Error fetching analytics:', error);
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

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white border border-gray-200 rounded-lg p-3 shadow-sm">
          <p className="text-sm font-normal text-gray-900">{payload[0].payload.month}</p>
          <p className="text-sm font-light text-gray-600">
            {formatCurrency(payload[0].value)}
          </p>
          {payload[0].payload.count !== undefined && (
            <p className="text-xs text-gray-400">
              {payload[0].payload.count} {payload[0].payload.count === 1 ? 'gasto' : 'gastos'}
            </p>
          )}
        </div>
      );
    }
    return null;
  };

  const PieTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white border border-gray-200 rounded-lg p-3 shadow-sm">
          <p className="text-sm font-normal text-gray-900">{payload[0].name}</p>
          <p className="text-sm font-light text-gray-600">
            {formatCurrency(payload[0].value)}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="min-h-screen bg-white pb-20">
      {/* Header */}
      <div className="px-6 py-6 border-b sticky top-0 bg-white z-10">
        <div className="flex items-center gap-4">
          <button
            onClick={() => onNavigate('dashboard')}
            className="text-gray-500 hover:text-gray-900 transition-colors"
          >
            <ArrowLeft className="w-6 h-6" strokeWidth={1.5} />
          </button>
          <div>
            <h1 className="text-xl font-light text-gray-900">Estadísticas</h1>
            <p className="text-sm text-gray-400 font-light">Análisis de tus gastos</p>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="px-6 py-6 space-y-6">
        {loading ? (
          <div className="space-y-6">
            {[1, 2, 3].map((i) => (
              <div key={i} className="border border-gray-100 p-6 rounded-lg animate-pulse">
                <div className="h-4 bg-gray-100 rounded w-1/3 mb-4"></div>
                <div className="h-48 bg-gray-100 rounded"></div>
              </div>
            ))}
          </div>
        ) : analytics ? (
          <>
            {/* Comparison Card */}
            <div className="border border-gray-200 rounded-lg p-6">
              <p className="text-gray-500 text-sm font-light mb-4">Comparación mensual</p>
              <div className="flex items-end justify-between">
                <div>
                  <p className="text-3xl font-light text-gray-900">
                    {formatCurrency(analytics.currentMonthTotal)}
                  </p>
                  <p className="text-xs text-gray-400 font-light mt-1">Este mes</p>
                </div>
                {analytics.percentageChange !== 0 && (
                  <div className={`flex items-center gap-1 ${
                    analytics.percentageChange > 0 ? 'text-red-500' : 'text-green-500'
                  }`}>
                    {analytics.percentageChange > 0 ? (
                      <TrendingUp className="w-4 h-4" strokeWidth={1.5} />
                    ) : (
                      <TrendingDown className="w-4 h-4" strokeWidth={1.5} />
                    )}
                    <span className="text-sm font-light">
                      {Math.abs(analytics.percentageChange).toFixed(1)}%
                    </span>
                  </div>
                )}
              </div>
              <div className="mt-4 pt-4 border-t border-gray-100">
                <p className="text-sm text-gray-500 font-light">
                  Mes anterior: {formatCurrency(analytics.previousMonthTotal)}
                </p>
              </div>
            </div>

            {/* Highest Month Card */}
            {analytics.highestMonth && analytics.highestMonth.total > 0 && (
              <div className="border border-gray-200 rounded-lg p-6">
                <p className="text-gray-500 text-sm font-light mb-2">Mes con más gastos</p>
                <p className="text-2xl font-light text-gray-900 mb-1">
                  {analytics.highestMonth.month}
                </p>
                <p className="text-lg text-gray-600 font-light">
                  {formatCurrency(analytics.highestMonth.total)}
                </p>
                <p className="text-xs text-gray-400 font-light mt-1">
                  {analytics.highestMonth.count} {analytics.highestMonth.count === 1 ? 'gasto' : 'gastos'}
                </p>
              </div>
            )}

            {/* Monthly Trend Chart */}
            <div className="border border-gray-200 rounded-lg p-6">
              <div className="flex items-center justify-between mb-6">
                <p className="text-gray-900 font-normal">Tendencia mensual</p>
                <div className="flex gap-2">
                  <button
                    onClick={() => setChartType('line')}
                    className={`p-2 rounded-lg transition-colors ${
                      chartType === 'line' 
                        ? 'bg-gray-100 text-gray-900' 
                        : 'text-gray-400 hover:text-gray-600'
                    }`}
                  >
                    <TrendingUp className="w-4 h-4" strokeWidth={1.5} />
                  </button>
                  <button
                    onClick={() => setChartType('bar')}
                    className={`p-2 rounded-lg transition-colors ${
                      chartType === 'bar' 
                        ? 'bg-gray-100 text-gray-900' 
                        : 'text-gray-400 hover:text-gray-600'
                    }`}
                  >
                    <BarChart3 className="w-4 h-4" strokeWidth={1.5} />
                  </button>
                </div>
              </div>
              
              <ResponsiveContainer width="100%" height={250}>
                {chartType === 'line' ? (
                  <LineChart data={analytics.monthlyData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
                    <XAxis 
                      dataKey="month" 
                      tick={{ fontSize: 11, fill: '#9ca3af' }}
                      tickLine={false}
                      axisLine={{ stroke: '#e5e7eb' }}
                    />
                    <YAxis 
                      tick={{ fontSize: 11, fill: '#9ca3af' }}
                      tickLine={false}
                      axisLine={{ stroke: '#e5e7eb' }}
                      tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`}
                    />
                    <Tooltip content={<CustomTooltip />} />
                    <Line 
                      type="monotone" 
                      dataKey="total" 
                      stroke="#3b82f6" 
                      strokeWidth={2}
                      dot={{ fill: '#3b82f6', r: 4 }}
                      activeDot={{ r: 6 }}
                    />
                  </LineChart>
                ) : (
                  <BarChart data={analytics.monthlyData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
                    <XAxis 
                      dataKey="month" 
                      tick={{ fontSize: 11, fill: '#9ca3af' }}
                      tickLine={false}
                      axisLine={{ stroke: '#e5e7eb' }}
                    />
                    <YAxis 
                      tick={{ fontSize: 11, fill: '#9ca3af' }}
                      tickLine={false}
                      axisLine={{ stroke: '#e5e7eb' }}
                      tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`}
                    />
                    <Tooltip content={<CustomTooltip />} />
                    <Bar 
                      dataKey="total" 
                      fill="#3b82f6" 
                      radius={[4, 4, 0, 0]}
                    />
                  </BarChart>
                )}
              </ResponsiveContainer>
            </div>

            {/* Category Distribution */}
            {analytics.categoryData.length > 0 && (
              <div className="border border-gray-200 rounded-lg p-6">
                <p className="text-gray-900 font-normal mb-6">Distribución por categoría</p>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={analytics.categoryData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                      outerRadius={100}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {analytics.categoryData.map((entry, index) => (
                        <Cell 
                          key={`cell-${index}`} 
                          fill={CATEGORY_COLORS[entry.name] || COLORS[index % COLORS.length]} 
                        />
                      ))}
                    </Pie>
                    <Tooltip content={<PieTooltip />} />
                  </PieChart>
                </ResponsiveContainer>
                
                {/* Category Legend */}
                <div className="mt-6 space-y-2">
                  {analytics.categoryData.map((category, index) => (
                    <div key={category.name} className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div 
                          className="w-3 h-3 rounded-full" 
                          style={{ backgroundColor: CATEGORY_COLORS[category.name] || COLORS[index % COLORS.length] }}
                        />
                        <span className="text-sm text-gray-600 font-light">{category.name}</span>
                      </div>
                      <span className="text-sm text-gray-900 font-normal">
                        {formatCurrency(category.value)}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        ) : (
          <div className="border border-gray-100 rounded-lg p-12 text-center">
            <PieChartIcon className="w-10 h-10 text-gray-300 mx-auto mb-3" strokeWidth={1.5} />
            <p className="text-gray-400 text-sm font-light">No hay datos suficientes</p>
          </div>
        )}
      </div>
    </div>
  );
}
