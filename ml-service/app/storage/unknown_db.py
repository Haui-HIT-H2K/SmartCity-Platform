# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

"""
SQLite database for storing unknown events
Embedded, fast, zero external dependencies
"""
import sqlite3
import logging
from pathlib import Path

logger = logging.getLogger(__name__)

class UnknownEventDB:
    def __init__(self, db_path="data/unknown_events.db"):
        self.db_path = Path(db_path)
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        self.init_db()
        logger.info(f"UnknownEventDB initialized at {self.db_path}")
    
    def init_db(self):
        """Initialize database schema"""
        conn = sqlite3.connect(str(self.db_path))
        cursor = conn.cursor()
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS unknown_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                metric_type TEXT NOT NULL,
                value REAL NOT NULL,
                confidence REAL NOT NULL,
                anomaly_score REAL NOT NULL,
                label TEXT DEFAULT 'UNKNOWN',
                status TEXT DEFAULT 'pending',
                operator_label TEXT,
                operator_notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                labeled_at TIMESTAMP,
                used_for_training INTEGER DEFAULT 0
            )
        """)
        
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_status ON unknown_events(status)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_metric_type ON unknown_events(metric_type)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_created_at ON unknown_events(created_at DESC)")
        
        conn.commit()
        conn.close()
    
    def insert_unknown(self, metric_type: str, value: float, confidence: float, 
                      anomaly_score: float, label: str) -> int:
        """Insert unknown event - optimized for async use"""
        conn = sqlite3.connect(str(self.db_path))
        cursor = conn.cursor()
        
        cursor.execute("""
            INSERT INTO unknown_events 
            (metric_type, value, confidence, anomaly_score, label)
            VALUES (?, ?, ?, ?, ?)
        """, (metric_type, value, confidence, anomaly_score, label))
        
        event_id = cursor.lastrowid
        conn.commit()
        conn.close()
        
        return event_id
    
    def get_pending_events(self, limit: int = 100):
        """Get events pending operator review"""
        conn = sqlite3.connect(str(self.db_path))
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT id, metric_type, value, confidence, anomaly_score, 
                   label, created_at
            FROM unknown_events
            WHERE status = 'pending'
            ORDER BY created_at DESC
            LIMIT ?
        """, (limit,))
        
        results = cursor.fetchall()
        conn.close()
        
        return [
            {
                "id": r[0],
                "metric_type": r[1],
                "value": r[2],
                "confidence": r[3],
                "anomaly_score": r[4],
                "label": r[5],
                "created_at": r[6]
            }
            for r in results
        ]
    
    def label_event(self, event_id: int, operator_label: str, notes: str = None):
        """Operator labels an event"""
        conn = sqlite3.connect(str(self.db_path))
        cursor = conn.cursor()
        
        cursor.execute("""
            UPDATE unknown_events
            SET operator_label = ?,
                operator_notes = ?,
                status = 'labeled',
                labeled_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """, (operator_label, notes, event_id))
        
        conn.commit()
        conn.close()
        logger.info(f"Event {event_id} labeled as '{operator_label}'")
    
    def get_statistics(self):
        """Get dashboard statistics"""
        conn = sqlite3.connect(str(self.db_path))
        cursor = conn.cursor()
        
        cursor.execute("SELECT COUNT(*) FROM unknown_events WHERE status = 'pending'")
        pending = cursor.fetchone()[0]
        
        cursor.execute("""
            SELECT COUNT(*) FROM unknown_events 
            WHERE status = 'labeled' 
            AND DATE(labeled_at) = DATE('now')
        """)
        labeled_today = cursor.fetchone()[0]
        
        cursor.execute("SELECT AVG(confidence) FROM unknown_events WHERE status = 'pending'")
        avg_confidence = cursor.fetchone()[0] or 0
        
        cursor.execute("SELECT COUNT(*) FROM unknown_events")
        total = cursor.fetchone()[0]
        
        conn.close()
        
        return {
            "pending": pending,
            "labeled_today": labeled_today,
            "avg_confidence": round(avg_confidence, 3),
            "total_events": total
        }
    
    def get_labeled_for_training(self, metric_type: str):
        """Get newly labeled events for retraining"""
        conn = sqlite3.connect(str(self.db_path))
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT id, value, operator_label
            FROM unknown_events
            WHERE metric_type = ?
              AND status = 'labeled'
              AND used_for_training = 0
        """, (metric_type,))
        
        results = cursor.fetchall()
        conn.close()
        
        return results
    
    def mark_used_for_training(self, event_ids):
        """Mark events as used for training"""
        if not event_ids:
            return
            
        conn = sqlite3.connect(str(self.db_path))
        cursor = conn.cursor()
        
        placeholders = ','.join(['?' for _ in event_ids])
        cursor.execute(f"""
            UPDATE unknown_events
            SET used_for_training = 1
            WHERE id IN ({placeholders})
        """, event_ids)
        
        conn.commit()
        conn.close()
        
        logger.info(f"Marked {len(event_ids)} events as used for training")
