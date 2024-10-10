# Ubuntu Jammy Base Image
FROM mcr.microsoft.com/devcontainers/base:jammy as devcontainer

ARG PYTHON_VERSION=3.11
ARG JAVA_VERSION=openjdk-11-jdk

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
    python${PYTHON_VERSION}.pip

# Install Miniforge (Mamba)
RUN curl -L -O https://github.com/conda-forge/miniforge/releases/latest/download/Miniforge3-Linux-x86_64.sh && \
bash Miniforge3-Linux-x86_64.sh -b -p /opt/conda && \
rm Miniforge3-Linux-x86_64.sh

# Add Mamba to the PATH
ENV PATH="/opt/conda/bin:$PATH"

# Create the dl4cv environment using Mamba and activate it
RUN mamba init bash \
    && mamba create -n dl4cv python=${PYTHON_VERSION} -y \
    && echo "source activate dl4cv" >> ~/.bashrc

# Install Python libraries for machine learning
RUN mamba install -n dl4cv -y \
    pytorch torchvision pytorch-cuda=11.8 -c pytorch -c nvidia \
    matplotlib seaborn pandas scikit-learn mlxtend nltk jupyterlab tqdm ipywidgets python-graphviz
