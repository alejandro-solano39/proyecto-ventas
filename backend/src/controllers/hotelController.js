const { query, queryOne } = require('../db/connection');

async function getHotels(req, res, next) {
  try {
    const { city } = req.query;
    const { rows } = city
      ? query('SELECT * FROM hotels WHERE LOWER(city) = LOWER(?) ORDER BY name', [city])
      : query('SELECT * FROM hotels ORDER BY name');
    res.json({ hotels: rows });
  } catch (err) {
    next(err);
  }
}

async function getHotelById(req, res, next) {
  try {
    const hotel = queryOne('SELECT * FROM hotels WHERE id = ?', [req.params.id]);
    if (!hotel) return res.status(404).json({ error: 'Hotel no encontrado' });
    res.json({ hotel });
  } catch (err) {
    next(err);
  }
}

module.exports = { getHotels, getHotelById };
