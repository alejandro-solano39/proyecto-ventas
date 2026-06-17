export const STAGES = {
  OPEN: { label: 'Abierta', color: 'bg-teal-100 text-teal-700', dot: '#14b8a6' },
  CONTACTED: { label: 'Contactado', color: 'bg-amber-100 text-amber-700', dot: '#f59e0b' },
  PROPOSAL_SENT: { label: 'Propuesta enviada', color: 'bg-sky-100 text-sky-700', dot: '#0ea5e9' },
  NEGOTIATION: { label: 'Negociación', color: 'bg-orange-100 text-orange-700', dot: '#f97316' },
  CLOSED_WON: { label: 'Ganada', color: 'bg-emerald-100 text-emerald-700', dot: '#10b981' },
  CLOSED_LOST: { label: 'Perdida', color: 'bg-rose-100 text-rose-700', dot: '#f43f5e' },
};

export const SENTIMENTS = {
  VERY_INTERESTED: { label: 'Muy interesado', color: 'text-orange-600', icon: 'Flame' },
  INTERESTED: { label: 'Interesado', color: 'text-emerald-600', icon: 'ThumbsUp' },
  NOT_INTERESTED: { label: 'No interesado', color: 'text-rose-600', icon: 'ThumbsDown' },
  FOLLOW_UP: { label: 'Seguimiento', color: 'text-amber-600', icon: 'CalendarClock' },
};

export const formatCurrency = (value) =>
  new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN', maximumFractionDigits: 0 }).format(value);
