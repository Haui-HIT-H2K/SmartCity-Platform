SPDX-License-Identifier: Apache-2.0
# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

"""
Quick Test Script for Data Simulator
Sends only 10,000 messages for quick testing
"""

import json
import random
import time
from datetime import datetime
import pika

# Config
RABBITMQ_HOST = "localhost"
RABBITMQ_PORT = 5672
RABBITMQ_USER = "edge_user"
RABBITMQ_PASS = "edge_pass"
QUEUE_NAME = "city-data-queue-1"
NUM_MESSAGES = 10000

print("=" * 80)
print("QUICK TEST - IoT Data Simulator")
print("=" * 80)
print(f"Target: {NUM_MESSAGES:,} messages to {RABBITMQ_HOST}:{RABBITMQ_PORT}")
print("=" * 80)

try:
    # Connect to RabbitMQ
    print("\nConnecting to RabbitMQ...")
    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    parameters = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        credentials=credentials
    )
    
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()
    channel.queue_declare(queue=QUEUE_NAME, durable=True)
    
    print(f"✓ Connected to RabbitMQ")
    print(f"✓ Queue '{QUEUE_NAME}' ready")
    print(f"\nSending {NUM_MESSAGES:,} messages...\n")
    
    start_time = time.time()
    
    for i in range(NUM_MESSAGES):
        # Generate sensor data
        data = {
            "sourceId": f"SENSOR_{random.randint(1, 100):03d}",
            "payload": {
                "temperature": round(random.uniform(15.0, 45.0), 1),
                "humidity": random.randint(30, 95),
                "co2_level": random.randint(350, 1000)
            },
            "timestamp": int(time.time() * 1000)
        }
        
        # Publish
        channel.basic_publish(
            exchange='',
            routing_key=QUEUE_NAME,
            body=json.dumps(data),
            properties=pika.BasicProperties(delivery_mode=2)
        )
        
        # Progress
        if (i + 1) % 1000 == 0:
            elapsed = time.time() - start_time
            rate = (i + 1) / elapsed
            print(f"Progress: {i + 1:,}/{NUM_MESSAGES:,} ({rate:,.0f} msg/s)")
    
    elapsed = time.time() - start_time
    rate = NUM_MESSAGES / elapsed
    
    print(f"\n✓ Completed: {NUM_MESSAGES:,} messages in {elapsed:.2f}s ({rate:,.0f} msg/s)")
    print(f"✓ Time: {datetime.now().strftime('%H:%M:%S')}")
    
    connection.close()
    print("\n" + "=" * 80)
    print("SUCCESS! Check backend logs and UI for data.")
    print("=" * 80)
    
except Exception as e:
    print(f"\n✗ ERROR: {e}")
    print(f"\nTroubleshooting:")
    print(f"1. Check RabbitMQ is running: docker ps | grep rabbit")
    print(f"2. Check port {RABBITMQ_PORT} is accessible")
    print(f"3. Verify credentials: {RABBITMQ_USER}:{RABBITMQ_PASS}")
