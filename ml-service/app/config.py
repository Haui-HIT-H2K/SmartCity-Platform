# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

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
