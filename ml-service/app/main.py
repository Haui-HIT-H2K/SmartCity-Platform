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

"""
FastAPI Application for ML anomaly detection service
Refactored version with modular structure
Enhanced with Human-in-the-Loop for unknown event detection
"""
import logging
import asyncio
from concurrent.futures import ThreadPoolExecutor
from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager

from app.config import SEMANTIC_MAP
from app.models.loader import load_all_models, get_model_count
from app.models.schemas import (
    PredictionInput, PredictionOutput, HealthResponse,
    BatchPredictionInput, BatchPredictionOutput, BatchPredictionResult
)
from app.storage import UnknownEventDB
import numpy as np

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Global models dictionary
models = {}

# Global UnknownEventDB instance
unknown_db = None

# Thread pool for async database writes
executor = ThreadPoolExecutor(max_workers=2)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load models and initialize unknown event handling on startup"""
    global models, unknown_db
    
    logger.info("Loading ML models...")
    models.update(load_all_models())
    logger.info(f"Total models loaded: {get_model_count(models)}/3")
    
    # Initialize unknown events database
    logger.info("Initializing Unknown Events Database...")
    unknown_db = UnknownEventDB()
    logger.info("Unknown Events Database ready")
    
    yield
    
    # Cleanup on shutdown
    executor.shutdown(wait=True)
    models.clear()
    logger.info("Models unloaded, executor shutdown")


# Create FastAPI app
app = FastAPI(
    title="ML Anomaly Detection Service",
    description="Smart City sensor anomaly detection API with Human-in-the-Loop",
    version="2.0.0",
    lifespan=lifespan
)

# Include operator API router
from app.api import operator_router
app.include_router(operator_router)


@app.post("/predict", response_model=PredictionOutput)
async def predict(input_data: PredictionInput):
    """
    Predict anomaly for sensor or camera data
    
    Args:
        input_data: Prediction input (sensor value or camera event)
        
    Returns:
        Prediction result with label, URI, and description
    """
    if input_data.source == "sensor":
        # Validate required fields
        if input_data.value is None:
            raise HTTPException(status_code=400, detail="Sensor value is required")
        
        # Default to temperature if metric_type not specified (backward compatibility)
        metric_type = input_data.metric_type or "temperature"
        
        # Validate metric_type
        if metric_type not in models:
            raise HTTPException(
                status_code=400,
                detail=f"Invalid metric_type. Must be one of: {', '.join(models.keys())}"
            )
        
        # Check if model is loaded
        model = models[metric_type]
        if model is None:
            raise HTTPException(
                status_code=503,
                detail=f"{metric_type.capitalize()} model not loaded"
            )
        
        # Predict using the appropriate model
        # Reshape to 2D array as expected by sklearn
        prediction = model.predict([[input_data.value]])
        
        # IsolationForest: 1 = Normal, -1 = Anomaly
        is_normal = prediction[0] == 1
        
        # Get anomaly score for logging/metrics
        # IsolationForest score_samples: more negative = more anomalous
        anomaly_score = model.score_samples([[input_data.value]])[0]
        
        # Calculate confidence score (0-1 range) for logging purposes
        # Using sigmoid transformation of anomaly score
        confidence = 1 / (1 + np.exp(-anomaly_score * 2))
        
        # FIXED LOGIC: Primary classification based on anomaly detection
        # Anomaly (is_normal=False) → HOT (critical data)
        # Normal (is_normal=True) → COLD (routine data)
        if is_normal:
            # Normal reading - classify as COLD
            label = "COLD"
            uri = "https://schema.org/SafeCondition"
            desc = f"Normal {metric_type.capitalize()} Reading"
        else:
            # Anomaly detected - classify as HOT
            label = "HOT"
            uri = "https://schema.org/Warning"
            desc = f"{metric_type.capitalize()} Anomaly Detected (score: {anomaly_score:.3f})"
        
        # NEW: Async log unknown/uncertain events (non-blocking)
        if confidence <= 0.8 and unknown_db is not None:
            # Fire and forget - runs in background thread
            loop = asyncio.get_event_loop()
            loop.run_in_executor(
                executor,
                unknown_db.insert_unknown,
                metric_type,
                input_data.value,
                confidence,
                anomaly_score,
                "UNCERTAIN" if confidence > 0.5 else "UNKNOWN"
            )
            # This adds <1ms overhead because it's async
        
        return PredictionOutput(
            label=label,
            uri=uri,
            desc=desc,
            metric_type=metric_type,
            value=input_data.value,
            confidence=round(confidence, 4),
            anomaly_score=round(anomaly_score, 4),
            feature_distance=None  # Can be added later with training stats
        )

    elif input_data.source == "camera":
        if not input_data.event:
            raise HTTPException(status_code=400, detail="Camera event is required")
        
        event_key = input_data.event.lower()
        if event_key in SEMANTIC_MAP:
            return PredictionOutput(**SEMANTIC_MAP[event_key])
        else:
            return PredictionOutput(
                label="UNKNOWN",
                uri="https://schema.org/Thing",
                desc="Unknown Event"
            )
            
    else:
        raise HTTPException(
            status_code=400,
            detail="Invalid source. Must be 'sensor' or 'camera'"
        )


@app.post("/predict/batch", response_model=BatchPredictionOutput)
async def predict_batch(input_data: BatchPredictionInput):
    """
    Batch predict anomaly for multiple sensor data points.
    Optimized for high-throughput ingestion pipelines.
    
    Args:
        input_data: List of sensor readings with metric_type and value
        
    Returns:
        List of prediction results (HOT/WARM/COLD labels)
    """
    if not input_data.items:
        return BatchPredictionOutput(results=[], total=0)
    
    results = []
    
    for item in input_data.items:
        metric_type = item.metric_type
        value = item.value
        
        # Skip if model not available
        if metric_type not in models or models[metric_type] is None:
            results.append(BatchPredictionResult(
                label="COLD",
                metric_type=metric_type,
                value=value
            ))
            continue
        
        model = models[metric_type]
        
        try:
            # Predict using IsolationForest
            prediction = model.predict([[value]])
            is_normal = prediction[0] == 1
            
            # Get anomaly score for logging
            anomaly_score = model.score_samples([[value]])[0]
            
            # FIXED: Primary classification based on anomaly detection
            # Anomaly (is_normal=False) → HOT, Normal → COLD
            label = "COLD" if is_normal else "HOT"
                
        except Exception as e:
            logger.error(f"Batch predict error for {metric_type}={value}: {e}")
            label = "COLD"  # Fallback
        
        results.append(BatchPredictionResult(
            label=label,
            metric_type=metric_type,
            value=value
        ))
    
    return BatchPredictionOutput(
        results=results,
        total=len(results)
    )


@app.get("/health", response_model=HealthResponse)
async def health():
    """
    Health check endpoint

    Returns:
        Service health status and model loading information
    """
    models_status = {
        metric: (model is not None) 
        for metric, model in models.items()
    }
    
    return HealthResponse(
        status="ok",
        models_loaded=models_status,
        total_models=get_model_count(models)
    )


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "ML Anomaly Detection",
        "version": "1.0.0",
        "status": "running",
        "docs": "/docs"
    }
