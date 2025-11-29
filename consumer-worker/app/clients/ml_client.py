"""
ML Service HTTP Client
Handles communication with ml-service API
"""

import requests
import time
import logging
from app import config

logger = logging.getLogger(__name__)


class MLServiceClient:
    """HTTP client for ml-service predictions"""
    
    def __init__(self):
        self.predict_url = config.ML_SERVICE_PREDICT_ENDPOINT
        self.health_url = config.ML_SERVICE_HEALTH_ENDPOINT
        self.timeout = config.HTTP_TIMEOUT
        self.max_retries = config.MAX_RETRIES
        self.retry_delay = config.RETRY_DELAY
        
        # Create session for connection pooling
        self.session = requests.Session()
    
    def health_check(self):
        """
        Check if ml-service is available
        
        Returns:
            bool: True if service is healthy, False otherwise
        """
        try:
            response = self.session.get(self.health_url, timeout=self.timeout)
            if response.status_code == 200:
                data = response.json()
                total_models = data.get("total_models", 0)
                logger.info(f"✓ ML Service healthy - {total_models} models loaded")
                return True
            else:
                logger.warning(f"✗ ML Service unhealthy - Status {response.status_code}")
                return False
        except Exception as e:
            logger.error(f"✗ ML Service health check failed: {e}")
            return False
    
    def predict(self, request_data):
        """
        Get prediction from ml-service with retry logic
        
        Args:
            request_data: Dict with 'source', 'value', 'metric_type'
            
        Returns:
            dict: Prediction result
            
        Raises:
            Exception: If all retries fail
        """
        for attempt in range(self.max_retries):
            try:
                response = self.session.post(
                    self.predict_url,
                    json=request_data,
                    timeout=self.timeout
                )
                
                if response.status_code == 200:
                    return response.json()
                else:
                    logger.warning(
                        f"Prediction failed (attempt {attempt + 1}/{self.max_retries}): "
                        f"Status {response.status_code} - {response.text}"
                    )
                    
            except requests.exceptions.Timeout:
                logger.warning(
                    f"Prediction timeout (attempt {attempt + 1}/{self.max_retries})"
                )
            except requests.exceptions.ConnectionError as e:
                logger.warning(
                    f"Connection error (attempt {attempt + 1}/{self.max_retries}): {e}"
                )
            except Exception as e:
                logger.error(f"Unexpected error during prediction: {e}")
            
            # Wait before retry (except on last attempt)
            if attempt < self.max_retries - 1:
                time.sleep(self.retry_delay)
        
        # All retries failed
        raise Exception(
            f"Failed to get prediction after {self.max_retries} attempts"
        )
    
    def batch_predict(self, request_list):
        """
        Process multiple predictions (currently sequential)
        
        Args:
            request_list: List of request dicts
            
        Returns:
            list: List of (success, result) tuples
        """
        results = []
        
        for item in request_list:
            try:
                prediction = self.predict(item["request"])
                results.append({
                    "metric_name": item["metric_name"],
                    "success": True,
                    "prediction": prediction
                })
            except Exception as e:
                logger.error(
                    f"Failed to predict {item['metric_name']}: {e}"
                )
                results.append({
                    "metric_name": item["metric_name"],
                    "success": False,
                    "error": str(e)
                })
        
        return results
    
    def close(self):
        """Close HTTP session"""
        self.session.close()
