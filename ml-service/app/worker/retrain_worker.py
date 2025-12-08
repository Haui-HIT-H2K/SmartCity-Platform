# Copyright 2025 Haui.HIT - H2K
# Licensed under the Apache License, Version 2.0

"""
Background worker for automatic model retraining
Monitors labeled events and retrains models when threshold reached
"""

import logging
import threading
import time
import numpy as np
from pathlib import Path
from sklearn.ensemble import IsolationForest
import joblib
from datetime import datetime

logger = logging.getLogger(__name__)

class RetrainingWorker(threading.Thread):
    """
    Background worker that automatically retrains ML models
    when sufficient labeled data is available.
    
    Features:
    - Runs as daemon thread
    - Checks every hour (configurable)
    - Hot-swaps models without downtime
    - Validates before deployment
    - Backs up old models
    """
    
    def __init__(self, unknown_db, models, check_interval=3600):
        """
        Initialize retraining worker
        
        Args:
            unknown_db: UnknownEventDB instance
            models: Reference to global models dict
            check_interval: Seconds between checks (default: 1 hour)
        """
        super().__init__(daemon=True, name="RetrainingWorker")
        self.unknown_db = unknown_db
        self.models = models
        self.check_interval = check_interval
        self.running = True
        
        # Thresholds
        self.min_labeled_count = 100  # Minimum samples needed
        self.validation_threshold = 0.8  # Model must be 80% accurate
        
        # Ensure backup directory exists
        self.backup_dir = Path("backups")
        self.backup_dir.mkdir(exist_ok=True)
        
    def run(self):
        """Main worker loop"""
        logger.info("🔄 Retraining worker started (check every %ds)", self.check_interval)
        
        while self.running:
            try:
                # Check each metric type
                for metric_type in ["temperature", "humidity", "co2"]:
                    self._check_and_retrain(metric_type)
                    
            except Exception as e:
                logger.exception(f"Worker error: {e}")
            
            # Sleep until next check
            time.sleep(self.check_interval)
        
        logger.info("Retraining worker stopped")
    
    def _check_and_retrain(self, metric_type):
        """Check if retraining needed and execute if so"""
        
        # Get labeled data
        labeled_data = self.unknown_db.get_labeled_for_training(metric_type)
        
        if len(labeled_data) < self.min_labeled_count:
            logger.debug(f"{metric_type}: {len(labeled_data)} labeled samples (need {self.min_labeled_count})")
            return
        
        logger.info(f"🔄 Retraining {metric_type} model with {len(labeled_data)} new samples...")
        
        try:
            # Load original training data
            original_data = self._load_original_training_data(metric_type)
            
            # Prepare new training data
            new_values = np.array([row[1] for row in labeled_data]).reshape(-1, 1)
            combined_data = np.vstack([original_data, new_values])
            
            logger.info(f"  Original: {len(original_data)} samples")
            logger.info(f"  New: {len(new_values)} samples")
            logger.info(f"  Combined: {len(combined_data)} samples")
            
            # Train new model
            new_model = self._train_model(combined_data)
            
            # Validate new model
            if not self._validate_model(new_model, original_data, metric_type):
                logger.warning(f"❌ {metric_type} new model failed validation - keeping old model")
                return
            
            # Backup old model
            self._backup_model(metric_type)
            
            # HOT-SWAP (atomic replacement in memory)
            old_model = self.models.get(metric_type)
            self.models[metric_type] = new_model
            
            # Save new model to disk
            self._save_model(new_model, metric_type)
            
            # Mark data as used
            event_ids = [row[0] for row in labeled_data]
            self.unknown_db.mark_used_for_training(event_ids)
            
            logger.info(f"✅ {metric_type} model updated successfully!")
            
            # Cleanup old model from memory
            del old_model
            
        except Exception as e:
            logger.exception(f"Failed to retrain {metric_type} model: {e}")
    
    def _load_original_training_data(self, metric_type):
        """
        Load original training data
        Regenerates the same data as train_models.py
        """
        np.random.seed(42)
        
        if metric_type == "temperature":
            # Normal range: 15-35°C
            normal_temp = np.random.uniform(15, 35, 5000).reshape(-1, 1)
            seasonal_temp = np.concatenate([
                np.random.normal(20, 3, 2000).reshape(-1, 1),  # Spring/Fall
                np.random.normal(28, 4, 2000).reshape(-1, 1),  # Summer
                np.random.normal(12, 3, 1000).reshape(-1, 1),  # Winter
            ])
            return np.concatenate([normal_temp, seasonal_temp])
            
        elif metric_type == "humidity":
            # Normal range: 30-80%
            normal_humidity = np.random.uniform(30, 80, 5000).reshape(-1, 1)
            seasonal_humidity = np.concatenate([
                np.random.normal(60, 10, 3000).reshape(-1, 1),  # Moderate
                np.random.normal(45, 8, 2000).reshape(-1, 1),   # Dry season
            ])
            return np.concatenate([normal_humidity, seasonal_humidity])
            
        else:  # co2
            # Normal range: 350-900 ppm
            normal_co2 = np.random.uniform(350, 600, 4000).reshape(-1, 1)
            indoor_co2 = np.random.uniform(600, 900, 3000).reshape(-1, 1)
            return np.concatenate([normal_co2, indoor_co2])
    
    def _train_model(self, data):
        """Train IsolationForest model"""
        model = IsolationForest(
            contamination=0.1,
            random_state=42,
            n_estimators=100,
            n_jobs=-1  # Use all CPU cores
        )
        model.fit(data)
        return model
    
    def _validate_model(self, new_model, original_data, metric_type):
        """
        Validate new model on original data
        Ensures new model still recognizes normal patterns
        """
        # Test on original data (should be mostly normal)
        predictions = new_model.predict(original_data)
        normal_rate = (predictions == 1).mean()
        
        logger.info(f"  Validation: {normal_rate:.1%} of original data classified as normal")
        
        # New model must classify at least 80% of original data as normal
        if normal_rate < self.validation_threshold:
            logger.warning(f"  Model too aggressive: only {normal_rate:.1%} normal (threshold: {self.validation_threshold:.0%})")
            return False
        
        # Check anomaly score distribution
        scores = new_model.score_samples(original_data)
        avg_score = scores.mean()
        
        logger.info(f"  Avg anomaly score: {avg_score:.3f}")
        
        return True
    
    def _backup_model(self, metric_type):
        """Backup current model before replacing"""
        model_path = Path(f"app/models/{metric_type}_model.pkl")
        
        if model_path.exists():
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            backup_path = self.backup_dir / f"{metric_type}_model_{timestamp}.pkl"
            
            # Copy current model to backup
            import shutil
            shutil.copy(model_path, backup_path)
            
            logger.info(f"  Backed up old model: {backup_path.name}")
    
    def _save_model(self, model, metric_type):
        """Save new model to disk"""
        model_path = Path(f"app/models/{metric_type}_model.pkl")
        joblib.dump(model, model_path)
        logger.info(f"  Saved new model: {model_path}")
    
    def stop(self):
        """Gracefully stop worker"""
        logger.info("Stopping retraining worker...")
        self.running = False
