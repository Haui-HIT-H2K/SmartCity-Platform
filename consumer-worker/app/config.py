SPDX-License-Identifier: Apache-2.0
# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

"""
Configuration for Consumer Worker
Connects python-data-simulator (RabbitMQ) to ml-service (HTTP API)
"""

import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# ============================================================
# RABBITMQ SETTINGS
# ============================================================

# Edge Node 1
RABBITMQ_HOST_1 = os.getenv("RABBITMQ_HOST_1", "localhost")
RABBITMQ_PORT_1 = int(os.getenv("RABBITMQ_PORT_1", 5672))
RABBITMQ_QUEUE_1 = os.getenv("RABBITMQ_QUEUE_1", "sensor_queue_1")

# Edge Node 2
RABBITMQ_HOST_2 = os.getenv("RABBITMQ_HOST_2", "localhost")
RABBITMQ_PORT_2 = int(os.getenv("RABBITMQ_PORT_2", 5673))
RABBITMQ_QUEUE_2 = os.getenv("RABBITMQ_QUEUE_2", "sensor_queue_2")

# Credentials
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "edge_user")
RABBITMQ_PASS = os.getenv("RABBITMQ_PASS", "edge_pass")
RABBITMQ_VHOST = os.getenv("RABBITMQ_VHOST", "/")

# ============================================================
# ML SERVICE SETTINGS
# ============================================================

ML_SERVICE_URL = os.getenv("ML_SERVICE_URL", "http://localhost:8000")
ML_SERVICE_PREDICT_ENDPOINT = f"{ML_SERVICE_URL}/predict"
ML_SERVICE_HEALTH_ENDPOINT = f"{ML_SERVICE_URL}/health"

# ============================================================
# CONSUMER SETTINGS
# ============================================================

# Which queue to consume from (1 or 2)
CONSUME_QUEUE = int(os.getenv("CONSUME_QUEUE", 1))

# Prefetch count (how many messages to fetch at once)
PREFETCH_COUNT = int(os.getenv("PREFETCH_COUNT", 10))

# Connection settings
MAX_RETRIES = int(os.getenv("MAX_RETRIES", 3))
RETRY_DELAY = int(os.getenv("RETRY_DELAY", 2))  # seconds
CONNECTION_TIMEOUT = int(os.getenv("CONNECTION_TIMEOUT", 10))  # seconds

# HTTP request timeout
HTTP_TIMEOUT = int(os.getenv("HTTP_TIMEOUT", 5))  # seconds

# ============================================================
# LOGGING SETTINGS
# ============================================================

LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")

# ============================================================
# METRIC MAPPING
# ============================================================

# Map payload keys to metric_type for ml-service
METRIC_MAPPING = {
    "temperature": "temperature",
    "humidity": "humidity",
    "co2_level": "co2"
}

# ============================================================
# HELPER FUNCTIONS
# ============================================================

def get_rabbitmq_config():
    """Get RabbitMQ configuration based on CONSUME_QUEUE"""
    if CONSUME_QUEUE == 1:
        return {
            "host": RABBITMQ_HOST_1,
            "port": RABBITMQ_PORT_1,
            "queue": RABBITMQ_QUEUE_1
        }
    else:
        return {
            "host": RABBITMQ_HOST_2,
            "port": RABBITMQ_PORT_2,
            "queue": RABBITMQ_QUEUE_2
        }

def print_config():
    """Print current configuration"""
    config = get_rabbitmq_config()
    print("=" * 60)
    print("CONSUMER WORKER CONFIGURATION")
    print("=" * 60)
    print(f"RabbitMQ Host:  {config['host']}:{config['port']}")
    print(f"RabbitMQ Queue: {config['queue']}")
    print(f"RabbitMQ User:  {RABBITMQ_USER}")
    print(f"ML Service:     {ML_SERVICE_URL}")
    print(f"Prefetch Count: {PREFETCH_COUNT}")
    print(f"Log Level:      {LOG_LEVEL}")
    print("=" * 60)
