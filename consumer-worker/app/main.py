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
Consumer Worker - Main Entry Point
Connects python-data-simulator (RabbitMQ) to ml-service (HTTP API)
"""

import logging
import sys
from app import config
from app.core.consumer import SensorConsumer


def setup_logging():
    """Configure logging"""
    logging.basicConfig(
        level=getattr(logging, config.LOG_LEVEL),
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )


def main():
    """Main entry point"""
    # Setup logging
    setup_logging()
    logger = logging.getLogger(__name__)
    
    # Print configuration
    config.print_config()
    
    # Get RabbitMQ configuration
    rabbitmq_config = config.get_rabbitmq_config()
    
    # Create consumer
    consumer = SensorConsumer(
        host=rabbitmq_config["host"],
        port=rabbitmq_config["port"],
        queue_name=rabbitmq_config["queue"]
    )
    
    # Start consuming
    try:
        success = consumer.start()
        if not success:
            logger.error("Failed to start consumer")
            sys.exit(1)
    except Exception as e:
        logger.error(f"Fatal error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
