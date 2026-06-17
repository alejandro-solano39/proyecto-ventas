import { useState } from 'react';
import { GripVertical } from 'lucide-react';
import { useOpportunities, useUpdateOpportunity } from '../hooks/useOpportunities';
import { STAGES, formatCurrency } from '../utils/constants';

const STAGE_ORDER = ['OPEN', 'CONTACTED', 'PROPOSAL_SENT', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST'];

export default function PipelinePage() {
  const { data, isLoading } = useOpportunities({ limit: 200 });
  const updateMutation = useUpdateOpportunity();
  const opps = data?.opportunities || [];
  const [dragId, setDragId] = useState(null);
  const [overStage, setOverStage] = useState(null);

  const byStage = (stage) => opps.filter((o) => o.stage === stage);

  const handleDrop = (stage) => {
    if (dragId) {
      const opp = opps.find((o) => o.id === dragId);
      if (opp && opp.stage !== stage) {
        updateMutation.mutate({ id: dragId, stage });
      }
    }
    setDragId(null);
    setOverStage(null);
  };

  if (isLoading) {
    return <div className="max-w-7xl mx-auto px-4 py-16 text-center text-slate-400">Cargando pipeline…</div>;
  }

  return (
    <div className="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-up">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Pipeline de ventas</h1>
        <p className="text-sm text-slate-500 mt-0.5">
          Arrastra las oportunidades entre etapas para actualizar su estado
        </p>
      </div>

      <div className="flex gap-4 overflow-x-auto pb-4">
        {STAGE_ORDER.map((stage) => {
          const stageOpps = byStage(stage);
          const total = stageOpps.reduce((s, o) => s + parseFloat(o.estimated_monthly_value || 0), 0);
          const { label, dot } = STAGES[stage];
          const isOver = overStage === stage;

          return (
            <div
              key={stage}
              onDragOver={(e) => { e.preventDefault(); setOverStage(stage); }}
              onDragLeave={() => setOverStage(null)}
              onDrop={() => handleDrop(stage)}
              className={`flex-shrink-0 w-72 rounded-2xl p-3 transition-colors ${
                isOver ? 'bg-brand-100' : 'bg-slate-100/70'
              }`}
            >
              {/* Header de columna */}
              <div className="flex items-center justify-between px-1 mb-3">
                <div className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full" style={{ background: dot }} />
                  <span className="font-semibold text-slate-700 text-sm">{label}</span>
                  <span className="text-xs bg-white text-slate-500 px-2 py-0.5 rounded-full font-medium">
                    {stageOpps.length}
                  </span>
                </div>
              </div>
              {total > 0 && (
                <p className="text-xs text-slate-400 px-1 mb-3 font-medium">{formatCurrency(total)}/mes</p>
              )}

              {/* Tarjetas */}
              <div className="space-y-2.5 min-h-[80px]">
                {stageOpps.map((opp) => (
                  <div
                    key={opp.id}
                    draggable
                    onDragStart={() => setDragId(opp.id)}
                    onDragEnd={() => { setDragId(null); setOverStage(null); }}
                    className={`bg-white rounded-xl border border-slate-100 p-3 shadow-card cursor-grab active:cursor-grabbing transition-opacity ${
                      dragId === opp.id ? 'opacity-50' : ''
                    }`}
                  >
                    <div className="flex items-start gap-2">
                      <GripVertical size={15} className="text-slate-300 mt-0.5 shrink-0" />
                      <div className="min-w-0 flex-1">
                        <p className="font-semibold text-slate-900 text-sm truncate">{opp.hotel_name}</p>
                        <p className="text-xs text-slate-400 truncate">
                          {opp.contact_name || 'Sin contacto'}
                          {opp.city && ` · ${opp.city}`}
                        </p>
                        {opp.estimated_monthly_value > 0 && (
                          <p className="text-sm font-bold text-brand-700 mt-1.5">
                            {formatCurrency(opp.estimated_monthly_value)}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
                {stageOpps.length === 0 && (
                  <div className="text-center text-xs text-slate-300 py-6 border-2 border-dashed border-slate-200 rounded-xl">
                    Suelta aquí
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
