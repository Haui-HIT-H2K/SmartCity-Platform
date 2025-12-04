# Copyright 2025 Haui.HIT - H2K

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

#!/bin/bash
set -e

# Check if models exist, if not, train them
MODELS_DIR="/app/app/models"
TEMP_MODEL="$MODELS_DIR/temperature_model.pkl"
HUMIDITY_MODEL="$MODELS_DIR/humidity_model.pkl"
CO2_MODEL="$MODELS_DIR/co2_model.pkl"

if [ ! -f "$TEMP_MODEL" ] || [ ! -f "$HUMIDITY_MODEL" ] || [ ! -f "$CO2_MODEL" ]; then
    echo "========================================"
    echo "Models not found. Training models..."
    echo "========================================"
    python3 /app/train_models.py
    echo "========================================"
    echo "Models trained successfully!"
    echo "========================================"
else
    echo "Models found. Skipping training."
fi

# Start the application
exec uvicorn app.main:app --host 0.0.0.0 --port 8000

