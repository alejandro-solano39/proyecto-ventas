CREATE TABLE IF NOT EXISTS users (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  role TEXT DEFAULT 'seller' CHECK (role IN ('admin', 'seller')),
  created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS hotels (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  city TEXT NOT NULL,
  latitude REAL NOT NULL,
  longitude REAL NOT NULL,
  stars INTEGER CHECK (stars BETWEEN 1 AND 5),
  avg_monthly_guests INTEGER DEFAULT 0,
  created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS visits (
  id TEXT PRIMARY KEY,
  hotel_id TEXT REFERENCES hotels(id),
  user_id TEXT REFERENCES users(id),
  audio_transcription TEXT,
  manual_notes TEXT,
  created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS opportunities (
  id TEXT PRIMARY KEY,
  hotel_id TEXT REFERENCES hotels(id),
  visit_id TEXT REFERENCES visits(id),
  user_id TEXT REFERENCES users(id),
  contact_name TEXT,
  contact_role TEXT,
  products_mentioned TEXT DEFAULT '[]',
  sentiment TEXT CHECK (sentiment IN ('VERY_INTERESTED','INTERESTED','NOT_INTERESTED','FOLLOW_UP')),
  estimated_monthly_value REAL DEFAULT 0,
  stage TEXT DEFAULT 'OPEN' CHECK (stage IN ('OPEN','CONTACTED','PROPOSAL_SENT','NEGOTIATION','CLOSED_WON','CLOSED_LOST')),
  next_action TEXT,
  next_action_due_date TEXT,
  created_at TEXT DEFAULT (datetime('now')),
  updated_at TEXT DEFAULT (datetime('now'))
);
