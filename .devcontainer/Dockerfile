# Ubuntu Jammy Base Image
FROM mcr.microsoft.com/devcontainers/base:jammy as devcontainer

ARG PYTHON_VERSION=3.11
ARG JAVA_VERSION=openjdk-17-jdk

# Allow automated installation of packages
ENV DEBIAN_FRONTEND=noninteractive

# Linux Tools
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    curl \
    git \
    unzip \
    wget

# Java (OpenJDK 11)
RUN apt-get install -y ${JAVA_VERSION}

# Kotlin Compiler (Optional based on your needs)
RUN curl -s https://get.sdkman.io | bash && \
    bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install kotlin"


# Python (3.11)
RUN apt-get update && apt-get install -y \
    python${PYTHON_VERSION} \
    python${PYTHON_VERSION}.dev \
    python${PYTHON_VERSION}-distutils \
    python3-pip

# Install Miniforge (Mamba)
RUN curl -L -O https://github.com/conda-forge/miniforge/releases/latest/download/Miniforge3-Linux-x86_64.sh && \
bash Miniforge3-Linux-x86_64.sh -b -p /opt/conda && \
rm Miniforge3-Linux-x86_64.sh

# Add Mamba to the PATH
ENV PATH="/opt/conda/bin:$PATH"

# Initialize Mamba
RUN mamba init bash

# Create and activate the dl4cv environment
RUN mamba create -n dl4cv python=${PYTHON_VERSION} -y

# Activate the environment and install libraries
RUN /bin/bash -c "source activate dl4cv && \
    mamba install pytorch torchvision pytorch-cuda=11.8 -c pytorch -c nvidia && \
    mamba install matplotlib seaborn pandas scikit-learn mlxtend nltk jupyterlab tqdm ipywidgets python-graphviz"
