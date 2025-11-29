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
