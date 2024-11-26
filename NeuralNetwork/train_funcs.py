import os
import random
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

import torch
from torchvision.datasets import Omniglot
from torchvision import transforms
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import Dataset, DataLoader, random_split, Subset
import torch.optim as optim
from tqdm.notebook import trange, tqdm
from PIL import Image
import random
from dataset import OmniglotDataset

def trainSiamese(model, train_dataloader, val_dataloader, optimizer, criterion):
    model.cuda()
    train_running_loss = 0.0
    val_running_loss = 0.0
    
    model.train()
    for img1,img2,lbl in train_dataloader:
        img1 = img1.cuda()
        img2 = img2.cuda()
        lbl = lbl.cuda()
        img1emb = model.calculateEmbedding(img1)
        img2emb = model.calculateEmbedding(img2)
        outputs = model.forward(img1emb,img2emb)
        loss = criterion(outputs,lbl)
        
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()
        
        train_running_loss += loss.item()
        
    train_loss = train_running_loss / len(train_dataloader)
    
    model.eval()
    with torch.no_grad():
        for img1,img2,lbl in val_dataloader:
            img1 = img1.cuda()
            img2 = img2.cuda()
            lbl = lbl.cuda()
            img1emb = model.calculateEmbedding(img1)
            img2emb = model.calculateEmbedding(img2)
            outputs = model.forward(img1emb,img2emb)
            loss = criterion(outputs,lbl)
            
            val_running_loss += loss.item()
        
    val_loss = val_running_loss / len(val_dataloader)
    
    return train_loss, val_loss
    
    
        
def testSiamese_kway_nshot(model, test_dataloader):
    correct_count = 0
    total_count = 0

    model.eval()
    with torch.no_grad():
        for query_image, support_images in tqdm(test_dataloader, desc="Testing", leave=False):
            query_image = query_image.cuda()
            query_image_emb = model.calculateEmbedding(query_image)
            scores = []
            for support_set in support_images:
                support_scores = []
                for support_image in support_set:
                    support_image = support_image.cuda()
                    support_image_emb = model.calculateEmbedding(support_image)
                    
                    score = model(query_image_emb, support_image_emb)
                    support_scores.append(score.item())
                
                avg_score = np.mean(support_scores)
                scores.append(avg_score)
            

            predicted_class = np.argmax(scores)
            if predicted_class == 0:
                correct_count += 1
            total_count += 1
    
    accuracy = correct_count / total_count
    print(f"Test Accuracy: {accuracy:.4f}")
    return accuracy