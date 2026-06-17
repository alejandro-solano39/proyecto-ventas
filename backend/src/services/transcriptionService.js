const Groq = require('groq-sdk');
const fs = require('fs');
const logger = require('../utils/logger');

const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

// Groq ofrece Whisper gratis — misma calidad que OpenAI pero sin costo
async function transcribeAudio(audioFilePath) {
  try {
    const transcription = await groq.audio.transcriptions.create({
      file: fs.createReadStream(audioFilePath),
      model: 'whisper-large-v3-turbo',
      language: 'es',
      response_format: 'text',
    });

    const text = typeof transcription === 'string'
      ? transcription.trim()
      : transcription.text?.trim() || '';

    logger.info(`Groq Whisper transcripción: "${text.substring(0, 100)}..."`);
    return text;
  } catch (err) {
    const detail = err.message || 'Error desconocido';
    logger.error('Error en transcripción Groq Whisper:', detail);
    throw new Error(`Error al transcribir audio: ${detail}`);
  }
}

module.exports = { transcribeAudio };
