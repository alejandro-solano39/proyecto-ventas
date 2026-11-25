const axios = require('axios');
const FormData = require('form-data');
const fs = require('node:fs');
const path = require('node:path');
const logger = require('../utils/logger');

const GROQ_URL = 'https://api.groq.com/openai/v1/audio/transcriptions';
const MAX_RETRIES = 3;
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// Whisper "alucina" frases de relleno cuando el audio está en silencio o casi vacío.
// Si la transcripción es solo una de estas, la tratamos como audio sin voz.
const HALLUCINATIONS = new Set([
  'gracias', 'gracias.', 'gracias por ver el video', 'gracias por ver',
  'subtítulos', 'subtítulos por', 'música', '. . .', '...',
]);

function isEmptyOrHallucination(text) {
  const t = text.toLowerCase().replace(/[.\s]+/g, ' ').trim();
  if (!t) return true;
  // Solo relleno repetido (ej: "gracias gracias gracias")
  const unique = [...new Set(t.split(' ').filter(Boolean))].join(' ');
  return HALLUCINATIONS.has(unique) || HALLUCINATIONS.has(t);
}

// Sube el audio a Groq Whisper una vez. Devuelve el texto (lanza si falla).
async function postToGroq(buffer, filename) {
  const form = new FormData();
  form.append('file', buffer, { filename });
  form.append('model', 'whisper-large-v3-turbo');
  form.append('language', 'es');
  form.append('response_format', 'text');

  const { data } = await axios.post(GROQ_URL, form, {
    headers: {
      ...form.getHeaders(),
      Authorization: `Bearer ${process.env.GROQ_API_KEY}`,
    },
    maxBodyLength: Infinity,
    maxContentLength: Infinity,
    timeout: 60000,
  });

  return (typeof data === 'string' ? data : data.text || '').trim();
}

function isTransient(err) {
  const msg = err.response?.data ? JSON.stringify(err.response.data) : err.message;
  return /premature close|fetch failed|ECONNRESET|ETIMEDOUT|socket hang up|network/i.test(msg || '');
}

// Groq ofrece Whisper gratis — misma calidad que OpenAI pero sin costo.
// Usamos axios + form-data (pila HTTP nativa de Node, igual que curl) en vez del SDK,
// porque el fetch nativo de Node 18+ (undici) corta la subida multipart -> "Premature close".
async function transcribeAudio(audioFilePath) {
  // Guard: archivo inexistente o demasiado pequeño (grabación vacía/cortada)
  const stat = fs.existsSync(audioFilePath) ? fs.statSync(audioFilePath) : null;
  if (!stat || stat.size < 2000) {
    throw new Error('El audio está vacío o es demasiado corto. Verifica el micrófono e intenta de nuevo.');
  }

  const buffer = fs.readFileSync(audioFilePath);
  const filename = path.basename(audioFilePath);

  let lastErr;
  for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
    try {
      const text = await postToGroq(buffer, filename);
      logger.info(`Groq Whisper transcripción: "${text.substring(0, 100)}..."`);
      if (isEmptyOrHallucination(text)) {
        throw new Error('No se detectó voz en el audio. Verifica el micrófono y habla claro al grabar.');
      }
      return text;
    } catch (err) {
      lastErr = err;
      if (isTransient(err) && attempt < MAX_RETRIES) {
        logger.warn(`Transcripción falló (intento ${attempt}/${MAX_RETRIES}): ${err.message}. Reintentando…`);
        await sleep(500 * attempt);
        continue;
      }
      break;
    }
  }

  const detail = lastErr?.message || 'Error desconocido';
  logger.error('Error en transcripción Groq Whisper:', detail);
  throw new Error(detail.startsWith('No se detectó') || detail.startsWith('El audio')
    ? detail
    : `Error al transcribir audio: ${detail}`);
}

module.exports = { transcribeAudio };
