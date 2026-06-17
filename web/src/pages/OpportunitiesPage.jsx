import { useState, useMemo } from 'react';
import { Radio } from 'lucide-react';
import { useOpportunities } from '../hooks/useOpportunities';
import OpportunityTable from '../components/OpportunityTable';
import FilterBar from '../components/FilterBar';
import { formatCurrency } from '../utils/constants';
import { exportOpportunitiesCsv } from '../utils/exportCsv';

export default function OpportunitiesPage() {
  const [filters, setFilters] = useState({});
  // stage y city van al backend; q y sentiment se filtran en cliente
  const { stage, city } = filters;
  const { data, isLoading, error, isFetching } = useOpportunities({ stage, city });

  const filtered = useMemo(() => {
    let list = data?.opportunities || [];
    if (filters.sentiment) list = list.filter((o) => o.sentiment === filters.sentiment);
    if (filters.q) {
      const q = filters.q.toLowerCase();
      list = list.filter(
        (o) =>
          (o.hotel_name || '').toLowerCase().includes(q) ||
          (o.contact_name || '').toLowerCase().includes(q)
      );
    }
    return list;
  }, [data, filters.sentiment, filters.q]);

  const totalValue = filtered.reduce((s, o) => s + parseFloat(o.estimated_monthly_value || 0), 0);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-up">
      <div className="flex items-start justify-between mb-6 gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Oportunidades</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            <span className="font-semibold text-slate-700">{filtered.length}</span> oportunidades
            {filtered.length > 0 && (
              <>
                {' · '}Valor total:{' '}
                <span className="text-brand-700 font-semibold">{formatCurrency(totalValue)}/mes</span>
              </>
            )}
            {isFetching && <span className="ml-2 text-slate-400">· actualizando…</span>}
          </p>
        </div>
        <span className="inline-flex items-center gap-1.5 text-xs font-medium text-emerald-700 bg-emerald-50 px-3 py-1.5 rounded-full border border-emerald-200">
          <Radio size={13} className="animate-pulse" />
          En vivo · 5s
        </span>
      </div>

      <FilterBar
        filters={filters}
        onChange={setFilters}
        onExport={() => exportOpportunitiesCsv(filtered)}
        resultCount={filtered.length}
      />

      {isLoading && <div className="text-center py-16 text-slate-400">Cargando oportunidades…</div>}

      {error && (
        <div className="bg-rose-50 border border-rose-200 rounded-xl p-4 text-rose-700 text-sm">
          Error al cargar oportunidades: {error.message}
        </div>
      )}

      {!isLoading && !error && <OpportunityTable opportunities={filtered} />}
    </div>
  );
}
