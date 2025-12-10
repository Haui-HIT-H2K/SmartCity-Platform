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

"""Configuration management for ml-service"""
import os
from pathlib import Path

# Base paths
BASE_DIR = Path(__file__).parent  # Changed from parent.parent to work in Docker
MODELS_DIR = BASE_DIR / "models"

# Model files
MODEL_FILES = {
    "temperature": MODELS_DIR / "temperature_model.pkl",
    "humidity": MODELS_DIR / "humidity_model.pkl",
    "co2": MODELS_DIR / "co2_model.pkl"
}

# Semantic Map for Camera Events
SEMANTIC_MAP = {
    "fire": {
        "label": "HOT",
        "uri": "https://schema.org/FireEvent",
        "desc": "Fire Hazard Detected"
    },
    "accident": {
        "label": "HOT",
        "uri": "https://schema.org/TrafficIncident",
        "desc": "Traffic Accident Detected"
    },
    "traffic_jam": {
        "label": "WARM",
        "uri": "https://w3id.org/sosa/Observation",
        "desc": "Traffic Congestion"
    },
    "normal": {
        "label": "COLD",
        "uri": "https://schema.org/SafeCondition",
        "desc": "Normal Conditions"
    }
}
