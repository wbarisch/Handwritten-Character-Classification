import torch
from datetime import datetime

def save_checkpoint(epoch, model, optimizer, train_loss_history, val_loss_history):
    
    save_path = str(epoch) + "_checkpoint_" + datetime.now().strftime('%d_%m') + ".pt"
    state_dict = {'model_state_dict': model.state_dict(),
                  'optimizer_state_dict': optimizer.state_dict(),
                  'train_loss_history': train_loss_history,
                  'val_loss_history':val_loss_history}

    torch.save(state_dict, save_path)

    print(f'Model saved to ==> {save_path}')

def load_checkpoint(save_path,model, optimizer):
    state_dict = torch.load(save_path)
    model.load_state_dict(state_dict['model_state_dict'])
    optimizer.load_state_dict(state_dict['optimizer_state_dict'])
    train_loss_history =  state_dict['train_loss_history']
    val_loss_history =  state_dict['val_loss_history']
    print(f'Model loaded from <== {save_path}')
    
    return train_loss_history, val_loss_history