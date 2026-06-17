# Backend - Sistema de Ventas Hoteleras

Node.js/Express API corriendo en puerto 3000.

## Setup

```bash
cp .env.example .env
# Edita .env con tus credenciales

npm install
npm run db:seed   # Crea tablas y datos de demo en PostgreSQL
npm start         # http://localhost:3000
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/auth/login | Login, devuelve JWT |
| GET | /api/hotels | Lista hoteles (query: ?city=Guadalajara) |
| POST | /api/visit-report | Sube audio + hotel_id, crea oportunidad |
| GET | /api/opportunities | Lista oportunidades |
| GET | /api/opportunities/:id | Detalle de oportunidad |
| PATCH | /api/opportunities/:id | Actualiza etapa/acción |

## POST /api/visit-report

**Content-Type:** `multipart/form-data`

| Campo | Tipo | Requerido |
|-------|------|-----------|
| audio | File (mp3/wav/m4a) | No (si hay manual_notes) |
| hotel_id | string | Sí |
| manual_notes | string | No (si hay audio) |

**Headers:** `Authorization: Bearer <token>`

## Variables de entorno (.env)

```
PORT=3000
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ventas_hoteleras
DB_USER=postgres
DB_PASSWORD=...
JWT_SECRET=...
ANTHROPIC_API_KEY=sk-ant-...
GOOGLE_APPLICATION_CREDENTIALS=/ruta/credentials.json
```
