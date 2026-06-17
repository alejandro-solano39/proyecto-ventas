import { useState, useMemo } from 'react';
import { Building2, Star, Users, Search, Target } from 'lucide-react';
import { useHotels } from '../hooks/useHotels';
import { useOpportunities } from '../hooks/useOpportunities';
import { formatCurrency } from '../utils/constants';

const CITIES = ['Todas', 'Guadalajara', 'CDMX', 'Monterrey'];

function Stars({ n }) {
  return (
    <span className="inline-flex">
      {Array.from({ length: n }).map((_, i) => (
        <Star key={i} size={13} className="text-gold-500 fill-gold-500" />
      ))}
    </span>
  );
}

export default function HotelsPage() {
  const [city, setCity] = useState('Todas');
  const [search, setSearch] = useState('');
  const { data: hotels, isLoading } = useHotels(city === 'Todas' ? undefined : city);
  const { data: oppData } = useOpportunities({ limit: 200 });
  const opps = oppData?.opportunities || [];

  // Mapa hotel_id -> { count, value }
  const oppByHotel = useMemo(() => {
    const m = {};
    opps.forEach((o) => {
      const k = o.hotel_id;
      if (!m[k]) m[k] = { count: 0, value: 0 };
      m[k].count += 1;
      m[k].value += parseFloat(o.estimated_monthly_value || 0);
    });
    return m;
  }, [opps]);

  const filtered = (hotels || []).filter((h) =>
    h.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-up">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Hoteles</h1>
        <p className="text-sm text-slate-500 mt-0.5">Cartera de prospectos · ventas tocando puertas</p>
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap items-center gap-3 mb-5">
        <div className="relative flex-1 min-w-[220px]">
          <Search size={17} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar hotel…"
            className="w-full border border-slate-300 rounded-xl pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
          />
        </div>
        <div className="flex gap-2">
          {CITIES.map((c) => (
            <button
              key={c}
              onClick={() => setCity(c)}
              className={`px-3.5 py-2 rounded-xl text-sm font-medium transition-all ${
                city === c
                  ? 'bg-brand-700 text-white'
                  : 'bg-white border border-slate-300 text-slate-600 hover:border-brand-400'
              }`}
            >
              {c}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? (
        <div className="text-center py-16 text-slate-400">Cargando hoteles…</div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((h) => {
            const stats = oppByHotel[h.id];
            return (
              <div key={h.id} className="bg-white rounded-2xl border border-slate-100 p-5 shadow-card hover:shadow-card-hover transition-all">
                <div className="flex items-start justify-between">
                  <div className="bg-brand-50 rounded-xl p-2.5">
                    <Building2 size={22} className="text-brand-600" />
                  </div>
                  {h.stars >= 4 && (
                    <span className="text-xs font-semibold bg-gold-500/10 text-gold-600 px-2 py-1 rounded-full">
                      Premium
                    </span>
                  )}
                </div>
                <h3 className="font-bold text-slate-900 mt-3">{h.name}</h3>
                <p className="text-sm text-slate-400">{h.city}</p>
                <div className="flex items-center gap-3 mt-2">
                  <Stars n={h.stars} />
                  <span className="inline-flex items-center gap-1 text-xs text-slate-500">
                    <Users size={13} /> {h.avg_monthly_guests}/mes
                  </span>
                </div>

                <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between">
                  <span className="inline-flex items-center gap-1.5 text-sm text-slate-600">
                    <Target size={15} className="text-brand-600" />
                    {stats ? `${stats.count} oportunidad${stats.count > 1 ? 'es' : ''}` : 'Sin oportunidades'}
                  </span>
                  {stats?.value > 0 && (
                    <span className="text-sm font-bold text-brand-700">{formatCurrency(stats.value)}</span>
                  )}
                </div>
              </div>
            );
          })}
          {filtered.length === 0 && (
            <p className="text-slate-400 text-sm col-span-full text-center py-12">
              No se encontraron hoteles.
            </p>
          )}
        </div>
      )}
    </div>
  );
}
