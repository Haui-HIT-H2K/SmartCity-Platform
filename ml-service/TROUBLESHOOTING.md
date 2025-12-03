# Troubleshooting ML Service

## Lỗi: "exec ./entrypoint.sh: no such file or directory"

### Nguyên nhân

Lỗi này thường xảy ra khi:
1. **Line endings sai**: Trên Windows, file có thể có CRLF thay vì LF (bash cần LF)
2. **Đường dẫn tương đối**: Docker có thể không tìm thấy file với đường dẫn tương đối `./entrypoint.sh`
3. **Quyền thực thi**: File không có quyền thực thi

### Cách khắc phục

Dockerfile đã được cập nhật để tự động xử lý:
- Chuyển đổi line endings từ CRLF sang LF
- Sử dụng đường dẫn tuyệt đối `/app/entrypoint.sh`
- Đảm bảo quyền thực thi được set đúng

Nếu vẫn gặp lỗi, rebuild container:

```bash
docker-compose build --no-cache ml-service
docker-compose up -d ml-service
```

### Kiểm tra

```bash
# Kiểm tra logs
docker logs smart-city-ml

# Kiểm tra file trong container
docker exec smart-city-ml ls -la /app/entrypoint.sh
docker exec smart-city-ml file /app/entrypoint.sh
```

---

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

