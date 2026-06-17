const Groq = require('groq-sdk');
const logger = require('../utils/logger');

const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

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
  "estimated_monthly_value": número en MXN estimado según tipo de hotel y productos mencionados,
  "next_action": "acción concreta a seguir (ej: Enviar catálogo, Llamada en 3 días)"
}`;

async function extractVisitInfo(transcriptionText) {
  try {
    const prompt = USER_PROMPT_TEMPLATE.replace('{transcription}', transcriptionText);

    const completion = await groq.chat.completions.create({
      model: 'llama-3.3-70b-versatile',
      messages: [
        { role: 'system', content: SYSTEM_PROMPT },
        { role: 'user', content: prompt },
      ],
      temperature: 0.1,
      max_tokens: 512,
    });

    const rawText = completion.choices[0].message.content.trim();
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
    logger.error('Error al procesar con Groq:', err.message);
    throw new Error(`Error al extraer información de visita: ${err.message}`);
  }
}

module.exports = { extractVisitInfo };
