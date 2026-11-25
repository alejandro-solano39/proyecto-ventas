const axios = require('axios');
const logger = require('../utils/logger');

// Usamos axios (pila HTTP nativa de Node) en vez del SDK de Groq: el fetch nativo de
// Node 18+ (undici) corta las peticiones -> "Premature close".
const GROQ_CHAT_URL = 'https://api.groq.com/openai/v1/chat/completions';
const MAX_RETRIES = 3;
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

const SYSTEM_PROMPT = `Eres un asistente de análisis de ventas especializado en el sector hotelero.
Tu tarea es extraer información estructurada de transcripciones de visitas de ventas.
Responde SIEMPRE con JSON válido, sin markdown, sin texto adicional, sin bloques de código.`;

const USER_PROMPT_TEMPLATE = `Dado el siguiente texto de una transcripción de visita a hotel, extrae la información en formato JSON.

Texto: "{transcription}"

Extrae exactamente estos campos (responde SOLO el JSON, sin nada más):
{
  "contact_name": "nombre completo del contacto, o null si no se menciona",
  "contact_role": "puesto del contacto (ej: Gerente Compras, Ama de Llaves, Director General), o null",
  "products_mentioned": ["array de productos mencionados, vacío si no hay"],
  "sentiment": "uno de: VERY_INTERESTED | INTERESTED | NOT_INTERESTED | FOLLOW_UP",
  "estimated_monthly_value": número en MXN SOLO si se menciona explícitamente una cantidad/monto en el texto; si no se menciona ningún valor, devuelve 0. NO inventes ni estimes cifras.,
  "next_action": "acción concreta a seguir (ej: Enviar catálogo, Llamada en 3 días)"
}`;

async function callGroqChat(prompt) {
  let lastErr;
  for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
    try {
      const { data } = await axios.post(
        GROQ_CHAT_URL,
        {
          // llama-3.3-70b-versatile fue retirado por Groq (404 model_not_found)
          model: process.env.GROQ_CHAT_MODEL || 'openai/gpt-oss-120b',
          messages: [
            { role: 'system', content: SYSTEM_PROMPT },
            { role: 'user', content: prompt },
          ],
          temperature: 0.1,
          reasoning_effort: 'low',
          // gpt-oss gasta tokens en razonamiento antes de responder; dejar margen
          max_tokens: 1024,
        },
        {
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${process.env.GROQ_API_KEY}`,
          },
          timeout: 60000,
        }
      );
      return data.choices[0].message.content.trim();
    } catch (err) {
      lastErr = err;
      const msg = err.response?.data ? JSON.stringify(err.response.data) : err.message;
      const transient = /premature close|fetch failed|ECONNRESET|ETIMEDOUT|socket hang up|network/i.test(msg || '');
      if (transient && attempt < MAX_RETRIES) {
        logger.warn(`Análisis Groq falló (intento ${attempt}/${MAX_RETRIES}): ${msg}. Reintentando…`);
        await sleep(500 * attempt);
        continue;
      }
      break;
    }
  }
  throw lastErr;
}

async function extractVisitInfo(transcriptionText) {
  try {
    const prompt = USER_PROMPT_TEMPLATE.replace('{transcription}', transcriptionText);

    const rawText = await callGroqChat(prompt);
    logger.debug(`Respuesta Groq: ${rawText}`);

    // Limpiar posibles bloques markdown que el modelo agregue
    const jsonText = rawText
      .replace(/```json\n?/g, '')
      .replace(/```\n?/g, '')
      .trim();

    const extracted = JSON.parse(jsonText);

    return {
      contact_name: extracted.contact_name || null,
      contact_role: extracted.contact_role || null,
      products_mentioned: Array.isArray(extracted.products_mentioned)
        ? extracted.products_mentioned
        : [],
      sentiment: ['VERY_INTERESTED', 'INTERESTED', 'NOT_INTERESTED', 'FOLLOW_UP'].includes(
        extracted.sentiment
      )
        ? extracted.sentiment
        : 'FOLLOW_UP',
      estimated_monthly_value: parseFloat(extracted.estimated_monthly_value) || 0,
      next_action: extracted.next_action || 'Dar seguimiento',
    };
  } catch (err) {
    const detail = err.response?.data ? JSON.stringify(err.response.data) : err.message;
    logger.error(`Error al procesar con Groq: ${detail}`);
    throw new Error(`Error al extraer información de visita: ${err.message}`);
  }
}

module.exports = { extractVisitInfo };
