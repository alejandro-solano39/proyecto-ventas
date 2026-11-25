require('dotenv').config();
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const fs = require('node:fs');
const path = require('node:path');
const { initDb, query } = require('./connection');

async function seed() {
  await initDb();

  const schema = fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8');
  const statements = schema.split(';').map(s => s.trim()).filter(Boolean);
  for (const s of statements) await query(s);
  console.log('✅ Esquema creado');

  const passwordHash = bcrypt.hashSync('demo123', 10);
  const insertUser = `INSERT INTO users (id, name, email, password_hash, role) VALUES (?,?,?,?,?) ON CONFLICT (email) DO NOTHING`;
  await query(insertUser, [uuidv4(), 'Admin Demo',     'admin@ventas.com',    passwordHash, 'admin']);
  await query(insertUser, [uuidv4(), 'Vendedor Demo',  'vendedor@ventas.com', passwordHash, 'seller']);
  console.log('✅ Usuarios demo creados');

  const insertHotel = `INSERT INTO hotels (id,name,city,latitude,longitude,stars,avg_monthly_guests) VALUES (?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING`;
  const hotels = [
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
    ['h019','Hotel Quinta Real Monterrey','Monterrey',   25.6490, -100.3620, 5, 430],
    ['h020','Hotel Sheraton Ambassador',  'Monterrey',   25.6720, -100.3110, 5, 470],
    ['h021','Hotel Camino Real Monterrey','Monterrey',   25.6480, -100.3550, 5, 440],
    ['h022','Hotel Presidente InterContinental MTY','Monterrey',25.6530,-100.3660,5, 490],
    ['h023','Hotel Fiesta Inn Monterrey Centro','Monterrey',25.6710,-100.3140,4, 350],
    ['h024','Hotel Holiday Inn Parque Fundidora','Monterrey',25.6790,-100.2870,4, 360],
    ['h025','Hotel Crowne Plaza Monterrey','Monterrey',  25.6640, -100.3350, 5, 420],
    ['h026','Hotel Hilton Garden Inn MTY','Monterrey',   25.6450, -100.3600, 4, 340],
    ['h027','Hotel NH Collection Monterrey','Monterrey', 25.6555, -100.3690, 4, 370],
    ['h028','Hotel Fairmont Monterrey',   'Monterrey',   25.6510, -100.3640, 5, 460],
    ['h029','Hotel Marriott Valle Oriente','Monterrey',  25.6360, -100.3390, 5, 480],
    ['h030','Hotel Live Aqua San Pedro',  'Monterrey',   25.6570, -100.4020, 5, 500],
    ['h031','Hotel City Express Plus MTY','Monterrey',   25.6690, -100.3230, 3, 280],
    ['h032','Hotel Grand Fiesta Americana','Monterrey',  25.6340, -100.3420, 5, 510],
    ['h033','Hotel Antaris Monterrey',    'Monterrey',   25.6820, -100.3050, 4, 330],
    ['h034','Hotel Chipinque',            'Monterrey',   25.6100, -100.3560, 4, 300],
    ['h035','Hotel Courtyard Monterrey',  'Monterrey',   25.6420, -100.3480, 4, 355],
    ['h036','Hotel Novotel Valle',        'Monterrey',   25.6390, -100.3410, 4, 345],
    ['h037','Hotel Radisson Plaza Gran',  'Monterrey',   25.6700, -100.3180, 4, 365],
    ['h038','Hotel Fiesta Inn Tecnológico','Monterrey',  25.6580, -100.2890, 4, 340],
  ];
  for (const h of hotels) await query(insertHotel, h);
  console.log('✅ Hoteles creados');
  console.log('✅ Seed completado en PostgreSQL');
}

seed().catch(console.error);
