require('dotenv').config();
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const fs = require('node:fs');
const path = require('node:path');
const { initDb, query } = require('./connection');

async function seed() {
  await initDb();

  const schema = fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8');
  schema.split(';').map(s => s.trim()).filter(Boolean).forEach(s => query(s));
  console.log('✅ Esquema creado');

  const passwordHash = bcrypt.hashSync('demo123', 10);
  const insertUser = `INSERT OR IGNORE INTO users (id, name, email, password_hash, role) VALUES (?,?,?,?,?)`;
  query(insertUser, [uuidv4(), 'Admin Demo',     'admin@ventas.com',    passwordHash, 'admin']);
  query(insertUser, [uuidv4(), 'Vendedor Demo',  'vendedor@ventas.com', passwordHash, 'seller']);
  console.log('✅ Usuarios demo creados');

  const insertHotel = `INSERT OR IGNORE INTO hotels (id,name,city,latitude,longitude,stars,avg_monthly_guests) VALUES (?,?,?,?,?,?,?)`;
  [
    // ─── Guadalajara ───
    ['h001','Hotel Presidente',           'Guadalajara', 20.6297, -103.3831, 5, 450],
    ['h002','Hotel Magno',                'Guadalajara', 20.634,  -103.382,  4, 350],
    ['h003','Hotel Hilton',               'Guadalajara', 20.626,  -103.385,  5, 500],
    ['h004','Hotel Aranzazu',             'Guadalajara', 20.628,  -103.39,   4, 300],
    ['h005','Hotel Fiesta Americana',     'Guadalajara', 20.635,  -103.375,  5, 400],
    // ─── CDMX (Ciudad de México) ───
    ['h006','Hotel Camino Real Polanco',  'CDMX',        19.4284, -99.1953,  5, 600],
    ['h007','Hotel Marriott Reforma',     'CDMX',        19.4270, -99.1780,  5, 550],
    ['h008','Hotel Sheraton María Isabel','CDMX',        19.4268, -99.1665,  5, 580],
    ['h009','Hotel Four Seasons Reforma', 'CDMX',        19.4225, -99.1690,  5, 480],
    ['h010','Hotel Hilton Centro Histórico','CDMX',      19.4350, -99.1410,  5, 620],
    ['h011','Hotel NH Centro Histórico',  'CDMX',        19.4340, -99.1380,  4, 380],
    ['h012','Hotel Galería Plaza Reforma','CDMX',        19.4255, -99.1720,  4, 420],
    ['h013','Hotel Krystal Grand Insurgentes','CDMX',    19.4100, -99.1700,  4, 360],
    ['h014','Hotel Fiesta Inn Aeropuerto','CDMX',        19.4360, -99.0850,  4, 500],
    ['h015','Hotel Presidente InterContinental','CDMX',  19.4270, -99.1900,  5, 640],
    // ─── Monterrey ───
    ['h016','Hotel Krystal Monterrey',    'Monterrey',   25.6866, -100.3161, 4, 320],
    ['h017','Hotel Safi Royal Luxury',    'Monterrey',   25.6520, -100.3680, 5, 410],
    ['h018','Hotel Fiesta Americana Pabellón','Monterrey',25.6700, -100.3090, 5, 450],
  ].forEach(h => query(insertHotel, h));
  console.log('✅ Hoteles creados');
  console.log('✅ Seed completado — BD lista en data/ventas.db');
}

seed().catch(console.error);
