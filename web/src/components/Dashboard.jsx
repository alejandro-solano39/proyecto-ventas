import {
  AreaChart, Area, BarChart, Bar, XAxis, YAxis, Tooltip,
  ResponsiveContainer, CartesianGrid, Cell, PieChart, Pie,
} from 'recharts';
import {
  Target, DollarSign, TrendingUp, TrendingDown, Receipt,
  Building2, ArrowUpRight,
} from 'lucide-react';
import { formatCurrency, STAGES, SENTIMENTS } from '../utils/constants';
import {
  computeKpis, funnelData, sentimentData, topHotels,
} from '../utils/analytics';

const SENTIMENT_COLORS = {
  VERY_INTERESTED: '#ea580c',
  INTERESTED: '#10b981',
  FOLLOW_UP: '#f59e0b',
  NOT_INTERESTED: '#f43f5e',
};

/* ---------- KPI con sparkline y delta ---------- */
function KpiCard({ icon: Icon, label, value, delta, series, accent, iconBg, sparkColor }) {
  const positive = delta >= 0;
  return (
    <div className="bg-white rounded-2xl border border-slate-100 p-5 shadow-card hover:shadow-card-hover transition-all hover:-translate-y-0.5">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-slate-500">{label}</p>
        <div className={`rounded-xl p-2 ${iconBg}`}>
          <Icon size={18} className={accent} />
        </div>
      </div>
      <p className={`text-2xl font-extrabold mt-2 ${accent}`}>{value}</p>

      <div className="flex items-end justify-between mt-1 gap-2">
        {typeof delta === 'number' && (
          <span
            className={`inline-flex items-center gap-0.5 text-xs font-semibold ${
              positive ? 'text-emerald-600' : 'text-rose-600'
            }`}
          >
            {positive ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
            {positive ? '+' : ''}{delta}%
          </span>
        )}
        {series && (
          <div className="h-9 flex-1 max-w-[110px]">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={series} margin={{ top: 2, bottom: 2, left: 0, right: 0 }}>
                <defs>
                  <linearGradient id={`spark-${label}`} x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor={sparkColor} stopOpacity={0.35} />
                    <stop offset="100%" stopColor={sparkColor} stopOpacity={0} />
                  </linearGradient>
                </defs>
                <Area
                  type="monotone" dataKey="value" stroke={sparkColor}
                  strokeWidth={2} fill={`url(#spark-${label})`} dot={false}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    </div>
  );
}

/* ---------- Embudo de ventas ---------- */
function Funnel({ data }) {
  const max = Math.max(...data.map((d) => d.value), 1);
  return (
    <div className="space-y-2.5">
      {data.map((d) => {
        const pct = (d.value / max) * 100;
        return (
          <div key={d.key} className="flex items-center gap-3">
            <span className="w-32 text-xs text-slate-500 text-right shrink-0">{d.label}</span>
            <div className="flex-1 bg-slate-50 rounded-lg h-8 relative overflow-hidden">
              <div
                className="h-full rounded-lg flex items-center px-3 transition-all duration-700"
                style={{ width: `${Math.max(pct, 8)}%`, background: d.color }}
              >
                <span className="text-xs font-bold text-white">{d.value}</span>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

export default function Dashboard({ opportunities }) {
  if (!opportunities?.length) return null;

  const k = computeKpis(opportunities);
  const funnel = funnelData(opportunities);
  const sentiments = sentimentData(opportunities);
  const top = topHotels(opportunities);

  return (
    <div className="space-y-6">
      {/* KPI cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCard
          icon={DollarSign} label="Valor potencial / mes"
          value={formatCurrency(k.totalValue)} delta={k.valueDelta} series={k.series}
          accent="text-brand-700" iconBg="bg-brand-50" sparkColor="#059669"
        />
        <KpiCard
          icon={Target} label="Oportunidades"
          value={k.total} delta={k.countDelta} series={k.series}
          accent="text-slate-900" iconBg="bg-slate-100" sparkColor="#64748b"
        />
        <KpiCard
          icon={Receipt} label="Ticket promedio"
          value={formatCurrency(k.avgTicket)}
          accent="text-gold-600" iconBg="bg-amber-50" sparkColor="#f59e0b"
        />
        <KpiCard
          icon={Building2} label="Hoteles activos"
          value={k.activeHotels}
          accent="text-emerald-600" iconBg="bg-emerald-50" sparkColor="#10b981"
        />
      </div>

      {/* Tendencia (ancha) */}
      <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-card">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-sm font-semibold text-slate-700">Tendencia de valor potencial</h3>
            <p className="text-xs text-slate-400">Últimos 14 días · MXN/mes generados</p>
          </div>
          <span className={`inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full ${
            k.valueDelta >= 0 ? 'text-emerald-700 bg-emerald-50' : 'text-rose-700 bg-rose-50'
          }`}>
            <ArrowUpRight size={13} />
            {k.valueDelta >= 0 ? '+' : ''}{k.valueDelta}% vs. periodo previo
          </span>
        </div>
        <ResponsiveContainer width="100%" height={220}>
          <AreaChart data={k.series} margin={{ top: 5, right: 8, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="trendFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#059669" stopOpacity={0.25} />
                <stop offset="100%" stopColor="#059669" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
            <XAxis dataKey="label" tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} interval="preserveStartEnd" />
            <YAxis tickFormatter={(v) => `$${(v / 1000).toFixed(0)}k`} tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} />
            <Tooltip
              formatter={(v) => [formatCurrency(v), 'Valor']}
              contentStyle={{ borderRadius: 12, border: '1px solid #e2e8f0', fontSize: 13 }}
            />
            <Area type="monotone" dataKey="value" stroke="#059669" strokeWidth={2.5} fill="url(#trendFill)" dot={false} />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* Embudo + Sentimiento */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        <div className="lg:col-span-3 bg-white rounded-2xl border border-slate-100 p-6 shadow-card">
          <h3 className="text-sm font-semibold text-slate-700 mb-1">Embudo de ventas</h3>
          <p className="text-xs text-slate-400 mb-5">Oportunidades por etapa del pipeline</p>
          <Funnel data={funnel} />
        </div>

        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-100 p-6 shadow-card">
          <h3 className="text-sm font-semibold text-slate-700 mb-1">Sentimiento del cliente</h3>
          <p className="text-xs text-slate-400 mb-4">Detectado por IA</p>
          {sentiments.length ? (
            <>
              <ResponsiveContainer width="100%" height={170}>
                <PieChart>
                  <Pie data={sentiments} dataKey="value" nameKey="key" cx="50%" cy="50%" innerRadius={45} outerRadius={75} paddingAngle={3}>
                    {sentiments.map((d) => <Cell key={d.key} fill={SENTIMENT_COLORS[d.key]} />)}
                  </Pie>
                  <Tooltip formatter={(v, _n, p) => [v, SENTIMENTS[p.payload.key]?.label]} />
                </PieChart>
              </ResponsiveContainer>
              <div className="flex flex-wrap gap-x-4 gap-y-1.5 mt-3 justify-center">
                {sentiments.map((d) => (
                  <span key={d.key} className="flex items-center gap-1.5 text-xs text-slate-600">
                    <span className="w-2.5 h-2.5 rounded-full" style={{ background: SENTIMENT_COLORS[d.key] }} />
                    {SENTIMENTS[d.key]?.label} <span className="font-semibold text-slate-800">({d.value})</span>
                  </span>
                ))}
              </div>
            </>
          ) : (
            <p className="text-sm text-slate-400 text-center py-10">Sin datos de sentimiento aún</p>
          )}
        </div>
      </div>

      {/* Top hoteles */}
      <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-card">
        <h3 className="text-sm font-semibold text-slate-700 mb-1">Top hoteles por valor potencial</h3>
        <p className="text-xs text-slate-400 mb-5">Mayor oportunidad de cierre</p>
        <ResponsiveContainer width="100%" height={Math.max(top.length * 48, 100)}>
          <BarChart data={top} layout="vertical" margin={{ top: 0, right: 16, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f1f5f9" />
            <XAxis type="number" tickFormatter={(v) => `$${(v / 1000).toFixed(0)}k`} tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} />
            <YAxis type="category" dataKey="name" width={150} tick={{ fontSize: 12, fill: '#475569' }} axisLine={false} tickLine={false} />
            <Tooltip formatter={(v) => [formatCurrency(v), 'Valor/mes']} cursor={{ fill: '#eef2ff' }} contentStyle={{ borderRadius: 12, border: '1px solid #e2e8f0', fontSize: 13 }} />
            <Bar dataKey="value" radius={[0, 6, 6, 0]} maxBarSize={26}>
              {top.map((_, i) => <Cell key={i} fill={i === 0 ? '#047857' : '#34d399'} />)}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
