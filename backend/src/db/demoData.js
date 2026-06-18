/**
 * Genera oportunidades demo variadas (etapas, sentimientos, fechas de los últimos 14 días)
 * para que el dashboard web luzca completo en una presentación.
 *
 * Uso:  node src/db/demoData.js          -> agrega ~22 oportunidades demo
 *       node src/db/demoData.js --reset  -> borra las demo (visit_id IS NULL) y regenera
 */
require('dotenv').config();
const { v4: uuidv4 } = require('uuid');
const { initDb, query } = require('./connection');

const STAGES = ['OPEN', 'CONTACTED', 'PROPOSAL_SENT', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST'];
const SENTIMENTS = ['VERY_INTERESTED', 'INTERESTED', 'FOLLOW_UP', 'NOT_INTERESTED'];
const PRODUCTS = [
  ['toallas premium', 'amenidades de baño'],
  ['blancos hoteleros', 'edredones'],
  ['amenidades premium', 'jabones artesanales'],
  ['sábanas algodón egipcio'],
  ['kit de bienvenida', 'pantuflas'],
];
const CONTACTS = [
  ['María López', 'Gerente de Compras'],
  ['Carlos Ruiz', 'Director General'],
  ['Ana Torres', 'Ama de Llaves'],
  ['Jorge Mendoza', 'Jefe de Operaciones'],
  ['Laura Díaz', 'Gerente de Hospitalidad'],
];
const ACTIONS = ['Enviar cotización', 'Llamada de seguimiento', 'Agendar demo', 'Enviar catálogo', 'Reunión presencial'];

function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
function isoDaysAgo(d) {
  const date = new Date();
  date.setDate(date.getDate() - d);
  return date.toISOString().slice(0, 19).replace('T', ' ');
}

async function run() {
  await initDb();

  if (process.argv.includes('--reset')) {
    await query('DELETE FROM opportunities WHERE visit_id IS NULL');
    console.log('🗑️  Oportunidades demo previas eliminadas');
  }

  const { rows: hotels } = await query('SELECT id, name, city FROM hotels');
  const { rows: users } = await query("SELECT id FROM users WHERE role = 'seller' LIMIT 1");
  const userId = users[0]?.id;
  if (!hotels.length || !userId) {
    console.error('No hay hoteles o usuario vendedor. Corre primero: node src/db/seed.js');
    return;
  }

  let created = 0;
  for (let i = 0; i < 22; i++) {
    const hotel = pick(hotels);
    const [name, role] = pick(CONTACTS);
    const stage = pick(STAGES);
    const sentiment = pick(SENTIMENTS);
    const value = [8000, 12000, 25000, 45000, 80000, 150000][Math.floor(Math.random() * 6)];
    const daysAgo = Math.floor(Math.random() * 14);
    const created_at = isoDaysAgo(daysAgo);

    await query(
      `INSERT INTO opportunities
        (id, hotel_id, visit_id, user_id, contact_name, contact_role,
         products_mentioned, sentiment, estimated_monthly_value, stage, next_action, created_at, updated_at)
       VALUES (?,?,NULL,?,?,?,?,?,?,?,?,?,?)`,
      [
        uuidv4(), hotel.id, userId, name, role,
        JSON.stringify(pick(PRODUCTS)), sentiment, value, stage, pick(ACTIONS),
        created_at, created_at,
      ]
    );
    created++;
  }
  console.log(`✅ ${created} oportunidades demo creadas (etapas, sentimientos y fechas variadas)`);
}

run().catch(console.error);
