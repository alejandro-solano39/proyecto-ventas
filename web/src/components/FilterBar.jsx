import { Search, X, Download } from 'lucide-react';
import { STAGES, SENTIMENTS } from '../utils/constants';

const selectClass =
  'border border-slate-300 rounded-xl px-3.5 py-2 text-sm bg-white text-slate-700 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent';

export default function FilterBar({ filters, onChange, onExport, resultCount }) {
  const set = (key, value) => onChange({ ...filters, [key]: value || undefined });
  const hasFilters = filters.stage || filters.city || filters.sentiment || filters.q;

  return (
    <div className="bg-white rounded-2xl border border-slate-100 shadow-card p-4 mb-5 space-y-3">
      {/* Búsqueda */}
      <div className="flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[220px]">
          <Search size={17} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            value={filters.q || ''}
            onChange={(e) => set('q', e.target.value)}
            placeholder="Buscar por hotel o contacto…"
            className="w-full border border-slate-300 rounded-xl pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
          />
        </div>
        <button
          onClick={onExport}
          className="inline-flex items-center gap-1.5 px-3.5 py-2.5 text-sm font-semibold text-white bg-brand-700 hover:bg-brand-800 rounded-xl transition-colors"
        >
          <Download size={16} />
          Exportar CSV
        </button>
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap items-center gap-3">
        <select value={filters.stage || ''} onChange={(e) => set('stage', e.target.value)} className={selectClass}>
          <option value="">Todas las etapas</option>
          {Object.entries(STAGES).map(([key, { label }]) => (
            <option key={key} value={key}>{label}</option>
          ))}
        </select>

        <select value={filters.city || ''} onChange={(e) => set('city', e.target.value)} className={selectClass}>
          <option value="">Todas las ciudades</option>
          <option value="Guadalajara">Guadalajara</option>
          <option value="CDMX">CDMX</option>
          <option value="Monterrey">Monterrey</option>
        </select>

        <select value={filters.sentiment || ''} onChange={(e) => set('sentiment', e.target.value)} className={selectClass}>
          <option value="">Todo sentimiento</option>
          {Object.entries(SENTIMENTS).map(([key, { label }]) => (
            <option key={key} value={key}>{label}</option>
          ))}
        </select>

        {hasFilters && (
          <button
            onClick={() => onChange({})}
            className="inline-flex items-center gap-1.5 px-3 py-2 text-sm text-slate-600 hover:text-rose-600 border border-slate-300 rounded-xl hover:border-rose-300 hover:bg-rose-50 transition-all"
          >
            <X size={14} />
            Limpiar
          </button>
        )}

        {typeof resultCount === 'number' && (
          <span className="text-sm text-slate-400 ml-auto">{resultCount} resultado{resultCount !== 1 ? 's' : ''}</span>
        )}
      </div>
    </div>
  );
}
