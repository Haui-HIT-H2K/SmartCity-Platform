import numpy as np
import joblib
from sklearn.ensemble import IsolationForest

# 1. Generate synthetic data
# "Normal" temperature data: Mean=28, Std=3
# We generate 1000 data points
rng = np.random.RandomState(42)
X_train = 3 * rng.randn(1000, 1) + 28

# 2. Train IsolationForest model
# contamination=0.05 assumes 5% of the data might be anomalies (noise in training or expected rate)
clf = IsolationForest(contamination=0.05, random_state=42)
clf.fit(X_train)

# 3. Save the trained model
model_filename = "sensor_anomaly_model.pkl"
joblib.dump(clf, model_filename)
print(f"Model saved to {model_filename}")

# 4. Verify with test predictions
# IsolationForest returns 1 for Inliers (Normal), -1 for Outliers (Anomaly)
test_data = np.array([[28], [80]]) # 28 is normal, 80 is definitely an anomaly
predictions = clf.predict(test_data)

print(f"Test Prediction for 28 (Expected 1/Normal): {predictions[0]}")
print(f"Test Prediction for 80 (Expected -1/Anomaly): {predictions[1]}")
