
<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

# ML Service - Multi-Model Anomaly Detection

Service FastAPI cho phát hiện bất thường sensor Smart City hỗ trợ **3 metric**: Temperature, Humidity, và CO2.

## 🚀 Tính năng

- ✅ **Hỗ trợ Multi-Model**: 3 model IsolationForest cho các loại sensor khác nhau
- ✅ **RESTful API**: FastAPI với automatic OpenAPI documentation
- ✅ **Semantic Web**: Trả về Schema.org URI cho khả năng tương tác
- ✅ **Sẵn sàng Docker**: Containerized deployment
- ✅ **Giám sát Health**: Theo dõi trạng thái loading model

---

## 📊 Metric được Hỗ trợ

| Metric | Phạm vi (Simulator) | Model | Trạng thái |
|--------|---------------------|-------|-----------|
| **Temperature** | 15-45°C | IsolationForest | ✅ Đã train |
| **Humidity** | 30-95% | IsolationForest | ✅ Đã train |
| **CO2** | 350-1000 ppm | IsolationForest | ✅ Đã train |

---

## 🛠️ Cài đặt & Thiết lập Lần đầu

Nhờ có `entrypoint.sh` mới, service hiện **tự động khắc phục**: mỗi khi container khởi động, nó kiểm tra 3 file model và tự động train nếu bị thiếu. Điều đó có nghĩa là clone mới hầu như không cần thao tác thủ công.

### ⚡ Bắt đầu Nhanh (Docker Compose – Khuyến nghị)

```bash
# Từ thư mục gốc repository
docker compose up -d --build ml-service
```

Điều gì xảy ra tự động:

- Dependency được cài đặt bên trong container.
- `entrypoint.sh` tìm kiếm `app/models/*.pkl`. Nếu bị thiếu, nó chạy `python3 /app/train_models.py` cho bạn.
- Sau khi training (hoặc sử dụng lại model hiện có) nó khởi động FastAPI với Uvicorn.

Các lệnh hữu ích tiếp theo:

```bash
# Tail log để xem output auto-training
docker logs -f smart-city-ml

# Kiểm tra model + API status
curl http://localhost:8000/health
```

> 💡 Cần rebuild? Chỉ cần chạy lại `docker compose build ml-service && docker compose up -d ml-service`. Entrypoint sẽ phát hiện model đã tồn tại và bỏ qua việc retrain.

### 🧑‍💻 Local Development (không dùng Docker)

Bạn vẫn có thể chạy service trực tiếp trên máy khi debug model:

```bash
# 1. Cài đặt dependencies
pip install -r requirements.txt

# 2. (Tùy chọn) Retrain model nếu bạn thay đổi training script
python3 train_models.py

# 3. Khởi động API
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

- Repository đi kèm với pre-trained model, nên bước 2 là tùy chọn trừ khi bạn muốn tạo lại chúng.
- Live reload được bật nên chỉnh sửa trong `app/` được nhận ngay lập tức.

---

## 📚 Cách sử dụng API

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
  "metric_type": "temperature"  // "temperature", "humidity", hoặc "co2"
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

## 🧪 Ví dụ

### Temperature Detection

```bash
# Temperature bình thường
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":28.5,"metric_type":"temperature"}'

# Temperature cao (anomaly)
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":80.0,"metric_type":"temperature"}'
```

### Humidity Detection

```bash
# Humidity bình thường
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":65,"metric_type":"humidity"}'

# Humidity thấp (anomaly)
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":10,"metric_type":"humidity"}'
```

### CO2 Detection

```bash
# CO2 bình thường
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":420,"metric_type":"co2"}'

# CO2 cao (anomaly)
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":2000,"metric_type":"co2"}'
```

### Backward Compatibility

```bash
# Không có metric_type (mặc định là temperature)
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":28.5}'
```

---

## 🤖 Model Training

Model được train bằng `train_models.py` với synthetic data dựa trên phạm vi simulator:

```bash
python3 train_models.py
```

**Tham số Training:**
- **Algorithm**: IsolationForest (sklearn)
- **Samples**: 1,000 mỗi model
- **Contamination**: 5%
- **Estimators**: 100

**File được Tạo:**
- `temperature_model.pkl` (1.4MB)
- `humidity_model.pkl` (1.4MB)
- `co2_model.pkl` (1.4MB)

---

## 🔗 Tích hợp với python-data-simulator

ML-service được thiết kế để làm việc với dữ liệu từ `python-data-simulator`:

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

**Transformation Cần thiết:**
Tách thành 3 API call (một cho mỗi metric) qua consumer worker:
```python
# Temperature
POST /predict {"source":"sensor","value":28.3,"metric_type":"temperature"}

# Humidity
POST /predict {"source":"sensor","value":65,"metric_type":"humidity"}

# CO2
POST /predict {"source":"sensor","value":420,"metric_type":"co2"}
```

---

## 📁 Cấu trúc Project

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
└── README.md               # File này
```

---

## 🌐 API Documentation

Khi đang chạy, truy cập:
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

---

## 🔧 Cấu hình

### Biến Môi trường

```bash
# Tùy chọn: Cấu hình port
export PORT=8000

# Tùy chọn: Model path
export TEMP_MODEL_PATH=temperature_model.pkl
export HUMIDITY_MODEL_PATH=humidity_model.pkl
export CO2_MODEL_PATH=co2_model.pkl
```

---

## 🐛 Xử lý Sự cố

### Model không load

```bash
# Kiểm tra file model tồn tại
ls -lh *.pkl

# Retrain model nếu thiếu
python3 train_models.py
```

### Lỗi Import

```bash
# Cài đặt lại dependencies
pip install --no-cache-dir -r requirements.txt
```

### Port đã được sử dụng

```bash
# Đổi port
uvicorn app:app --port 8001
```

---

## 📈 Hiệu năng

- **Prediction latency**: ~5-10ms mỗi request
- **Throughput**: ~100-200 request/giây
- **Memory usage**: ~150MB (3 model đã load)

---

## 🎯 Roadmap

- [ ] Multi-variate anomaly detection (combined feature)
- [ ] Online learning / model retraining
- [ ] Batch prediction endpoint
- [ ] Historical data analysis
- [ ] Grafana dashboard integration

---

## 📄 Giấy phép

Apache 2.0 License

---

## 👥 Contributors

Smart City Platform Team

**Cập nhật lần cuối**: 2025-11-28
