"""
FastAPI Application for ML anomaly detection service
Refactored version with modular structure
"""
import logging
from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager

from app.config import SEMANTIC_MAP
from app.models.loader import load_all_models, get_model_count
from app.models.schemas import PredictionInput, PredictionOutput, HealthResponse

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Global models dictionary
models = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load models on startup, cleanup on shutdown"""
    global models
    
    logger.info("Loading ML models...")
    models.update(load_all_models())
    logger.info(f"Total models loaded: {get_model_count(models)}/3")
    
    yield
    
    # Cleanup on shutdown
    models.clear()
    logger.info("Models unloaded")


# Create FastAPI app
app = FastAPI(
    title="ML Anomaly Detection Service",
    description="Smart City sensor anomaly detection API",
    version="1.0.0",
    lifespan=lifespan
)


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
        
        if is_normal:
            return PredictionOutput(
                label="COLD",
                uri="https://schema.org/SafeCondition",
                desc=f"Normal {metric_type.capitalize()} Reading",
                metric_type=metric_type,
                value=input_data.value
            )
        else:
            return PredictionOutput(
                label="HOT",
                uri="https://schema.org/Warning",
                desc=f"{metric_type.capitalize()} Anomaly Detected",
                metric_type=metric_type,
                value=input_data.value
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
