# Copyright 2025 Haui.HIT - H2K
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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
