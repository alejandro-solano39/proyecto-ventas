const express = require('express');
const auth = require('../middleware/auth');
const { getHotels, getHotelById } = require('../controllers/hotelController');

const router = express.Router();

router.get('/', auth, getHotels);
router.get('/:id', auth, getHotelById);

module.exports = router;
