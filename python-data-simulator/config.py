"""
Configuration cho IoT Data Simulator
Giả lập 40 triệu requests tới RabbitMQ
"""

# ============================================================
# RABBITMQ CONNECTION SETTINGS
# ============================================================

# Edge Node 1 (localhost:5672)
EDGE1_HOST = "localhost"
EDGE1_PORT = 5672
EDGE1_QUEUE = "sensor_queue_1"

# Edge Node 2 (localhost:5673)
EDGE2_HOST = "localhost"
EDGE2_PORT = 5673
EDGE2_QUEUE = "sensor_queue_2"

# Credentials
RABBITMQ_USER = "edge_user"
RABBITMQ_PASS = "edge_pass"
RABBITMQ_VHOST = "/"

# ============================================================
# SIMULATION SETTINGS
# ============================================================

# Tổng số request cần gửi (40 triệu)
TOTAL_REQUESTS = 40_000_000

# Số lượng threads (20 threads: 10 cho mỗi edge)
NUM_THREADS = 20

# Số threads cho mỗi edge node
THREADS_PER_EDGE = NUM_THREADS // 2

# ============================================================
# SENSOR DATA SETTINGS
# ============================================================

# Số lượng sensors (SENSOR_001 -> SENSOR_1000)
NUM_SENSORS = 1000

# Temperature range (°C)
TEMP_MIN = 15.0
TEMP_MAX = 45.0

# Humidity range (%)
HUMIDITY_MIN = 30
HUMIDITY_MAX = 95

# CO2 Level range (ppm)
CO2_MIN = 350
CO2_MAX = 1000

# ============================================================
# PERFORMANCE SETTINGS
# ============================================================

# Retry settings
MAX_RETRIES = 3
RETRY_DELAY = 1  # seconds

# Connection timeout
CONNECTION_TIMEOUT = 10  # seconds
