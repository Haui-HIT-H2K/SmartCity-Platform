SPDX-License-Identifier: Apache-2.0
# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

#!/usr/bin/env python3
"""
Quick test script to send a test message to RabbitMQ
This simulates what python-data-simulator does
"""

import pika
import json
import time

# RabbitMQ connection settings
RABBITMQ_HOST = "localhost"
RABBITMQ_PORT = 5672
RABBITMQ_USER = "edge_user"
RABBITMQ_PASS = "edge_pass"
QUEUE_NAME = "sensor_queue_1"

def send_test_message():
    """Send a single test message to RabbitMQ"""
    
    # Create credentials
    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    
    # Create connection parameters
    parameters = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        virtual_host="/",
        credentials=credentials
    )
    
    # Connect to RabbitMQ
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()
    
    # Declare queue
    channel.queue_declare(queue=QUEUE_NAME, durable=True)
    
    # Create test message (simulating simulator output)
    test_message = {
        "sourceId": "SENSOR_TEST_001",
        "payload": {
            "temperature": 28.5,
            "humidity": 65,
            "co2_level": 420
        },
        "timestamp": int(time.time() * 1000)
    }
    
    # Publish message
    channel.basic_publish(
        exchange='',
        routing_key=QUEUE_NAME,
        body=json.dumps(test_message),
        properties=pika.BasicProperties(delivery_mode=2)
    )
    
    print("✓ Test message sent to RabbitMQ")
    print(f"  Queue: {QUEUE_NAME}")
    print(f"  Message: {json.dumps(test_message, indent=2)}")
    
    # Close connection
    connection.close()

if __name__ == "__main__":
    print("Sending test message to RabbitMQ...")
    send_test_message()
