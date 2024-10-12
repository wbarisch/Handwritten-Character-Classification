# Development Container Setup

## Introduction
This document contains the steps required to set up the development container for this project.

## Prerequisites
- Docker installed on your machine
- Dev Containers Extension (if using VS Code)
- Docker Extension (if using VS Code)

## Features
- Nvidia-cuda
- Powershell
- Python
- SSH
- Conda and Miniforge
- Kotlin
TODO: Add Onnx to dev container. Also get list of versions from container

## Setting up Dev Environment in Windows with VS Code
1. Install these extensions in VS Code: "Dev Containers" and "Docker".
2. After installing Docker extension, a pop-up to install "WSL" will be on the screen. Click yes on this.
4. Install "Docker" from "https://docs.docker.com/desktop/install/windows-install/"
5. In installer make sure to select this option: "Use WSL 2 instead of Hyper-V (recommended)
6. After installer has finished, restart your machine
7. When machine is restarted, "Docker" app will open. You don't need to create an account. You can press on skip for all instances
8. In Terminal in VS Code, type this command: "docker load -i /path/to/your/hcrimage.tar"
9. It will take 5-20 minutes to load the docker image, depending on your machine. When it is finished loading, it will say in the command line: "Loaded image: vsc-handwritten-character-classification-9078cfcc6f41b0a5ca27effc0aa0b2137f67b88ae48b881826a33a93234150fc-uid:latest"
10. To verify that the docker image has loaded, input this command in terminal: "docker images". It show should the docker image, its "TAG", "IMAGE ID" (make note of your image ID for step 11) and so forth
11. To run the docker image, use this command: "docker run -it <image-name> /bin/bash". For me the <image-name> is: "e0b1594d3c78"
12. Now you are inside the dev container
13. To leave the container from the linux terminal, type "exit"
14. <Optional> The container is now shut down, however "WSL" will keep running in the background and use a 2GB of memory. To shutdown "WSL" as well, type "wsl --shutdown"

Note: The image when loaded in docker after unpacking it is 35GB in size.

## Setting up Dev Environment in Windows without VS Code

1. TODO. There is a way to setup the docker image in the "Docker" application in Windows, but haven't got around to trying it yet

## Creation Steps of Initial Dev Container
1. In VSCode press Ctrl+Shift+P and type in "Add Dev Container" and press enter
2. Click on "Add to Workspace"
3. Select "Ubuntu" devcontainer and then select "jammy"
4. Select needed features that are required in this environment
5. Create a "Dockerfile" within the newly created .devcontainer folder
6. First line is where the image for this container will be download from
7. TODO: Finish steps for creation of Dev Container




