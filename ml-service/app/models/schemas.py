# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

"""Pydantic schemas for API requests and responses"""
from pydantic import BaseModel
from typing import Optional


class PredictionInput(BaseModel):
    """Request schema for prediction endpoint"""
    source: str  # "sensor" or "camera"
    value: Optional[float] = None  # For sensor
    event: Optional[str] = None  # For camera
    metric_type: Optional[str] = None  # "temperature", "humidity", "co2" - for sensor
    unit: Optional[str] = None


class PredictionOutput(BaseModel):
    """Response schema for prediction endpoint"""
    label: str  # HOT/WARM/COLD/UNKNOWN
    uri: str  # Schema.org URI
    desc: str  # Description
    metric_type: Optional[str] = None  # Echo back for sensors
    value: Optional[float] = None  # Echo back for sensors
    confidence: Optional[float] = None  # Confidence score (0-1) for human-in-the-loop
    anomaly_score: Optional[float] = None  # IsolationForest anomaly score
    feature_distance: Optional[float] = None  # Distance from training distribution


class HealthResponse(BaseModel):
    """Response schema for health endpoint"""
    status: str
    models_loaded: dict
    total_models: int
