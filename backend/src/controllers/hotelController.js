const { query, queryOne } = require('../db/connection');

async function getHotels(req, res, next) {
  try {
    const { city } = req.query;
    // visited = true si el hotel tiene al menos una visita registrada (fuente: tabla visits).
    // Así la marca de "visitado" es consistente entre la web y la app (vive en la BD, no en el dispositivo).
    const base = `
      SELECT h.*, (COUNT(v.id) > 0) AS visited
      FROM hotels h
      LEFT JOIN visits v ON v.hotel_id = h.id`;
    const { rows } = city
      ? await query(`${base} WHERE LOWER(h.city) = LOWER(?) GROUP BY h.id ORDER BY h.name`, [city])
      : await query(`${base} GROUP BY h.id ORDER BY h.name`);
    res.json({ hotels: rows });
  } catch (err) {
    next(err);
  }
}

async function getHotelById(req, res, next) {
  try {
    const hotel = await queryOne('SELECT * FROM hotels WHERE id = ?', [req.params.id]);
    if (!hotel) return res.status(404).json({ error: 'Hotel no encontrado' });
    res.json({ hotel });
  } catch (err) {
    next(err);
  }
}

module.exports = { getHotels, getHotelById };
