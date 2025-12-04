SPDX-License-Identifier: Apache-2.0
# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

"""
Schema Transformer
Converts RabbitMQ messages from python-data-simulator to ml-service API requests
"""

import json
from app import config


class SchemaTransformer:
    """Transform sensor data from RabbitMQ format to ml-service API format"""
    
    def __init__(self):
        self.metric_mapping = config.METRIC_MAPPING
    
    def transform(self, message_body):
        """
        Transform RabbitMQ message to list of ml-service API requests
        
        Args:
            message_body: Raw message bytes from RabbitMQ
            
        Returns:
            dict: Parsed sensor data with sourceId, payload, timestamp
            list: List of API request payloads for ml-service
        """
        try:
            # Parse JSON message
            data = json.loads(message_body)
            
            # Extract fields
            source_id = data.get("sourceId", "UNKNOWN")
            payload = data.get("payload", {})
            timestamp = data.get("timestamp", 0)
            
            # Transform each metric in payload to API request
            api_requests = []
            
            for key, value in payload.items():
                # Map payload key to metric_type
                metric_type = self.metric_mapping.get(key)
                
                if metric_type:
                    api_request = {
                        "source": "sensor",
                        "value": float(value),
                        "metric_type": metric_type
                    }
                    api_requests.append({
                        "metric_name": key,
                        "request": api_request
                    })
            
            sensor_data = {
                "sourceId": source_id,
                "payload": payload,
                "timestamp": timestamp
            }
            
            return sensor_data, api_requests
            
        except json.JSONDecodeError as e:
            raise ValueError(f"Invalid JSON message: {e}")
        except Exception as e:
            raise ValueError(f"Failed to transform message: {e}")
    
    def format_prediction_log(self, source_id, metric_name, value, prediction):
        """
        Format prediction result for logging
        
        Args:
            source_id: Sensor ID
            metric_name: Metric name (temperature, humidity, co2_level)
            value: Sensor value
            prediction: Prediction result from ml-service
            
        Returns:
            str: Formatted log message
        """
        label = prediction.get("label", "UNKNOWN")
        desc = prediction.get("desc", "No description")
        
        return (
            f"[{source_id}] {metric_name}={value:.1f} → "
            f"{label} ({desc})"
        )
