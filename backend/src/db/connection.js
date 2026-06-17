const initSqlJs = require('sql.js');
const fs = require('node:fs');
const path = require('node:path');

const DB_PATH = path.join(__dirname, '../../data/ventas.db');
const DATA_DIR = path.join(__dirname, '../../data');

if (!fs.existsSync(DATA_DIR)) fs.mkdirSync(DATA_DIR, { recursive: true });

let _db = null;

// Guardar a disco después de cada escritura
function persist(db) {
  const data = db.export();
  fs.writeFileSync(DB_PATH, Buffer.from(data));
}

async function getDb() {
  if (_db) return _db;
  const SQL = await initSqlJs();
  if (fs.existsSync(DB_PATH)) {
    const fileBuffer = fs.readFileSync(DB_PATH);
    _db = new SQL.Database(fileBuffer);
  } else {
    _db = new SQL.Database();
  }
  return _db;
}

// Interfaz síncrona con helpers para SELECT y mutaciones
function query(sql, params = []) {
  if (!_db) throw new Error('DB no inicializada — llama a initDb() primero');

  const verb = sql.trim().split(/\s+/)[0].toUpperCase();
  if (verb === 'SELECT' || verb === 'WITH') {
    const stmt = _db.prepare(sql);
    stmt.bind(params);
    const rows = [];
    while (stmt.step()) rows.push(stmt.getAsObject());
    stmt.free();
    return { rows };
  }

  _db.run(sql, params);
  persist(_db);
  return { rows: [], rowCount: 1 };
}

// Equivalente a "get one row"
function queryOne(sql, params = []) {
  const { rows } = query(sql, params);
  return rows[0] || null;
}

// Inicializar DB (debe llamarse al arrancar el servidor)
async function initDb() {
  await getDb();
}

module.exports = { query, queryOne, initDb, get db() { return _db; } };
