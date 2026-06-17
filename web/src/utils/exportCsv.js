import { STAGES, SENTIMENTS } from './constants';

/** Exporta una lista de oportunidades a un archivo CSV descargable. */
export function exportOpportunitiesCsv(opportunities) {
  const headers = [
    'Hotel', 'Ciudad', 'Contacto', 'Puesto', 'Productos',
    'Sentimiento', 'Valor mensual (MXN)', 'Etapa', 'Próxima acción', 'Fecha',
  ];

  const rows = opportunities.map((o) => [
    o.hotel_name ?? '',
    o.city ?? '',
    o.contact_name ?? '',
    o.contact_role ?? '',
    Array.isArray(o.products_mentioned) ? o.products_mentioned.join('; ') : '',
    SENTIMENTS[o.sentiment]?.label ?? o.sentiment ?? '',
    o.estimated_monthly_value ?? 0,
    STAGES[o.stage]?.label ?? o.stage ?? '',
    o.next_action ?? '',
    o.created_at ?? '',
  ]);

  const escape = (v) => {
    const s = String(v).replace(/"/g, '""');
    return /[",\n]/.test(s) ? `"${s}"` : s;
  };

  const csv = [headers, ...rows].map((r) => r.map(escape).join(',')).join('\n');
  // BOM para que Excel respete los acentos UTF-8
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `oportunidades_${new Date().toISOString().slice(0, 10)}.csv`;
  link.click();
  URL.revokeObjectURL(url);
}
