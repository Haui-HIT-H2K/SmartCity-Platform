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
Training script for Smart City ML models
Trains IsolationForest models for anomaly detection on:
- Temperature
- Humidity  
- CO2 levels
"""

import numpy as np
import joblib
from pathlib import Path
from sklearn.ensemble import IsolationForest

# Create models directory if it doesn't exist
models_dir = Path(__file__).parent / "app" / "models"
models_dir.mkdir(parents=True, exist_ok=True)

print("=" * 60)
print("Smart City ML Model Training")
print("=" * 60)

# ============================================================
# 1. TEMPERATURE MODEL (°C)
# ============================================================
print("\n[1/3] Training Temperature Anomaly Detection Model...")

# Normal temperature range: 15-35°C (typical urban environment)
# Generate synthetic training data with normal patterns
np.random.seed(42)
normal_temp = np.random.uniform(15, 35, 5000).reshape(-1, 1)
# Add some seasonal variations
seasonal_temp = np.concatenate([
    np.random.normal(20, 3, 2000).reshape(-1, 1),  # Spring/Fall
    np.random.normal(28, 4, 2000).reshape(-1, 1),  # Summer
    np.random.normal(12, 3, 1000).reshape(-1, 1),  # Winter
])
training_temp = np.concatenate([normal_temp, seasonal_temp])

# Train IsolationForest
# contamination=0.1 means we expect ~10% of data to be anomalies
temp_model = IsolationForest(
    contamination=0.1,
    random_state=42,
    n_estimators=100
)
temp_model.fit(training_temp)

# Save model
temp_model_path = models_dir / "temperature_model.pkl"
joblib.dump(temp_model, temp_model_path)
print(f"✓ Temperature model saved: {temp_model_path}")

# Test the model
test_values = [20, 35, 45, -5]  # Normal, Normal, Anomaly, Anomaly
predictions = temp_model.predict([[v] for v in test_values])
print(f"  Test predictions: {test_values}")
print(f"  Results: {['Normal' if p == 1 else 'ANOMALY' for p in predictions]}")

# ============================================================
# 2. HUMIDITY MODEL (%)
# ============================================================
print("\n[2/3] Training Humidity Anomaly Detection Model...")

# Normal humidity range: 30-80% (typical urban environment)
np.random.seed(43)
normal_humidity = np.random.uniform(30, 80, 5000).reshape(-1, 1)
# Add variations
seasonal_humidity = np.concatenate([
    np.random.normal(60, 10, 3000).reshape(-1, 1),  # Moderate
    np.random.normal(45, 8, 2000).reshape(-1, 1),   # Dry season
])
training_humidity = np.concatenate([normal_humidity, seasonal_humidity])

# Train model
humidity_model = IsolationForest(
    contamination=0.1,
    random_state=43,
    n_estimators=100
)
humidity_model.fit(training_humidity)

# Save model
humidity_model_path = models_dir / "humidity_model.pkl"
joblib.dump(humidity_model, humidity_model_path)
print(f"✓ Humidity model saved: {humidity_model_path}")

# Test the model
test_values = [50, 70, 95, 10]  # Normal, Normal, Anomaly, Anomaly
predictions = humidity_model.predict([[v] for v in test_values])
print(f"  Test predictions: {test_values}")
print(f"  Results: {['Normal' if p == 1 else 'ANOMALY' for p in predictions]}")

# ============================================================
# 3. CO2 MODEL (ppm)
# ============================================================
print("\n[3/3] Training CO2 Anomaly Detection Model...")

# Normal CO2 range: 350-600 ppm (outdoor/urban)
# 600-1000 ppm can be normal indoors
np.random.seed(44)
normal_co2 = np.random.uniform(350, 600, 4000).reshape(-1, 1)
indoor_co2 = np.random.uniform(600, 900, 3000).reshape(-1, 1)
training_co2 = np.concatenate([normal_co2, indoor_co2])

# Train model
co2_model = IsolationForest(
    contamination=0.1,
    random_state=44,
    n_estimators=100
)
co2_model.fit(training_co2)

# Save model
co2_model_path = models_dir / "co2_model.pkl"
joblib.dump(co2_model, co2_model_path)
print(f"✓ CO2 model saved: {co2_model_path}")

# Test the model
test_values = [400, 700, 1500, 2000]  # Normal, Normal, Anomaly, Anomaly
predictions = co2_model.predict([[v] for v in test_values])
print(f"  Test predictions: {test_values}")
print(f"  Results: {['Normal' if p == 1 else 'ANOMALY' for p in predictions]}")

print("\n" + "=" * 60)
print("✓ All models trained and saved successfully!")
print("=" * 60)
print(f"\nModels saved in: {models_dir}")
print("Files created:")
print(f"  - temperature_model.pkl")
print(f"  - humidity_model.pkl")
print(f"  - co2_model.pkl")
print("\nNext steps:")
print("  1. Rebuild ML service: docker-compose build ml-service")
print("  2. Restart services: docker-compose up -d")
print("=" * 60)
