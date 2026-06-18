const fs = require('fs');
const { v4: uuidv4 } = require('uuid');
const { query, queryOne } = require('../db/connection');
const { transcribeAudio } = require('../services/transcriptionService');
const { extractVisitInfo } = require('../services/claudeService');
const logger = require('../utils/logger');

// Paso 1: solo transcribe el audio y devuelve el texto para que el usuario lo revise/edite.
async function transcribeOnly(req, res, next) {
  const audioPath = req.file?.path;
  try {
    if (!req.file) return res.status(400).json({ error: 'Se requiere un archivo de audio' });
    logger.info(`Transcribiendo audio (solo texto): ${req.file.filename}`);
    const transcription = await transcribeAudio(audioPath);
    res.json({ transcription });
  } catch (err) {
    next(err);
  } finally {
    if (audioPath && fs.existsSync(audioPath)) fs.unlinkSync(audioPath);
  }
}

async function submitVisitReport(req, res, next) {
  const audioPath = req.file?.path;
  try {
    const { hotel_id, manual_notes } = req.body;
    if (!hotel_id) return res.status(400).json({ error: 'hotel_id es requerido' });
    if (!req.file && !manual_notes) {
      return res.status(400).json({ error: 'Se requiere audio o notas manuales' });
    }

    const hotel = await queryOne('SELECT id, name FROM hotels WHERE id = ?', [hotel_id]);
    if (!hotel) return res.status(404).json({ error: 'Hotel no encontrado' });

    let transcription = manual_notes || '';
    if (req.file) {
      logger.info(`Transcribiendo audio: ${req.file.filename}`);
      transcription = await transcribeAudio(audioPath);
    }

    logger.info('Enviando a Groq para extracción de entidades');
    const extracted = await extractVisitInfo(transcription);

    const visitId = uuidv4();
    await query(
      'INSERT INTO visits (id, hotel_id, user_id, audio_transcription, manual_notes) VALUES (?,?,?,?,?)',
      [visitId, hotel_id, req.user.id, transcription, manual_notes || null]
    );

    const oppId = uuidv4();
    await query(
      `INSERT INTO opportunities
        (id, hotel_id, visit_id, user_id, contact_name, contact_role,
         products_mentioned, sentiment, estimated_monthly_value, stage, next_action)
       VALUES (?,?,?,?,?,?,?,?,?,'OPEN',?)`,
      [
        oppId, hotel_id, visitId, req.user.id,
        extracted.contact_name,
        extracted.contact_role,
        JSON.stringify(extracted.products_mentioned),
        extracted.sentiment,
        extracted.estimated_monthly_value,
        extracted.next_action,
      ]
    );

    const opportunity = await queryOne('SELECT * FROM opportunities WHERE id = ?', [oppId]);
    logger.info(`Oportunidad creada: ${oppId}`);

    res.status(201).json({
      opportunity_id: opportunity.id,
      hotel_id: opportunity.hotel_id,
      transcription, // texto que detectó la IA, para mostrarlo en la app
      contact_name: opportunity.contact_name,
      contact_role: opportunity.contact_role,
      products_mentioned: JSON.parse(opportunity.products_mentioned || '[]'),
      sentiment: opportunity.sentiment,
      estimated_value: opportunity.estimated_monthly_value,
      stage: opportunity.stage,
      next_action: opportunity.next_action,
      created_at: opportunity.created_at,
    });
  } catch (err) {
    next(err);
  } finally {
    if (audioPath && fs.existsSync(audioPath)) fs.unlinkSync(audioPath);
  }
}

module.exports = { submitVisitReport, transcribeOnly };
