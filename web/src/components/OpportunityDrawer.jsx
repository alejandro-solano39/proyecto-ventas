import { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  X, Building2, User, Briefcase, MapPin, Calendar, Quote,
  Package, ArrowRight, Flame, ThumbsUp, ThumbsDown, CalendarClock, Check,
} from 'lucide-react';
import { STAGES, SENTIMENTS, formatCurrency } from '../utils/constants';
import { useOpportunityDetail, useUpdateOpportunity } from '../hooks/useOpportunities';

const SENTIMENT_ICONS = { Flame, ThumbsUp, ThumbsDown, CalendarClock };

function Row({ icon: Icon, label, value }) {
  return (
    <div className="flex items-start gap-3">
      <div className="bg-slate-100 rounded-lg p-1.5 mt-0.5">
        <Icon size={15} className="text-slate-500" />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-slate-400">{label}</p>
        <p className="text-sm font-medium text-slate-800 break-words">{value || '—'}</p>
      </div>
    </div>
  );
}

export default function OpportunityDrawer({ opportunityId, summary, onClose }) {
  const { data, isLoading } = useOpportunityDetail(opportunityId);
  const updateMutation = useUpdateOpportunity();
  const [stage, setStage] = useState(summary?.stage);

  useEffect(() => { setStage(summary?.stage); }, [summary?.stage, opportunityId]);

  const opp = data?.opportunity || summary || {};
  const visit = data?.related_visits?.[0];
  const sentiment = SENTIMENTS[opp.sentiment];
  const SentIcon = sentiment ? SENTIMENT_ICONS[sentiment.icon] : null;
  const transcription = visit?.audio_transcription || visit?.manual_notes;

  const saveStage = () => {
    if (stage && stage !== opp.stage) updateMutation.mutate({ id: opportunityId, stage });
  };

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]"
        onClick={onClose}
      />
      {/* Panel */}
      <aside className="relative w-full max-w-md bg-white h-full shadow-2xl overflow-y-auto animate-[slideIn_0.25s_ease]">
        {/* Header */}
        <div className="sticky top-0 bg-gradient-to-r from-brand-900 to-brand-700 text-white px-6 py-5 z-10">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1.5 rounded-lg hover:bg-white/15 transition-colors"
          >
            <X size={18} />
          </button>
          <div className="flex items-center gap-2 text-emerald-100 text-xs mb-1">
            <Building2 size={14} />
            Oportunidad
          </div>
          <h2 className="text-xl font-bold leading-tight">{opp.hotel_name || '—'}</h2>
          <div className="flex items-center gap-1.5 text-emerald-100 text-sm mt-1">
            <MapPin size={13} /> {opp.city || '—'}
          </div>
        </div>

        <div className="p-6 space-y-6">
          {/* Valor destacado */}
          {opp.estimated_monthly_value > 0 && (
            <div className="bg-gradient-to-br from-brand-50 to-emerald-50 rounded-2xl p-5 border border-brand-100">
              <p className="text-xs text-brand-700 font-medium">Valor potencial mensual</p>
              <p className="text-3xl font-extrabold text-brand-800 mt-1">
                {formatCurrency(opp.estimated_monthly_value)}
              </p>
            </div>
          )}

          {/* Datos clave */}
          <div className="grid grid-cols-1 gap-4">
            <Row icon={User} label="Contacto" value={opp.contact_name} />
            <Row icon={Briefcase} label="Puesto" value={opp.contact_role} />
            {sentiment && (
              <div className="flex items-start gap-3">
                <div className="bg-slate-100 rounded-lg p-1.5 mt-0.5">
                  {SentIcon && <SentIcon size={15} className={sentiment.color} />}
                </div>
                <div>
                  <p className="text-xs text-slate-400">Sentimiento (IA)</p>
                  <p className={`text-sm font-semibold ${sentiment.color}`}>{sentiment.label}</p>
                </div>
              </div>
            )}
            <Row
              icon={Calendar} label="Fecha de registro"
              value={opp.created_at ? format(new Date(opp.created_at), "dd 'de' MMMM yyyy, HH:mm", { locale: es }) : null}
            />
          </div>

          {/* Productos mencionados */}
          {opp.products_mentioned?.length > 0 && (
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Package size={14} /> Productos mencionados
              </p>
              <div className="flex flex-wrap gap-2">
                {opp.products_mentioned.map((p, i) => (
                  <span key={i} className="text-xs bg-slate-100 text-slate-700 px-2.5 py-1 rounded-lg font-medium">
                    {p}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Próxima acción */}
          {opp.next_action && (
            <div className="bg-amber-50 border border-amber-200 rounded-xl p-4">
              <p className="text-xs font-semibold text-amber-700 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                <ArrowRight size={14} /> Próxima acción
              </p>
              <p className="text-sm text-amber-900">{opp.next_action}</p>
            </div>
          )}

          {/* Transcripción */}
          {transcription && (
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Quote size={14} /> Lo que se dijo en la visita
              </p>
              <div className="bg-slate-50 rounded-xl p-4 text-sm text-slate-600 leading-relaxed italic border-l-4 border-brand-300">
                “{transcription}”
              </div>
            </div>
          )}

          {isLoading && !transcription && (
            <p className="text-sm text-slate-400 text-center py-2">Cargando detalle…</p>
          )}

          {/* Cambiar etapa */}
          <div className="border-t border-slate-100 pt-5">
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">
              Etapa del pipeline
            </p>
            <div className="flex gap-2">
              <select
                value={stage || ''}
                onChange={(e) => setStage(e.target.value)}
                className="flex-1 border border-slate-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
              >
                {Object.entries(STAGES).map(([k, { label }]) => (
                  <option key={k} value={k}>{label}</option>
                ))}
              </select>
              <button
                onClick={saveStage}
                disabled={stage === opp.stage || updateMutation.isPending}
                className="inline-flex items-center gap-1.5 bg-brand-700 hover:bg-brand-800 disabled:bg-slate-300 text-white text-sm font-semibold px-4 rounded-xl transition-colors"
              >
                <Check size={15} />
                Guardar
              </button>
            </div>
          </div>
        </div>
      </aside>
    </div>
  );
}
