import { STAGES } from './constants';

const num = (v) => parseFloat(v || 0) || 0;
const dayKey = (d) => new Date(d).toISOString().slice(0, 10);

/** Devuelve los últimos N días como array de claves ISO (yyyy-mm-dd), de más antiguo a hoy. */
function lastNDays(n) {
  const days = [];
  const today = new Date();
  for (let i = n - 1; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i);
    days.push(dayKey(d));
  }
  return days;
}

/** Cuenta y suma de valor agrupados por día (para sparklines y tendencia). */
export function dailySeries(opportunities, n = 14) {
  const days = lastNDays(n);
  const countByDay = Object.fromEntries(days.map((d) => [d, 0]));
  const valueByDay = Object.fromEntries(days.map((d) => [d, 0]));

  opportunities.forEach((o) => {
    const k = dayKey(o.created_at);
    if (k in countByDay) {
      countByDay[k] += 1;
      valueByDay[k] += num(o.estimated_monthly_value);
    }
  });

  return days.map((d) => ({
    day: d,
    label: new Date(d).toLocaleDateString('es-MX', { day: '2-digit', month: 'short' }),
    count: countByDay[d],
    value: valueByDay[d],
  }));
}

/** % de cambio comparando la suma de la primera mitad vs la segunda mitad de la serie. */
export function trendDelta(series, field = 'value') {
  if (!series.length) return 0;
  const mid = Math.floor(series.length / 2);
  const prev = series.slice(0, mid).reduce((s, d) => s + d[field], 0);
  const curr = series.slice(mid).reduce((s, d) => s + d[field], 0);
  if (prev === 0) return curr > 0 ? 100 : 0;
  return Math.round(((curr - prev) / prev) * 100);
}

/** Métricas principales para los KPIs. */
export function computeKpis(opportunities) {
  const total = opportunities.length;
  const totalValue = opportunities.reduce((s, o) => s + num(o.estimated_monthly_value), 0);
  const withValue = opportunities.filter((o) => num(o.estimated_monthly_value) > 0);
  const avgTicket = withValue.length ? totalValue / withValue.length : 0;
  const won = opportunities.filter((o) => o.stage === 'CLOSED_WON').length;
  const conversion = total ? Math.round((won / total) * 100) : 0;
  const activeHotels = new Set(opportunities.map((o) => o.hotel_name).filter(Boolean)).size;

  const series = dailySeries(opportunities, 14);
  return {
    total,
    totalValue,
    avgTicket,
    won,
    conversion,
    activeHotels,
    series,
    valueDelta: trendDelta(series, 'value'),
    countDelta: trendDelta(series, 'count'),
  };
}

/** Embudo de ventas: cuenta por etapa en orden de pipeline. */
export function funnelData(opportunities) {
  return Object.entries(STAGES)
    .filter(([key]) => key !== 'CLOSED_LOST')
    .map(([key, { label, dot }]) => ({
      key,
      label,
      color: dot,
      value: opportunities.filter((o) => o.stage === key).length,
    }));
}

/** Distribución por sentimiento. */
export function sentimentData(opportunities) {
  const order = ['VERY_INTERESTED', 'INTERESTED', 'FOLLOW_UP', 'NOT_INTERESTED'];
  return order.map((key) => ({
    key,
    value: opportunities.filter((o) => o.sentiment === key).length,
  })).filter((d) => d.value > 0);
}

/** Top hoteles por valor potencial. */
export function topHotels(opportunities, limit = 5) {
  const map = {};
  opportunities.forEach((o) => {
    const name = o.hotel_name || 'Sin nombre';
    if (!map[name]) map[name] = { name, city: o.city, value: 0, count: 0 };
    map[name].value += num(o.estimated_monthly_value);
    map[name].count += 1;
  });
  return Object.values(map)
    .sort((a, b) => b.value - a.value)
    .slice(0, limit);
}
