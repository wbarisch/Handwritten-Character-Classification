# Ubuntu Jammy Base Image
FROM mcr.microsoft.com/devcontainers/base:jammy as devcontainer

ARG PYTHON_VERSION=3.11
ARG JAVA_VERSION=openjdk-17-jdk

# Allow automated installation of packages
ENV DEBIAN_FRONTEND=noninteractive

# Android SDK versions
ENV ANDROID_SDK_TOOLS_VERSION 7583922
ENV ANDROID_SDK_HOME /usr/lib/android-sdk
ENV PATH $PATH:$ANDROID_SDK_HOME/cmdline-tools/latest/bin:$ANDROID_SDK_HOME/platform-tools

# Gradle 8.10.2
ENV GRADLE_VERSION 8.10.2

# Linux Tools
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    curl \
    git \
    unzip \
    wget

# Java (OpenJDK 17)
RUN apt-get install -y ${JAVA_VERSION} \
    && rm -rf /var/lib/apt/lists/*

# Install Android SDK command-line tools
RUN mkdir -p ${ANDROID_SDK_HOME}/cmdline-tools/latest && \
    wget -O cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS_VERSION}_latest.zip" && \
    unzip -q cmdline-tools.zip -d ${ANDROID_SDK_HOME}/cmdline-tools/latest && \
    rm cmdline-tools.zip && \
    mv ${ANDROID_SDK_HOME}/cmdline-tools/latest/cmdline-tools/* ${ANDROID_SDK_HOME}/cmdline-tools/latest/ && \
    rmdir ${ANDROID_SDK_HOME}/cmdline-tools/latest/cmdline-tools

# Adding Android SDK to $PATH 
ENV PATH="${PATH}:${ANDROID_SDK_HOME}/cmdline-tools/latest/bin:${ANDROID_SDK_HOME}/platform-tools"

# Android Platform Tools
RUN yes | sdkmanager --licenses && \
    sdkmanager "platforms;android-30" "build-tools;30.0.3" "ndk;21.4.7075529"

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

# Mamba Added to $PATH
ENV PATH="/opt/conda/bin:$PATH"

# Initialize Conda and create dl4cv environment for root
RUN conda init bash && \
    bash -c "source /opt/conda/etc/profile.d/conda.sh && mamba create -n dl4cv python=${PYTHON_VERSION} -y"

# Install Machine Learning Libraries in dl4cv environment, including onnx
RUN bash -c "source /opt/conda/etc/profile.d/conda.sh && conda activate dl4cv && \
    mamba install pytorch=2.4.0 torchvision=0.19.0 pytorch-cuda=12.4 onnx onnxruntime=1.15.1 -c pytorch -c nvidia -c conda-forge && \
    mamba install matplotlib seaborn pandas scikit-learn mlxtend nltk jupyterlab tqdm ipywidgets python-graphviz"

# Install SDKMAN and set up Gradle and Kotlin for root
RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source /root/.sdkman/bin/sdkman-init.sh && \
    sdk install gradle ${GRADLE_VERSION} && \
    sdk install kotlin && \
    echo 'source /root/.sdkman/bin/sdkman-init.sh' >> /root/.bashrc"

# Adding SDKMAN binaries to PATH (Gradle and Kotlin)
ENV PATH="/root/.sdkman/candidates/gradle/current/bin:/root/.sdkman/candidates/kotlin/current/bin:$PATH"

# Disable update commands (move to end)
RUN mv /usr/bin/apt-get /usr/bin/apt-get.original && \
    echo '#!/bin/sh' > /usr/bin/apt-get && \
    echo 'echo "apt-get is disabled in this container."' >> /usr/bin/apt-get && \
    chmod +x /usr/bin/apt-get && \
    mv /usr/bin/curl /usr/bin/curl.original && \
    echo '#!/bin/sh' > /usr/bin/curl && \
    echo 'echo "curl is disabled in this container."' >> /usr/bin/curl && \
    chmod +x /usr/bin/curl && \
    mv /usr/bin/wget /usr/bin/wget.original && \
    echo '#!/bin/sh' > /usr/bin/wget && \
    echo 'echo "wget is disabled in this container."' >> /usr/bin/wget && \
    chmod +x /usr/bin/wget

# Ensure Conda is initialized and dl4cv is activated when the container starts
RUN echo "source /opt/conda/etc/profile.d/conda.sh && conda activate dl4cv" >> ~/.bashrc
