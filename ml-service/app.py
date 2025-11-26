import joblib
import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from contextlib import asynccontextmanager

# Global variable to hold the model
model = None

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

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Load the ML model on startup
    global model
    try:
        model = joblib.load("sensor_anomaly_model.pkl")
        print("Model loaded successfully.")
    except Exception as e:
        print(f"Error loading model: {e}")
        # In production, you might want to raise an error or handle this more gracefully
        # For now, we'll allow the app to start but predictions will fail
    yield
    # Clean up resources if needed
    pass

app = FastAPI(lifespan=lifespan)

class PredictionInput(BaseModel):
    source: str  # "sensor" or "camera"
    value: float = None # For sensor
    event: str = None   # For camera
    unit: str = None

@app.post("/predict")
async def predict(input_data: PredictionInput):
    if input_data.source == "sensor":
        if model is None:
            raise HTTPException(status_code=503, detail="Model not loaded")
        
        if input_data.value is None:
            raise HTTPException(status_code=400, detail="Sensor value is required")

        # Predict using the loaded model
        # Reshape to 2D array as expected by sklearn
        prediction = model.predict([[input_data.value]])
        
        # IsolationForest: 1 = Normal, -1 = Anomaly
        is_normal = prediction[0] == 1
        
        if is_normal:
            return {
                "label": "COLD",
                "uri": "https://schema.org/SafeCondition",
                "desc": "Normal Sensor Reading"
            }
        else:
            return {
                "label": "HOT",
                "uri": "https://schema.org/Warning",
                "desc": "Anomaly Detected"
            }

    elif input_data.source == "camera":
        if not input_data.event:
             raise HTTPException(status_code=400, detail="Camera event is required")
        
        event_key = input_data.event.lower()
        if event_key in SEMANTIC_MAP:
            return SEMANTIC_MAP[event_key]
        else:
            return {
                "label": "UNKNOWN",
                "uri": "https://schema.org/Thing",
                "desc": "Unknown Event"
            }
            
    else:
        raise HTTPException(status_code=400, detail="Invalid source. Must be 'sensor' or 'camera'")

@app.get("/health")
async def health():
    return {"status": "ok", "model_loaded": model is not None}
