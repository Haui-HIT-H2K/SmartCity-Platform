# Troubleshooting ML Service

## Lỗi: "Temperature model not loaded" (503 Service Unavailable)

### Nguyên nhân

1. **Models chưa được train**: Khi clone project về, models có thể chưa có hoặc bị thiếu
2. **Version mismatch**: Models được train với scikit-learn version khác với version trong container

### Cách khắc phục

#### Cách 1: Tự động (Khuyến nghị)

ML service sẽ tự động train models khi khởi động nếu models chưa có:

```bash
# Rebuild và restart ML service
docker-compose build ml-service
docker-compose up -d ml-service
```

#### Cách 2: Train models thủ công

Nếu muốn train models trước khi build:

```bash
cd ml-service
pip install -r requirements.txt
python train_models.py
```

Sau đó rebuild container:

```bash
docker-compose build ml-service
docker-compose up -d ml-service
```

### Kiểm tra models đã được load

```bash
# Kiểm tra health endpoint
curl http://localhost:8000/health

# Kết quả mong đợi:
# {
#   "status": "ok",
#   "models_loaded": {
#     "temperature": true,
#     "humidity": true,
#     "co2": true
#   },
#   "total_models": 3
# }
```

### Kiểm tra logs

```bash
docker logs smart-city-ml

# Tìm dòng:
# ✓ Temperature model loaded from temperature_model.pkl
# ✓ Humidity model loaded from humidity_model.pkl
# ✓ Co2 model loaded from co2_model.pkl
# Total models loaded: 3/3
```

### Lưu ý

- Models được lưu trong `ml-service/app/models/*.pkl`
- Models đã được commit vào git (không bị gitignore)
- Nếu models không có, entrypoint script sẽ tự động train khi container khởi động

