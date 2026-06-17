const express = require('express');
const auth = require('../middleware/auth');
const upload = require('../middleware/upload');
const { submitVisitReport, transcribeOnly } = require('../controllers/visitController');

const router = express.Router();

// Paso 1: solo transcribir (para revisar el texto antes de crear la oportunidad)
router.post('/transcribe', auth, upload.single('audio'), transcribeOnly);

// Paso 2: enviar reporte. 'audio' es opcional; si no hay, usa manual_notes (texto editado)
router.post('/visit-report', auth, upload.single('audio'), submitVisitReport);

module.exports = router;
