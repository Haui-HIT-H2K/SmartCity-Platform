# Run Python Data Simulator to generate test data

# NOTE: Make sure RabbitMQ services are running first
# docker-compose up -d edge-subnet-1 edge-subnet-2

cd python-data-simulator

# Install dependencies
pip install -r requirements.txt

# For quick testing, you can modify config.py to reduce TOTAL_REQUESTS
# For example: TOTAL_REQUESTS = 10_000 instead of 40_000_000

# Run simulator
python main.py

# Expected output:
# - Messages sent to city-data-queue-1 and city-data-queue-2
# - Progress logs every 100,000 messages
# - Completion summary with total messages and throughput
