const express = require('express');
const auth = require('../middleware/auth');
const {
  getOpportunities,
  getOpportunityById,
  updateOpportunity,
  deleteOpportunity,
} = require('../controllers/opportunityController');

const router = express.Router();

router.get('/', auth, getOpportunities);
router.get('/:id', auth, getOpportunityById);
router.patch('/:id', auth, updateOpportunity);
router.delete('/:id', auth, deleteOpportunity);

module.exports = router;
