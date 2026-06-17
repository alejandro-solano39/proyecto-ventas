import { Link } from 'react-router-dom';
import { useOpportunities } from '../hooks/useOpportunities';
import { useAuth } from '../hooks/useAuth';
import Dashboard from '../components/Dashboard';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Building2, Clock, Inbox, Radio, DollarSign, Columns3, Target, ArrowRight,
} from 'lucide-react';
import { STAGES, formatCurrency } from '../utils/constants';

function greeting() {
  const h = new Date().getHours();
  if (h < 12) return 'Buenos días';
  if (h < 19) return 'Buenas tardes';
  return 'Buenas noches';
}

function QuickAction({ to, icon: Icon, title, subtitle }) {
  return (
    <Link
      to={to}
      className="group bg-white rounded-2xl border border-slate-100 p-4 shadow-card hover:shadow-card-hover transition-all flex items-center gap-3"
    >
      <div className="bg-brand-50 rounded-xl p-2.5">
        <Icon size={20} className="text-brand-600" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="font-semibold text-slate-900 text-sm">{title}</p>
        <p className="text-xs text-slate-500 truncate">{subtitle}</p>
      </div>
      <ArrowRight size={16} className="text-slate-300 group-hover:text-brand-500 transition-colors shrink-0" />
    </Link>
  );
}

export default function DashboardPage() {
  const { data, isLoading } = useOpportunities({ limit: 200 });
  const { user } = useAuth();
  const opportunities = data?.opportunities || [];

  const totalValue = opportunities.reduce((s, o) => s + parseFloat(o.estimated_monthly_value || 0), 0);

  const recent = [...opportunities]
    .sort((a, b) => new Date(b.created_at) - new Date(a.created_at))
    .slice(0, 6);

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-16 text-center text-slate-400">
        Cargando dashboard…
      </div>
    );
  }

  const firstName = (user?.name || '').split(' ')[0];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Hero con saludo + valor total */}
      <div className="rounded-3xl bg-gradient-to-br from-brand-900 via-brand-700 to-brand-600 p-7 text-white animate-fade-up">
        <div className="flex items-start justify-between flex-wrap gap-3">
          <div>
            <p className="text-emerald-100">{greeting()}{firstName ? ',' : ''}</p>
            <h1 className="text-2xl font-bold">{user?.name || 'Vendedor'} 👋</h1>
            <p className="text-emerald-100 text-sm mt-1 capitalize">
              {format(new Date(), "EEEE, dd 'de' MMMM yyyy", { locale: es })}
            </p>
          </div>
          <span className="inline-flex items-center gap-1.5 text-xs font-medium text-white bg-white/15 px-3 py-1.5 rounded-full">
            <Radio size={13} className="animate-pulse" />
            Datos en vivo
          </span>
        </div>
        <div className="mt-5 inline-flex items-center gap-3 bg-white/15 rounded-2xl px-5 py-3">
          <DollarSign size={26} className="text-gold-400" />
          <div>
            <p className="text-emerald-100 text-xs">Valor potencial total</p>
            <p className="text-2xl font-extrabold">
              {formatCurrency(totalValue)}<span className="text-sm font-medium text-emerald-100">/mes</span>
            </p>
          </div>
        </div>
      </div>

      {/* Accesos rápidos */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <QuickAction to="/opportunities" icon={Target} title="Oportunidades" subtitle="Lista y detalle de ventas" />
        <QuickAction to="/pipeline" icon={Columns3} title="Pipeline" subtitle="Tablero de etapas" />
        <QuickAction to="/hotels" icon={Building2} title="Hoteles" subtitle="Cartera de prospectos" />
      </div>

      {opportunities.length === 0 ? (
        <div className="bg-white rounded-2xl border border-slate-100 shadow-card py-20 text-center animate-fade-up">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-brand-50 mb-4">
            <Inbox size={32} className="text-brand-400" />
          </div>
          <p className="text-slate-700 font-semibold text-lg">Aún no hay oportunidades</p>
          <p className="text-slate-400 text-sm mt-1 max-w-sm mx-auto">
            Registra tu primera visita desde la app Android. Las oportunidades aparecerán aquí
            automáticamente en cuanto se procesen con IA.
          </p>
        </div>
      ) : (
        <div className="space-y-8 animate-fade-up" style={{ animationDelay: '0.05s' }}>
          <Dashboard opportunities={opportunities} />

          {/* Últimas oportunidades */}
          <div>
            <h2 className="text-lg font-semibold text-slate-800 mb-3 flex items-center gap-2">
              <Clock size={18} className="text-brand-600" />
              Últimas oportunidades
            </h2>
            <div className="space-y-2.5">
              {recent.map((opp) => (
                <div
                  key={opp.id}
                  className="bg-white border border-slate-100 rounded-xl p-4 flex items-center justify-between shadow-card hover:shadow-card-hover transition-shadow"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="bg-brand-50 rounded-lg p-2 shrink-0">
                      <Building2 size={18} className="text-brand-600" />
                    </div>
                    <div className="min-w-0">
                      <div className="font-semibold text-slate-900 truncate">
                        {opp.hotel_name}
                        {opp.city && (
                          <span className="text-slate-400 font-normal text-sm"> · {opp.city}</span>
                        )}
                      </div>
                      <div className="text-sm text-slate-500 truncate">
                        {opp.contact_name || 'Sin contacto'}
                        {opp.contact_role && (
                          <span className="text-slate-400"> · {opp.contact_role}</span>
                        )}
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    {opp.estimated_monthly_value > 0 && (
                      <span className="text-brand-700 font-bold text-sm hidden sm:inline">
                        {formatCurrency(opp.estimated_monthly_value)}/mes
                      </span>
                    )}
                    <span
                      className={`text-xs px-2.5 py-1 rounded-full font-semibold ${STAGES[opp.stage]?.color || 'bg-slate-100 text-slate-600'}`}
                    >
                      {STAGES[opp.stage]?.label || opp.stage}
                    </span>
                    <span className="text-xs text-slate-400 whitespace-nowrap hidden md:inline">
                      {format(new Date(opp.created_at), 'dd MMM HH:mm', { locale: es })}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
