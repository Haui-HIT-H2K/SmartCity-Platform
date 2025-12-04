SPDX-License-Identifier: Apache-2.0
# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

"""
RabbitMQ Consumer
Subscribes to sensor data queue and processes messages
"""

import pika
import logging
import time
import signal
import sys
from app import config
from app.core.transformer import SchemaTransformer
from app.clients.ml_client import MLServiceClient

logger = logging.getLogger(__name__)


class SensorConsumer:
    """RabbitMQ consumer for sensor data"""
    
    def __init__(self, host, port, queue_name):
        """
        Initialize consumer
        
        Args:
            host: RabbitMQ host
            port: RabbitMQ port
            queue_name: Queue name to consume from
        """
        self.host = host
        self.port = port
        self.queue_name = queue_name
        
        self.connection = None
        self.channel = None
        self.is_consuming = False
        
        # Initialize transformer and ML client
        self.transformer = SchemaTransformer()
        self.ml_client = MLServiceClient()
        
        # Statistics
        self.messages_processed = 0
        self.messages_failed = 0
        self.start_time = None
        
        # Setup signal handlers for graceful shutdown
        signal.signal(signal.SIGINT, self.signal_handler)
        signal.signal(signal.SIGTERM, self.signal_handler)
    
    def signal_handler(self, sig, frame):
        """Handle shutdown signals gracefully"""
        logger.info("\n🛑 Shutdown signal received. Closing connections...")
        self.stop()
        sys.exit(0)
    
    def connect(self):
        """
        Connect to RabbitMQ with retry logic
        
        Returns:
            bool: True if connected successfully
        """
        for attempt in range(config.MAX_RETRIES):
            try:
                # Create credentials
                credentials = pika.PlainCredentials(
                    config.RABBITMQ_USER,
                    config.RABBITMQ_PASS
                )
                
                # Create connection parameters
                parameters = pika.ConnectionParameters(
                    host=self.host,
                    port=self.port,
                    virtual_host=config.RABBITMQ_VHOST,
                    credentials=credentials,
                    connection_attempts=3,
                    retry_delay=1,
                    heartbeat=600,
                    blocked_connection_timeout=300
                )
                
                # Create connection
                self.connection = pika.BlockingConnection(parameters)
                self.channel = self.connection.channel()
                
                # Declare queue (ensure it exists)
                self.channel.queue_declare(queue=self.queue_name, durable=True)
                
                # Set QoS - prefetch count
                self.channel.basic_qos(prefetch_count=config.PREFETCH_COUNT)
                
                logger.info(
                    f"✓ Connected to RabbitMQ: {self.host}:{self.port} → {self.queue_name}"
                )
                return True
                
            except Exception as e:
                logger.warning(
                    f"Connection attempt {attempt + 1}/{config.MAX_RETRIES} failed: {e}"
                )
                if attempt < config.MAX_RETRIES - 1:
                    time.sleep(config.RETRY_DELAY)
        
        logger.error("❌ Failed to connect to RabbitMQ after all retries")
        return False
    
    def callback(self, ch, method, properties, body):
        """
        Callback function for processing messages
        
        Args:
            ch: Channel
            method: Delivery method
            properties: Message properties
            body: Message body (bytes)
        """
        try:
            # Transform message
            sensor_data, api_requests = self.transformer.transform(body)
            source_id = sensor_data["sourceId"]
            
            # Get predictions from ml-service
            results = self.ml_client.batch_predict(api_requests)
            
            # Log results
            for result in results:
                if result["success"]:
                    log_msg = self.transformer.format_prediction_log(
                        source_id,
                        result["metric_name"],
                        sensor_data["payload"][result["metric_name"]],
                        result["prediction"]
                    )
                    logger.info(log_msg)
                else:
                    logger.error(
                        f"[{source_id}] Failed to predict {result['metric_name']}: "
                        f"{result.get('error', 'Unknown error')}"
                    )
            
            # Acknowledge message
            ch.basic_ack(delivery_tag=method.delivery_tag)
            self.messages_processed += 1
            
            # Log progress every 100 messages
            if self.messages_processed % 100 == 0:
                self.log_statistics()
                
        except ValueError as e:
            # Schema transformation error - reject message
            logger.error(f"Invalid message format: {e}")
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
            self.messages_failed += 1
            
        except Exception as e:
            # Unexpected error - reject and requeue
            logger.error(f"Unexpected error processing message: {e}")
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
            self.messages_failed += 1
    
    def log_statistics(self):
        """Log processing statistics"""
        if self.start_time:
            elapsed = time.time() - self.start_time
            rate = self.messages_processed / elapsed if elapsed > 0 else 0
            logger.info(
                f"📊 Stats: Processed={self.messages_processed:,}, "
                f"Failed={self.messages_failed:,}, Rate={rate:.1f} msg/s"
            )
    
    def start(self):
        """Start consuming messages"""
        # Check ml-service health first
        logger.info("Checking ml-service availability...")
        if not self.ml_client.health_check():
            logger.error("❌ ML Service is not available. Exiting.")
            return False
        
        # Connect to RabbitMQ
        if not self.connect():
            return False
        
        # Start consuming
        logger.info(f"🚀 Starting to consume from queue: {self.queue_name}")
        logger.info(f"Prefetch count: {config.PREFETCH_COUNT}")
        logger.info("Waiting for messages... (Press Ctrl+C to stop)\n")
        
        self.is_consuming = True
        self.start_time = time.time()
        
        try:
            self.channel.basic_consume(
                queue=self.queue_name,
                on_message_callback=self.callback
            )
            
            self.channel.start_consuming()
            
        except KeyboardInterrupt:
            logger.info("\n🛑 Interrupted by user")
            self.stop()
        except Exception as e:
            logger.error(f"Error during consumption: {e}")
            self.stop()
            return False
        
        return True
    
    def stop(self):
        """Stop consuming and close connections"""
        self.is_consuming = False
        
        # Log final statistics
        logger.info("\n" + "=" * 60)
        logger.info("FINAL STATISTICS")
        logger.info("=" * 60)
        self.log_statistics()
        logger.info("=" * 60)
        
        # Close connections
        if self.channel and not self.channel.is_closed:
            try:
                self.channel.stop_consuming()
                logger.info("✓ Channel closed")
            except:
                pass
        
        if self.connection and not self.connection.is_closed:
            try:
                self.connection.close()
                logger.info("✓ Connection closed")
            except:
                pass
        
        # Close ML client
        self.ml_client.close()
        logger.info("✓ ML client closed")
