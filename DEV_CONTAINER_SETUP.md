# Development Container Setup

## Introduction
This document contains the steps required to set up the development container for this project.

## Prerequisites
- Docker installed on your machine
- Dev Containers Extension (if using VS Code)
- Docker Extension (if using VS Code)

## Setting up Dev Environment in Windows with VS Code
1. Install these extensions in VS Code: "Dev Containers" and "Docker".
2. After installing Docker extension, a pop-up to install "WSL" will be on the screen. Click yes on this.
4. Install "Docker" from "https://docs.docker.com/desktop/install/windows-install/"
5. In installer make sure to select this option: "Use WSL 2 instead of Hyper-V (recommended)
6. After installer has finished, restart your machine
7. When machine is restarted, "Docker" app will open. You don't need to create an account. You can press on skip for all instances
8. Checkout branch HCC-70 and make sure to have the folder .devcontainer in your directory
9. Select Ctrl+Shift+P and type in VS Code: Dev Containers: Rebuild Without Cache and Reopen in Container
10. A new VS Code environment will open up and by clicking on box pop-up in bottom corner you can see in the terminal the progress bar of installing the Dev Container
11. Once Dev Container is finished installing, you can open a new bash instance by clicking on the "+" in the bottom right of VS Code
12. To exit this Dev Container, click on the bottom left corner blue button that says "Dev Container" and select "Close Remote Connection". This will
return your local directory in VS Code
13. After the install, you will have delete your build cache as it is taking up space. Type into terminal environment: "docker system df".
14. Then to delete the build cache, type "docker builder prune" and then type "y"
15. Now to reconnect to Dev Container, select Ctrl+Shift+P and select "Dev Containers: Reopen in Container"

Note: The image when loaded in docker after unpacking it is 35GB in size.
Note: To run docker commands, the Docker application must be running in the background on Windows machines




