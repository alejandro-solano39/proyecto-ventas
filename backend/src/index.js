require('dotenv').config();
const express = require('express');
const cors = require('cors');
const logger = require('./utils/logger');
const errorHandler = require('./middleware/errorHandler');
const { initDb } = require('./db/connection');

const authRoutes = require('./routes/auth');
const hotelRoutes = require('./routes/hotels');
const visitRoutes = require('./routes/visits');
const opportunityRoutes = require('./routes/opportunities');

const app = express();
const PORT = process.env.PORT || 3000;

const allowedOrigins = (process.env.ALLOWED_ORIGINS || 'http://localhost:5173').split(',');
app.use(cors({
  origin: (origin, cb) => {
    if (!origin || allowedOrigins.includes(origin)) return cb(null, true);
    cb(new Error(`Origen no permitido: ${origin}`));
  },
  credentials: true,
}));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.get('/health', (req, res) => res.json({ status: 'ok', timestamp: new Date().toISOString() }));

app.use('/api/auth', authRoutes);
app.use('/api/hotels', hotelRoutes);
app.use('/api', visitRoutes);
app.use('/api/opportunities', opportunityRoutes);

app.use((req, res) => res.status(404).json({ error: 'Ruta no encontrada' }));
app.use(errorHandler);

async function start() {
  await initDb();
  logger.info('✅ Base de datos inicializada');
  app.listen(PORT, () => {
    logger.info(`🚀 Backend corriendo en http://localhost:${PORT}`);
    logger.info(`   POST /api/auth/login`);
    logger.info(`   GET  /api/hotels`);
    logger.info(`   POST /api/visit-report`);
    logger.info(`   GET  /api/opportunities`);
    logger.info(`   PATCH /api/opportunities/:id`);
  });
}

start().catch(err => {
  logger.error('Error al arrancar el servidor:', err);
  process.exit(1);
});

module.exports = app;
