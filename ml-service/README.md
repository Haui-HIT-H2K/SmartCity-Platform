# ML Service - Multi-Model Anomaly Detection

FastAPI service for Smart City sensor anomaly detection supporting **3 metrics**: Temperature, Humidity, and CO2.

## 🚀 Features

- ✅ **Multi-Model Support**: 3 IsolationForest models for different sensor types
- ✅ **RESTful API**: FastAPI with automatic OpenAPI documentation
- ✅ **Semantic Web**: Returns Schema.org URIs for interoperability
- ✅ **Docker Ready**: Containerized deployment
- ✅ **Health Monitoring**: Track model loading status

---

## 📊 Supported Metrics

| Metric | Range (Simulator) | Model | Status |
|--------|------------------|-------|--------|
| **Temperature** | 15-45°C | IsolationForest | ✅ Trained |
| **Humidity** | 30-95% | IsolationForest | ✅ Trained |
| **CO2** | 350-1000 ppm | IsolationForest | ✅ Trained |

---

## 🛠️ Installation

### Option 1: Docker (Recommended)

```bash
# Build image
docker build -t ml-service:latest .

# Run container
docker run -d -p 8000:8000 --name ml-service ml-service:latest
```

### Option 2: Local Development

```bash
# Install dependencies
pip install -r requirements.txt

# Train models (first time only)
python3 train_models.py

# Start server
uvicorn app:app --reload --host 0.0.0.0 --port 8000
```

---

## 📚 API Usage

### Health Check

```bash
GET /health
```

**Response:**
```json
{
  "status": "ok",
  "models_loaded": {
    "temperature": true,
    "humidity": true,
    "co2": true
  },
  "total_models": 3
}
```

### Predict Anomaly

```bash
POST /predict
Content-Type: application/json
```

**Request Body:**
```json
{
  "source": "sensor",
  "value": 28.5,
  "metric_type": "temperature"  // "temperature", "humidity", or "co2"
}
```

**Response (Normal):**
```json
{
  "label": "COLD",
  "uri": "https://schema.org/SafeCondition",
  "desc": "Normal Temperature Reading",
  "metric_type": "temperature",
  "value": 28.5
}
```

**Response (Anomaly):**
```json
{
  "label": "HOT",
  "uri": "https://schema.org/Warning",
  "desc": "Temperature Anomaly Detected",
  "metric_type": "temperature",
  "value": 100.0
}
```

---

## 🧪 Examples

### Temperature Detection

```bash
# Normal temperature
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":28.5,"metric_type":"temperature"}'

# High temperature (anomaly)
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":80.0,"metric_type":"temperature"}'
```

### Humidity Detection

```bash
# Normal humidity
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":65,"metric_type":"humidity"}'

# Low humidity (anomaly)
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":10,"metric_type":"humidity"}'
```

### CO2 Detection

```bash
# Normal CO2
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":420,"metric_type":"co2"}'

# High CO2 (anomaly)
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":2000,"metric_type":"co2"}'
```

### Backward Compatibility

```bash
# Without metric_type (defaults to temperature)
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":28.5}'
```

---

## 🤖 Model Training

Models are trained using `train_models.py` with synthetic data based on simulator ranges:

```bash
python3 train_models.py
```

**Training Parameters:**
- **Algorithm**: IsolationForest (sklearn)
- **Samples**: 1,000 per model
- **Contamination**: 5%
- **Estimators**: 100

**Generated Files:**
- `temperature_model.pkl` (1.4MB)
- `humidity_model.pkl` (1.4MB)
- `co2_model.pkl` (1.4MB)

---

## 🔗 Integration with python-data-simulator

The ml-service is designed to work with data from `python-data-simulator`:

**Simulator Output:**
```json
{
  "sourceId": "SENSOR_042",
  "payload": {
    "temperature": 28.3,
    "humidity": 65,
    "co2_level": 420
  },
  "timestamp": 1700000000000
}
```

**Transformation Required:**
Split into 3 API calls (one per metric) via a consumer worker:
```python
# Temperature
POST /predict {"source":"sensor","value":28.3,"metric_type":"temperature"}

# Humidity
POST /predict {"source":"sensor","value":65,"metric_type":"humidity"}

# CO2
POST /predict {"source":"sensor","value":420,"metric_type":"co2"}
```

---

## 📁 Project Structure

```
ml-service/
├── app.py                    # FastAPI application
├── train_models.py           # Multi-model training script
├── requirements.txt          # Python dependencies
├── Dockerfile               # Container configuration
├── .dockerignore           # Docker ignore rules
├── temperature_model.pkl    # Trained temperature model
├── humidity_model.pkl       # Trained humidity model
├── co2_model.pkl           # Trained CO2 model
└── README.md               # This file
```

---

## 🌐 API Documentation

Once running, visit:
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

---

## 🔧 Configuration

### Environment Variables

```bash
# Optional: Port configuration
export PORT=8000

# Optional: Model paths
export TEMP_MODEL_PATH=temperature_model.pkl
export HUMIDITY_MODEL_PATH=humidity_model.pkl
export CO2_MODEL_PATH=co2_model.pkl
```

---

## 🐛 Troubleshooting

### Models not loading

```bash
# Check model files exist
ls -lh *.pkl

# Retrain models if missing
python3 train_models.py
```

### Import errors

```bash
# Reinstall dependencies
pip install --no-cache-dir -r requirements.txt
```

### Port already in use

```bash
# Change port
uvicorn app:app --port 8001
```

---

## 📈 Performance

- **Prediction latency**: ~5-10ms per request
- **Throughput**: ~100-200 requests/second
- **Memory usage**: ~150MB (3 models loaded)

---

## 🎯 Roadmap

- [ ] Multi-variate anomaly detection (combined features)
- [ ] Online learning / model retraining
- [ ] Batch prediction endpoint
- [ ] Historical data analysis
- [ ] Grafana dashboard integration

---

## 📄 License

Apache 2.0 License

---

## 👥 Contributors

Smart City Platform Team

**Last Updated**: 2025-11-28
