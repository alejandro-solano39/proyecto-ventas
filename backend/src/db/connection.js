const { Pool } = require('pg');

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false },
});

// Convierte placeholders estilo SQLite (?) a estilo Postgres ($1, $2, ...)
function toPgPlaceholders(sql) {
  let i = 0;
  return sql.replaceAll('?', () => `$${++i}`);
}

async function query(sql, params = []) {
  const { rows, rowCount } = await pool.query(toPgPlaceholders(sql), params);
  return { rows, rowCount };
}

async function queryOne(sql, params = []) {
  const { rows } = await query(sql, params);
  return rows[0] || null;
}

async function initDb() {
  await pool.query('SELECT 1');
}

module.exports = { query, queryOne, initDb };
