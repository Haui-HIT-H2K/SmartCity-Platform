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
IoT Data Simulator - Main Script
Giả lập 40 triệu requests tới RabbitMQ với multi-threading
"""

import json
import random
import time
import threading
from datetime import datetime
import pika
import config


class SensorSimulator(threading.Thread):
    """
    Worker thread giả lập sensor gửi dữ liệu vào RabbitMQ
    """
    
    def __init__(self, thread_id, host, port, queue_name, request_limit):
        """
        Args:
            thread_id: ID của thread
            host: RabbitMQ host
            port: RabbitMQ port
            queue_name: Tên queue
            request_limit: Số lượng messages cần gửi
        """
        super().__init__()
        self.thread_id = thread_id
        self.host = host
        self.port = port
        self.queue_name = queue_name
        self.request_limit = request_limit
        self.sent_count = 0
        
    def generate_sensor_data(self):
        """
        Sinh dữ liệu cảm biến giả (tối ưu tốc độ)
        Hỗ trợ inject anomaly để tạo dữ liệu bất thường
        """
        sensor_id = f"SENSOR_{random.randint(1, config.NUM_SENSORS):03d}"
        
        # Check if we should inject anomaly
        inject_anomaly = (
            config.INJECT_ANOMALY and 
            random.random() < config.ANOMALY_PROBABILITY
        )
        
        if inject_anomaly:
            # Generate anomaly data (extreme values for HOT classification)
            # Randomly choose high or low anomaly for each metric
            if random.random() < 0.5:
                temperature = round(random.uniform(config.ANOMALY_TEMP_HIGH - 10, config.ANOMALY_TEMP_HIGH), 1)
            else:
                temperature = round(random.uniform(config.ANOMALY_TEMP_LOW, config.ANOMALY_TEMP_LOW + 10), 1)
            
            if random.random() < 0.5:
                humidity = random.randint(config.ANOMALY_HUMIDITY_HIGH - 10, config.ANOMALY_HUMIDITY_HIGH)
            else:
                humidity = random.randint(config.ANOMALY_HUMIDITY_LOW, config.ANOMALY_HUMIDITY_LOW + 10)
            
            if random.random() < 0.5:
                co2_level = random.randint(config.ANOMALY_CO2_HIGH - 500, config.ANOMALY_CO2_HIGH)
            else:
                co2_level = random.randint(config.ANOMALY_CO2_LOW, config.ANOMALY_CO2_LOW + 100)
        else:
            # Generate normal data
            temperature = round(random.uniform(config.TEMP_MIN, config.TEMP_MAX), 1)
            humidity = random.randint(config.HUMIDITY_MIN, config.HUMIDITY_MAX)
            co2_level = random.randint(config.CO2_MIN, config.CO2_MAX)
        
        data = {
            "sourceId": sensor_id,
            "payload": {
                "temperature": temperature,
                "humidity": humidity,
                "co2_level": co2_level
            },
            "timestamp": int(time.time() * 1000),  # Unix time milliseconds
            "is_anomaly": inject_anomaly  # Flag for debugging (optional)
        }
        
        return json.dumps(data)
    
    def connect_rabbitmq(self):
        """
        Tạo kết nối tới RabbitMQ với retry
        """
        for attempt in range(config.MAX_RETRIES):
            try:
                credentials = pika.PlainCredentials(
                    config.RABBITMQ_USER, 
                    config.RABBITMQ_PASS
                )
                
                parameters = pika.ConnectionParameters(
                    host=self.host,
                    port=self.port,
                    virtual_host=config.RABBITMQ_VHOST,
                    credentials=credentials,
                    connection_attempts=3,
                    retry_delay=1
                )
                
                connection = pika.BlockingConnection(parameters)
                channel = connection.channel()
                
                # Declare queue (durable)
                channel.queue_declare(queue=self.queue_name, durable=True)
                
                print(f"[Thread-{self.thread_id}] Connected to {self.host}:{self.port} → {self.queue_name}")
                
                return connection, channel
                
            except Exception as e:
                print(f"[Thread-{self.thread_id}] Connection attempt {attempt + 1} failed: {e}")
                if attempt < config.MAX_RETRIES - 1:
                    time.sleep(config.RETRY_DELAY)
                else:
                    raise
    
    def run(self):
        """
        Main execution của thread
        """
        print(f"[Thread-{self.thread_id}] Starting - Target: {self.request_limit:,} messages")
        
        connection = None
        channel = None
        
        try:
            # Kết nối RabbitMQ
            connection, channel = self.connect_rabbitmq()
            
            # Gửi messages
            start_time = time.time()
            
            for i in range(self.request_limit):
                # Generate sensor data
                message = self.generate_sensor_data()
                
                # Publish message
                channel.basic_publish(
                    exchange='',
                    routing_key=self.queue_name,
                    body=message,
                    properties=pika.BasicProperties(
                        delivery_mode=2,  # Make message persistent
                    )
                )
                
                self.sent_count += 1
                
                # Log progress mỗi 100,000 messages
                if self.sent_count % 100_000 == 0:
                    elapsed = time.time() - start_time
                    rate = self.sent_count / elapsed if elapsed > 0 else 0
                    print(f"[Thread-{self.thread_id}] Progress: {self.sent_count:,}/{self.request_limit:,} "
                          f"({rate:,.0f} msg/s)")
            
            # Hoàn thành
            elapsed = time.time() - start_time
            rate = self.sent_count / elapsed if elapsed > 0 else 0
            
            print(f"[Thread-{self.thread_id}] ✓ Completed: {self.sent_count:,} messages "
                  f"in {elapsed:.2f}s ({rate:,.0f} msg/s)")
            
        except Exception as e:
            print(f"[Thread-{self.thread_id}] ✗ Error: {e}")
            
        finally:
            # Đóng kết nối
            if connection and not connection.is_closed:
                connection.close()
                print(f"[Thread-{self.thread_id}] Connection closed")


def main():
    """
    Main execution
    """
    print("=" * 80)
    print("IoT DATA SIMULATOR - Smart City Platform")
    print("=" * 80)
    print(f"Total Requests: {config.TOTAL_REQUESTS:,}")
    print(f"Number of Threads: {config.NUM_THREADS}")
    print(f"Edge Node 1: {config.EDGE1_HOST}:{config.EDGE1_PORT} → {config.EDGE1_QUEUE}")
    print(f"Edge Node 2: {config.EDGE2_HOST}:{config.EDGE2_PORT} → {config.EDGE2_QUEUE}")
    
    # Show anomaly injection status
    if config.INJECT_ANOMALY:
        print("-" * 80)
        print("🔥 ANOMALY INJECTION: ENABLED")
        print(f"   Probability: {config.ANOMALY_PROBABILITY * 100:.0f}% of data will be anomalies")
        print(f"   Expected HOT records: ~{int(config.TOTAL_REQUESTS * config.ANOMALY_PROBABILITY):,}")
        print("-" * 80)
    else:
        print("-" * 80)
        print("✓ ANOMALY INJECTION: DISABLED (all normal data)")
        print("-" * 80)
    
    print("=" * 80)
    
    # Chia quota cho mỗi thread
    requests_per_thread = config.TOTAL_REQUESTS // config.NUM_THREADS
    
    print(f"\nRequests per thread: {requests_per_thread:,}")
    print(f"Threads per edge: {config.THREADS_PER_EDGE}")
    print("\nStarting simulation...\n")
    
    # Tạo threads
    threads = []
    thread_id = 1
    
    # Threads cho Edge Node 1
    for i in range(config.THREADS_PER_EDGE):
        thread = SensorSimulator(
            thread_id=thread_id,
            host=config.EDGE1_HOST,
            port=config.EDGE1_PORT,
            queue_name=config.EDGE1_QUEUE,
            request_limit=requests_per_thread
        )
        threads.append(thread)
        thread_id += 1
    
    # Threads cho Edge Node 2
    for i in range(config.THREADS_PER_EDGE):
        thread = SensorSimulator(
            thread_id=thread_id,
            host=config.EDGE2_HOST,
            port=config.EDGE2_PORT,
            queue_name=config.EDGE2_QUEUE,
            request_limit=requests_per_thread
        )
        threads.append(thread)
        thread_id += 1
    
    # Bắt đầu đo thời gian
    overall_start = time.time()
    
    # Start tất cả threads
    for thread in threads:
        thread.start()
    
    # Join và đợi tất cả threads hoàn thành
    for thread in threads:
        thread.join()
    
    # Tính tổng thời gian
    overall_elapsed = time.time() - overall_start
    
    # Tính tổng số messages đã gửi
    total_sent = sum(thread.sent_count for thread in threads)
    
    # Tính throughput
    throughput = total_sent / overall_elapsed if overall_elapsed > 0 else 0
    
    # In báo cáo cuối cùng
    print("\n" + "=" * 80)
    print("SIMULATION COMPLETED!")
    print("=" * 80)
    print(f"Total Messages Sent: {total_sent:,} / {config.TOTAL_REQUESTS:,}")
    print(f"Total Time: {overall_elapsed:.2f} seconds")
    print(f"Average Throughput: {throughput:,.0f} messages/second")
    print(f"Completion Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 80)


if __name__ == "__main__":
    main()
