# calibration.py
import os
import numpy as np
from PIL import Image
from torchvision import transforms
from onnxruntime.quantization import CalibrationDataReader

class EmbeddingCalibrationDataReader(CalibrationDataReader):
    def __init__(self, calibration_image_folder, num_images=100):
        self.image_folder = calibration_image_folder
        self.num_images = num_images
        self.transform = transforms.Compose([
            transforms.Grayscale(num_output_channels=1),
            transforms.Resize((105, 105)),
            transforms.RandomAffine(degrees=30, translate=(0.1, 0.1), scale=(0.9, 1.1), fill=255),
            transforms.ToTensor()
        ])
        self.image_paths = self._get_image_paths()
        self.data_iter = iter(self.image_paths)

    def _get_image_paths(self):
        image_files = [
            os.path.join(self.image_folder, f)
            for f in os.listdir(self.image_folder)
            if os.path.isfile(os.path.join(self.image_folder, f)) and f.lower().endswith(('.png', '.jpg', '.jpeg'))
        ]
        np.random.shuffle(image_files)
        if len(image_files) < self.num_images:
            raise ValueError(f"Not enough images for calibration. Required: {self.num_images}, Found: {len(image_files)}")
        return image_files[:self.num_images]

    def get_next(self):
        try:
            image_path = next(self.data_iter)
        except StopIteration:
            return None
        image = Image.open(image_path).convert('L')
        image_tensor = self.transform(image).unsqueeze(0).numpy()

        return {'input_image': image_tensor}
