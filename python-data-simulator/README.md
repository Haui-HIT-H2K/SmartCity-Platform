<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

# IoT Data Simulator

Python tool để giả lập dữ liệu IoT cảm biến cho Smart City Platform.

## Tính năng

- ✅ Gửi **40 triệu requests** tới RabbitMQ
- ✅ Multi-threading (20 threads: 10/edge)
- ✅ Kết nối tới 2 RabbitMQ Edge Nodes
- ✅ Auto-retry khi mất kết nối
- ✅ Real-time progress tracking
- ✅ Performance metrics

## Cài đặt

```bash
pip install -r requirements.txt
```

## Cấu hình

File `config.py` chứa các thông số:
- `TOTAL_REQUESTS = 40_000_000` - Tổng số messages
- `NUM_THREADS = 20` - Số threads
- RabbitMQ connections (Edge 1: port 5672, Edge 2: port 5673)

## Chạy Simulator

```bash
python main.py
```

## Dữ liệu mẫu

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

## Cấu trúc Project

```
python-data-simulator/
├── main.py              # Script chính
├── config.py            # Cấu hình
├── requirements.txt     # Dependencies
└── README.md           # Hướng dẫn
```

## Lưu ý

- Đảm bảo RabbitMQ đang chạy trên Docker
- Port mapping: Edge-1 (5672), Edge-2 (5673)
- Credentials: `edge_user/edge_pass`
