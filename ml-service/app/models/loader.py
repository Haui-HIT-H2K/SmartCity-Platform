# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

"""Model loading utilities"""
import joblib
import logging
from app.config import MODEL_FILES

logger = logging.getLogger(__name__)


def load_all_models():
    """
    Load all ML models from disk
    
    Returns:
        dict: Dictionary mapping metric types to loaded models
    """
    models = {}
    
    for metric_type, model_path in MODEL_FILES.items():
        try:
            models[metric_type] = joblib.load(model_path)
            logger.info(f"✓ {metric_type.capitalize()} model loaded from {model_path.name}")
        except Exception as e:
            logger.error(f"✗ Error loading {metric_type} model: {e}")
            models[metric_type] = None
    
    return models


def get_model_count(models):
    """Count successfully loaded models"""
    return sum(1 for m in models.values() if m is not None)
