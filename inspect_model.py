import tensorflow as tf
import os

model_path = os.path.join(os.getcwd(), 'fastapi_backend', 'model.h5')
print(f"Checking model at: {model_path}")

try:
    if not os.path.exists(model_path):
        print(f"File not found: {model_path}")
    else:
        model = tf.keras.models.load_model(model_path)
        print("Model loaded successfully")
        print("Input shape:", model.input_shape)
        print("Output shape:", model.output_shape)
        model.summary()
except Exception as e:
    print(f"Error loading model: {e}")
