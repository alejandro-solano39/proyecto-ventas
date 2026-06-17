const express = require('express');
const auth = require('../middleware/auth');
const {
  getOpportunities,
  getOpportunityById,
  updateOpportunity,
} = require('../controllers/opportunityController');

const router = express.Router();

router.get('/', auth, getOpportunities);
router.get('/:id', auth, getOpportunityById);
router.patch('/:id', auth, updateOpportunity);

module.exports = router;
