# static_quantization.py
from onnxruntime.quantization import quantize_static, QuantType, QuantFormat
from calibration import EmbeddingCalibrationDataReader
import warnings

warnings.filterwarnings("ignore")
model_fp32 = 'siamese_embedding_model_500.onnx'
model_quant = 'siamese_embedding_model_500_quantized.onnx'
calibration_image_folder = 'calibration_data/'

calibration_data_reader = EmbeddingCalibrationDataReader(calibration_image_folder, num_images=100)
try:
    quantize_static(
        model_input=model_fp32,
        model_output=model_quant,
        calibration_data_reader=calibration_data_reader,
        quant_format=QuantFormat.QOperator,
        per_channel=True,
        activation_type=QuantType.QUInt8,
        weight_type=QuantType.QInt8
    )
    print(f"Static quantization complete. Quantized model saved to {model_quant}")
except Exception as e:
    print(f"Static quantization failed: {e}")
