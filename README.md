# Sistema de Ventas Hoteleras

Demo: Android + Backend Node.js + Web React

## Estructura

```
proyecto-ventas/
├─ backend/    → Node.js/Express (puerto 3000)
├─ web/        → React + Vite  (puerto 5173)
└─ app/        → Android Studio (emulador en puerto 8000)
```

## Setup Rápido

### 1. Backend
```bash
cd backend
cp .env.example .env
# Edita .env con tus API keys
npm install
npm start
```

### 2. Web
```bash
cd web
npm install
npm run dev
```

### 3. Android
- Abre carpeta `app/` en Android Studio
- Conecta emulador o celular
- Run app

## Credenciales Demo
- `admin@ventas.com` / `demo123`
- `vendedor@ventas.com` / `demo123`

## APIs Requeridas
- **Anthropic Claude API** → https://console.anthropic.com
- **Google Cloud Speech-to-Text** → https://console.cloud.google.com
- **PostgreSQL** → local o remoto
