"""
Multi-Model Training Script for Smart City Sensor Anomaly Detection

This script trains 3 separate IsolationForest models for:
- Temperature (15-45°C)
- Humidity (30-95%)
- CO2 Level (350-1000 ppm)

Each model is trained on synthetic data matching the ranges from python-data-simulator
"""

import numpy as np
import joblib
from sklearn.ensemble import IsolationForest

# Set random seed for reproducibility
np.random.seed(42)

def train_temperature_model():
    """
    Train anomaly detection model for temperature sensor
    
    Simulator range: 15.0 - 45.0°C
    Normal distribution: mean=28°C, std=5°C
    """
    print("=" * 60)
    print("Training Temperature Anomaly Detection Model")
    print("=" * 60)
    
    # Generate synthetic training data
    # Mean at 28°C (middle of 15-45 range), std=5 to cover normal variations
    X_train = np.random.normal(loc=28.0, scale=5.0, size=1000).reshape(-1, 1)
    
    # Train IsolationForest
    # contamination=0.05 assumes 5% of data might be anomalies
    model = IsolationForest(
        contamination=0.05,
        random_state=42,
        n_estimators=100
    )
    model.fit(X_train)
    
    # Save model
    filename = "../models/temperature_model.pkl"
    joblib.dump(model, filename)
    print(f"✓ Model saved to {filename}")
    
    # Test predictions
    test_values = [
        (28.0, "Normal temperature"),
        (15.0, "Lower bound (normal)"),
        (45.0, "Upper bound (normal)"),
        (10.0, "Below normal (anomaly)"),
        (80.0, "Above normal (anomaly)")
    ]
    
    print("\nTest Predictions:")
    print("-" * 60)
    for value, description in test_values:
        prediction = model.predict([[value]])
        result = "NORMAL" if prediction[0] == 1 else "ANOMALY"
        print(f"  {value:>6.1f}°C - {result:8s} - {description}")
    
    return model


def train_humidity_model():
    """
    Train anomaly detection model for humidity sensor
    
    Simulator range: 30 - 95%
    Normal distribution: mean=62.5%, std=12%
    """
    print("\n" + "=" * 60)
    print("Training Humidity Anomaly Detection Model")
    print("=" * 60)
    
    # Generate synthetic training data
    # Mean at 62.5% (middle of 30-95 range), std=12 to cover normal variations
    X_train = np.random.normal(loc=62.5, scale=12.0, size=1000).reshape(-1, 1)
    
    # Train IsolationForest
    model = IsolationForest(
        contamination=0.05,
        random_state=42,
        n_estimators=100
    )
    model.fit(X_train)
    
    # Save model
    filename = "../models/humidity_model.pkl"
    joblib.dump(model, filename)
    print(f"✓ Model saved to {filename}")
    
    # Test predictions
    test_values = [
        (65.0, "Normal humidity"),
        (30.0, "Lower bound (normal)"),
        (95.0, "Upper bound (normal)"),
        (15.0, "Below normal (anomaly)"),
        (105.0, "Above normal (anomaly)")
    ]
    
    print("\nTest Predictions:")
    print("-" * 60)
    for value, description in test_values:
        prediction = model.predict([[value]])
        result = "NORMAL" if prediction[0] == 1 else "ANOMALY"
        print(f"  {value:>6.1f}%  - {result:8s} - {description}")
    
    return model


def train_co2_model():
    """
    Train anomaly detection model for CO2 sensor
    
    Simulator range: 350 - 1000 ppm
    Normal distribution: mean=675 ppm, std=120 ppm
    """
    print("\n" + "=" * 60)
    print("Training CO2 Anomaly Detection Model")
    print("=" * 60)
    
    # Generate synthetic training data
    # Mean at 675 ppm (middle of 350-1000 range), std=120 to cover normal variations
    X_train = np.random.normal(loc=675.0, scale=120.0, size=1000).reshape(-1, 1)
    
    # Train IsolationForest
    model = IsolationForest(
        contamination=0.05,
        random_state=42,
        n_estimators=100
    )
    model.fit(X_train)
    
    # Save model
    filename = "../models/co2_model.pkl"
    joblib.dump(model, filename)
    print(f"✓ Model saved to {filename}")
    
    # Test predictions
    test_values = [
        (420.0, "Normal CO2"),
        (350.0, "Lower bound (normal)"),
        (1000.0, "Upper bound (normal)"),
        (250.0, "Below normal (anomaly)"),
        (2000.0, "Above normal (anomaly)")
    ]
    
    print("\nTest Predictions:")
    print("-" * 60)
    for value, description in test_values:
        prediction = model.predict([[value]])
        result = "NORMAL" if prediction[0] == 1 else "ANOMALY"
        print(f"  {value:>7.0f} ppm - {result:8s} - {description}")
    
    return model


def main():
    """Main training pipeline"""
    print("\n" + "=" * 60)
    print("SMART CITY SENSOR ANOMALY DETECTION - MODEL TRAINING")
    print("=" * 60)
    print("\nTraining 3 models for multi-metric anomaly detection...")
    print("Data source: python-data-simulator ranges\n")
    
    # Train all models
    temp_model = train_temperature_model()
    humidity_model = train_humidity_model()
    co2_model = train_co2_model()
    
    # Final summary
    print("\n" + "=" * 60)
    print("TRAINING COMPLETED!")
    print("=" * 60)
    print("\nGenerated model files:")
    print("  ✓ models/temperature_model.pkl")
    print("  ✓ models/humidity_model.pkl")
    print("  ✓ models/co2_model.pkl")
    
    print("\nModel Statistics:")
    print(f"  - Training samples per model: 1,000")
    print(f"  - Algorithm: IsolationForest")
    print(f"  - Contamination rate: 5%")
    print(f"  - Number of estimators: 100")
    
    print("\nNext steps:")
    print("  1. Run: uvicorn app:app --reload")
    print("  2. Test API with POST /predict")
    print("  3. Monitor predictions in production")
    print("=" * 60)


if __name__ == "__main__":
    main()
