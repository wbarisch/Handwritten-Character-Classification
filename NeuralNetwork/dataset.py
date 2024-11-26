import os
import random
import numpy as np
import torch

from torch.utils.data import Dataset
from PIL import Image


class OmniglotDataset(Dataset):
    def __init__(self, dataset_directory, num_of_sets, transform=None):
        self.dataset_directory = dataset_directory
        self.categories = [[folder, os.listdir(os.path.join(dataset_directory, folder))] for folder in os.listdir(dataset_directory)  if not folder.startswith('.') ]
        self.num_of_sets = num_of_sets
        self.transform = transform
        
    def __len__(self):
        return self.num_of_sets

    def __getitem__(self, index):
        
        img1 = None
        img2 = None
        lbl = None
        if index % 2 == 0: #Same letter

            alphabet = random.choice(self.categories)
            charecter = random.choice(alphabet[1])
            alphabet = alphabet[0]
            
            imgs_path = os.path.join(self.dataset_directory,alphabet,charecter)
            img1name = random.choice(os.listdir(imgs_path))
            img2name = random.choice(os.listdir(imgs_path))
            img1 = Image.open(os.path.join(imgs_path , img1name))
            img2 = Image.open(os.path.join(imgs_path , img2name))
            lbl = 1.0
        else: #different letters
            alphabet1 = random.choice(self.categories)
            charecter1 = random.choice(alphabet1[1])
            alphabet1 = alphabet1[0]
            img1_path = os.path.join(self.dataset_directory,alphabet1,charecter1)
            
            img1name = random.choice(os.listdir(img1_path))
            
            alphabet2 = random.choice(self.categories)
            charecter2 = random.choice(alphabet2[1])
            alphabet2 = alphabet2[0]
            img2_path = os.path.join(self.dataset_directory,alphabet2,charecter2)
            img2name = random.choice(os.listdir(img2_path))

            img1 = Image.open(os.path.join(img1_path , img1name))
            img2 = Image.open(os.path.join(img2_path , img2name))
            lbl = 0.0

        if self.transform:
            img1 = self.transform(img1)
            img2 = self.transform(img2)
        
        return img1, img2, torch.from_numpy(np.array(lbl, dtype=np.float32)).view(1)
    
    
class kWay_nShotDataset(Dataset):
    def __init__(self, dataset_directory, num_of_sets, kway, nshot, transform=None):
        self.dataset_directory = dataset_directory
        # Flatten all characters across all alphabets for global sampling
        self.all_characters = [
            (alphabet, character)
            for alphabet in os.listdir(dataset_directory) if not alphabet.startswith('.')
            for character in os.listdir(os.path.join(dataset_directory, alphabet))
        ]
        self.num_of_sets = num_of_sets
        self.transform = transform
        self.kway = kway  
        self.nshot = nshot  

    def __len__(self):
        return self.num_of_sets

    def __getitem__(self, index):

        query_alphabet, query_character = random.choice(self.all_characters)
        query_path = os.path.join(self.dataset_directory, query_alphabet, query_character)
        query_image_name = random.choice(os.listdir(query_path))
        query_image = Image.open(os.path.join(query_path, query_image_name))
        
        if self.transform:
            query_image = self.transform(query_image)

        support_images = []

        support_characters = [(query_alphabet, query_character)] + [
            char for char in random.sample(
                [c for c in self.all_characters if c != (query_alphabet, query_character)],
                self.kway - 1
            )
        ]

        for (alphabet, character) in support_characters:
            character_path = os.path.join(self.dataset_directory, alphabet, character)
            image_files = os.listdir(character_path)
            
            class_images = []
            for _ in range(self.nshot):
                image_name = random.choice(image_files)
                image = Image.open(os.path.join(character_path, image_name))
                
                if self.transform:
                    image = self.transform(image)
                    
                class_images.append(image)
            
            support_images.append(class_images)  
        return query_image, support_images


