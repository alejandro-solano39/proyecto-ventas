import { useState } from 'react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Flame, ThumbsUp, ThumbsDown, CalendarClock, ArrowRight,
  ChevronUp, ChevronDown, Check, X, Pencil, ChevronRight,
} from 'lucide-react';
import { STAGES, SENTIMENTS, formatCurrency } from '../utils/constants';
import { useUpdateOpportunity } from '../hooks/useOpportunities';
import OpportunityDrawer from './OpportunityDrawer';

const SENTIMENT_ICONS = { Flame, ThumbsUp, ThumbsDown, CalendarClock };

function StageBadge({ stage }) {
  const { label, color, dot } = STAGES[stage] || { label: stage, color: 'bg-slate-100 text-slate-700', dot: '#94a3b8' };
  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${color}`}>
      <span className="w-1.5 h-1.5 rounded-full" style={{ background: dot }} />
      {label}
    </span>
  );
}

function SentimentBadge({ sentiment }) {
  const s = SENTIMENTS[sentiment];
  if (!s) return <span className="text-slate-300">—</span>;
  const Icon = SENTIMENT_ICONS[s.icon];
  return (
    <span className={`inline-flex items-center gap-1.5 text-xs font-medium ${s.color}`} title={s.label}>
      <Icon size={15} />
      <span className="hidden lg:inline">{s.label}</span>
    </span>
  );
}

export default function OpportunityTable({ opportunities }) {
  const [sortKey, setSortKey] = useState('created_at');
  const [sortDir, setSortDir] = useState('desc');
  const [editingId, setEditingId] = useState(null);
  const [editStage, setEditStage] = useState('');
  const [selected, setSelected] = useState(null); // oportunidad abierta en el drawer
  const updateMutation = useUpdateOpportunity();

  const handleSort = (key) => {
    if (sortKey === key) setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    else { setSortKey(key); setSortDir('asc'); }
  };

  const sorted = [...(opportunities || [])].sort((a, b) => {
    let av = a[sortKey], bv = b[sortKey];
    if (sortKey === 'estimated_monthly_value') { av = parseFloat(av); bv = parseFloat(bv); }
    if (av < bv) return sortDir === 'asc' ? -1 : 1;
    if (av > bv) return sortDir === 'asc' ? 1 : -1;
    return 0;
  });

  const handleStageUpdate = (id) => {
    updateMutation.mutate({ id, stage: editStage });
    setEditingId(null);
  };

  const th = (label, key) => (
    <th
      onClick={() => handleSort(key)}
      className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider cursor-pointer hover:text-brand-700 select-none"
    >
      <span className="inline-flex items-center gap-1">
        {label}
        {sortKey === key && (sortDir === 'asc' ? <ChevronUp size={13} /> : <ChevronDown size={13} />)}
      </span>
    </th>
  );

  if (!opportunities?.length) {
    return (
      <div className="text-center py-16 text-slate-400 bg-white rounded-2xl border border-slate-100">
        No hay oportunidades que coincidan con los filtros.
      </div>
    );
  }

  return (
    <>
      <div className="overflow-x-auto rounded-2xl border border-slate-100 shadow-card bg-white">
        <table className="min-w-full divide-y divide-slate-100">
          <thead className="bg-slate-50/80">
            <tr>
              {th('Hotel', 'hotel_name')}
              {th('Contacto', 'contact_name')}
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Puesto</th>
              {th('Monto/mes', 'estimated_monthly_value')}
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Sentimiento</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Etapa</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Próxima acción</th>
              {th('Fecha', 'created_at')}
              <th className="px-2 py-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-50">
            {sorted.map((opp) => (
              <tr
                key={opp.id}
                onClick={() => setSelected(opp)}
                className="hover:bg-brand-50/40 transition-colors cursor-pointer group"
              >
                <td className="px-4 py-3">
                  <div className="font-semibold text-slate-900 text-sm group-hover:text-brand-700 transition-colors">{opp.hotel_name}</div>
                  <div className="text-xs text-slate-400">{opp.city}</div>
                </td>
                <td className="px-4 py-3 text-sm text-slate-700 font-medium">{opp.contact_name || '—'}</td>
                <td className="px-4 py-3 text-sm text-slate-500">{opp.contact_role || '—'}</td>
                <td className="px-4 py-3 text-sm font-bold text-brand-700 whitespace-nowrap">
                  {opp.estimated_monthly_value
                    ? formatCurrency(opp.estimated_monthly_value)
                    : <span className="text-slate-300 font-normal">—</span>}
                </td>
                <td className="px-4 py-3">
                  <SentimentBadge sentiment={opp.sentiment} />
                </td>
                <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                  {editingId === opp.id ? (
                    <div className="flex gap-1 items-center">
                      <select
                        value={editStage}
                        onChange={(e) => setEditStage(e.target.value)}
                        className="text-xs border border-slate-300 rounded-lg px-2 py-1 focus:outline-none focus:ring-2 focus:ring-brand-500"
                      >
                        {Object.entries(STAGES).map(([k, { label }]) => (
                          <option key={k} value={k}>{label}</option>
                        ))}
                      </select>
                      <button
                        onClick={() => handleStageUpdate(opp.id)}
                        className="p-1 text-emerald-600 hover:bg-emerald-50 rounded"
                        title="Guardar"
                      >
                        <Check size={15} />
                      </button>
                      <button
                        onClick={() => setEditingId(null)}
                        className="p-1 text-slate-400 hover:bg-slate-100 rounded"
                        title="Cancelar"
                      >
                        <X size={15} />
                      </button>
                    </div>
                  ) : (
                    <button
                      onClick={() => { setEditingId(opp.id); setEditStage(opp.stage); }}
                      className="group/stage inline-flex items-center gap-1.5 hover:opacity-80 transition-opacity"
                      title="Cambiar etapa"
                    >
                      <StageBadge stage={opp.stage} />
                      <Pencil size={12} className="text-slate-300 group-hover/stage:text-slate-500" />
                    </button>
                  )}
                </td>
                <td className="px-4 py-3 text-xs text-slate-600 max-w-[180px]">
                  {opp.next_action ? (
                    <span className="inline-flex items-center gap-1.5">
                      <ArrowRight size={13} className="text-brand-500 shrink-0" />
                      <span className="truncate" title={opp.next_action}>{opp.next_action}</span>
                    </span>
                  ) : '—'}
                </td>
                <td className="px-4 py-3 text-xs text-slate-400 whitespace-nowrap">
                  {format(new Date(opp.created_at), 'dd MMM yyyy HH:mm', { locale: es })}
                </td>
                <td className="px-2 py-3">
                  <ChevronRight size={16} className="text-slate-300 group-hover:text-brand-500 transition-colors" />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selected && (
        <OpportunityDrawer
          opportunityId={selected.id}
          summary={selected}
          onClose={() => setSelected(null)}
        />
      )}
    </>
  );
}
