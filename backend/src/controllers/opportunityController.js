const { query, queryOne } = require('../db/connection');

async function getOpportunities(req, res, next) {
  try {
    const { stage, city, limit = 50, page = 1 } = req.query;
    const offset = (parseInt(page) - 1) * parseInt(limit);
    const conditions = [];
    const params = [];

    if (stage) { conditions.push('o.stage = ?'); params.push(stage); }
    if (city)  { conditions.push('LOWER(h.city) = LOWER(?)'); params.push(city); }

    const where = conditions.length ? `WHERE ${conditions.join(' AND ')}` : '';

    const countRow = queryOne(
      `SELECT COUNT(*) as count FROM opportunities o LEFT JOIN hotels h ON o.hotel_id = h.id ${where}`,
      params
    );
    const total = countRow ? countRow.count : 0;

    const { rows } = query(
      `SELECT o.*, h.name AS hotel_name, h.city, u.name AS seller_name
       FROM opportunities o
       LEFT JOIN hotels h ON o.hotel_id = h.id
       LEFT JOIN users u ON o.user_id = u.id
       ${where}
       ORDER BY o.created_at DESC
       LIMIT ? OFFSET ?`,
      [...params, parseInt(limit), offset]
    );

    const opportunities = rows.map(r => ({
      ...r,
      products_mentioned: JSON.parse(r.products_mentioned || '[]'),
    }));

    res.json({ opportunities, total, page: parseInt(page), limit: parseInt(limit) });
  } catch (err) {
    next(err);
  }
}

async function getOpportunityById(req, res, next) {
  try {
    const row = queryOne(
      `SELECT o.*, h.name AS hotel_name, h.city, h.stars, u.name AS seller_name
       FROM opportunities o
       LEFT JOIN hotels h ON o.hotel_id = h.id
       LEFT JOIN users u ON o.user_id = u.id
       WHERE o.id = ?`,
      [req.params.id]
    );

    if (!row) return res.status(404).json({ error: 'Oportunidad no encontrada' });

    const visit = row.visit_id
      ? queryOne('SELECT * FROM visits WHERE id = ?', [row.visit_id])
      : null;

    res.json({
      opportunity: { ...row, products_mentioned: JSON.parse(row.products_mentioned || '[]') },
      related_visits: visit ? [visit] : [],
    });
  } catch (err) {
    next(err);
  }
}

async function updateOpportunity(req, res, next) {
  try {
    const { id } = req.params;
    const { stage, next_action, next_action_due_date } = req.body;

    const valid = ['OPEN','CONTACTED','PROPOSAL_SENT','NEGOTIATION','CLOSED_WON','CLOSED_LOST'];
    if (stage && !valid.includes(stage)) {
      return res.status(400).json({ error: `Etapa inválida. Valores: ${valid.join(', ')}` });
    }

    const existing = queryOne('SELECT * FROM opportunities WHERE id = ?', [id]);
    if (!existing) return res.status(404).json({ error: 'Oportunidad no encontrada' });

    query(
      `UPDATE opportunities SET
         stage = ?,
         next_action = ?,
         next_action_due_date = ?,
         updated_at = datetime('now')
       WHERE id = ?`,
      [
        stage || existing.stage,
        next_action || existing.next_action,
        next_action_due_date || existing.next_action_due_date,
        id,
      ]
    );

    const updated = queryOne('SELECT * FROM opportunities WHERE id = ?', [id]);
    res.json({ opportunity: { ...updated, products_mentioned: JSON.parse(updated.products_mentioned || '[]') } });
  } catch (err) {
    next(err);
  }
}

module.exports = { getOpportunities, getOpportunityById, updateOpportunity };
