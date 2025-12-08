# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

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
from app.models.schemas import PredictionInput, PredictionOutput, HealthResponse
from app.storage import UnknownEventDB

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Global models dictionary
models = {}

# Global UnknownEventDB instance
unknown_db = None

# Global retraining worker
worker = None

# Thread pool for async database writes
executor = ThreadPoolExecutor(max_workers=2)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load models and initialize unknown event handling on startup"""
    global models, unknown_db, worker
    
    logger.info("Loading ML models...")
    models.update(load_all_models())
    logger.info(f"Total models loaded: {get_model_count(models)}/3")
    
    # Initialize unknown events database
    logger.info("Initializing Unknown Events Database...")
    unknown_db = UnknownEventDB()
    logger.info("Unknown Events Database ready")
    
    # Start background retraining worker
    from app.worker import RetrainingWorker
    worker = RetrainingWorker(
        unknown_db=unknown_db,
        models=models,
        check_interval=3600  # Check every hour
    )
    worker.start()
    logger.info("✅ Background retraining worker started (check every 1h)")
    
    yield
    
    # Cleanup on shutdown
    if worker:
        worker.stop()
        worker.join(timeout=5)  # Wait max 5s for worker to stop
        logger.info("Retraining worker stopped")
    
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
        
        # NEW: Get anomaly score for confidence calculation
        # IsolationForest score_samples: more negative = more anomalous
        anomaly_score = model.score_samples([[input_data.value]])[0]
        
        # NEW: Calculate confidence score (0-1 range)
        # Using sigmoid transformation of anomaly score
        # Higher score = more normal = higher confidence
        import numpy as np
        confidence = 1 / (1 + np.exp(-anomaly_score * 2))  # Scale factor 2 for better range
        
        # NEW: Determine label based on confidence threshold
        if confidence > 0.8:
            # High confidence - use original prediction
            if is_normal:
                label = "COLD"
                uri = "https://schema.org/SafeCondition"
                desc = f"Normal {metric_type.capitalize()} Reading"
            else:
                label = "HOT"
                uri = "https://schema.org/Warning"
                desc = f"{metric_type.capitalize()} Anomaly Detected"
        elif confidence > 0.5:
            # Medium confidence - uncertain (treat as COLD for backward compat)
            label = "COLD"
            uri = "https://schema.org/EventStatusType"
            desc = f"{metric_type.capitalize()} - Uncertain Classification"
        else:
            # Low confidence - unknown/out-of-distribution (treat as COLD for backward compat)
            label = "COLD"
            uri = "https://schema.org/Thing"
            desc = f"{metric_type.capitalize()} - Unknown Pattern Detected"
        
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
