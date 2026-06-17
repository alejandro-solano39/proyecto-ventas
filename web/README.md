# Web - Dashboard de Ventas Hoteleras

React + Vite corriendo en puerto 5173.

## Setup

```bash
npm install
npm run dev   # http://localhost:5173
```

## Páginas

- `/login` — Login con JWT
- `/dashboard` — KPIs + gráficas + últimas oportunidades
- `/opportunities` — Tabla completa con filtros, sorting y edición de etapa inline

## Features
- Polling automático cada 5 segundos (TanStack Query)
- Filtros por etapa y ciudad
- Ordenamiento por cualquier columna
- Cambio de etapa inline en la tabla
- Indicador "Live" de actualización en tiempo real
