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
Configuration cho IoT Data Simulator
Giả lập 40 triệu requests tới RabbitMQ
"""

# ============================================================
# RABBITMQ CONNECTION SETTINGS
# ============================================================

# Edge Node 1 (localhost:5672)
EDGE1_HOST = "localhost"
EDGE1_PORT = 5672
EDGE1_QUEUE = "city-data-queue-1"

# Edge Node 2 (localhost:5673)
EDGE2_HOST = "localhost"
EDGE2_PORT = 5673
EDGE2_QUEUE = "city-data-queue-2"

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
# ANOMALY INJECTION SETTINGS
# ============================================================

# Enable anomaly injection mode (True = inject anomalies)
INJECT_ANOMALY = True

# Probability of generating anomaly (0.0 - 1.0)
# Example: 0.1 = 10% of data will be anomalies
ANOMALY_PROBABILITY = 0.15

# Anomaly ranges - values that ML model will classify as HOT
# These are outside the normal training ranges

# Temperature anomaly (ML trained on 15-35°C)
ANOMALY_TEMP_LOW = -10.0   # Very cold (below 0°C)
ANOMALY_TEMP_HIGH = 60.0   # Very hot (above 50°C)

# Humidity anomaly (ML trained on 30-80%)
ANOMALY_HUMIDITY_LOW = 5    # Very dry (below 20%)
ANOMALY_HUMIDITY_HIGH = 100 # Saturated (above 90%)

# CO2 anomaly (ML trained on 350-900 ppm)
ANOMALY_CO2_LOW = 200       # Too low (unusual)
ANOMALY_CO2_HIGH = 3000     # Dangerous level (above 1500 ppm)

# ============================================================
# PERFORMANCE SETTINGS
# ============================================================

# Retry settings
MAX_RETRIES = 3
RETRY_DELAY = 1  # seconds

# Connection timeout
CONNECTION_TIMEOUT = 10  # seconds
