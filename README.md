# Handwritten Character Classification

The primary goal of this project is to enhance the driving experience by developing a compliant and user-friendly alternative input system. This system leverages machine learning and neural network technologies to facilitate safe and efficient text input while driving. You can read the attached documentation for design decisions and architecture.

## Project setup

### Android dev

In order to get this repo up and running, you need to have `Gradle` installed on your system. Simply import the gradle project into Android Studio and it should download all necessary files and libraries.

### Pytorch ML dev

#### Using VSCODE
Setup the devcontainer by opening this repo in VSCode. It should recognize the `.devcontainer` folder automatically and start building it. Further instructions on how to setup the devcontainer can be found under the [Devcontainer documentation](.devcontainer/DEV_CONTAINER_SETUP)

#### Other environments
For other environments, the following are required:

- **Python**: Version 3.11.10
- **Mamba**: Version 1.5.9  
- **PyTorch**: Version 2.4.0  
- **TorchVision**: Version 0.19.0
- **Onnx Runtime**: 1.17.0
- **CUDA**: Version 12.4  
- **Matplotlib**: Version 3.9.2
- **Seaborn**: Version 0.13.2
- **Pandas**: Version 2.2.3
- **Scikit-learn**: Version 1.5.2
- **MLxtend**: Version 0.23.1
- **NLTK**: Version 3.9.1
- **JupyterLab**: Version 4.2.5
- **Tqdm**: Version 4.66.5
- **Ipywidgets**: Version 8.1.5
- **Python-Graphviz**: Version 0.20.3

The required dev files for the Pytorch environment are in the `Neural-network-base-branch` branch.

## Changing the UI

Since the main scope of our project is to have a backbone ready for further refinement, this repo is easily expandable and loosely coupled. As a design pattern, we used MVVM for Android development.

For more details on how the different components are coupled, check out Section 5.2.1 of our documentation that properly explains it.