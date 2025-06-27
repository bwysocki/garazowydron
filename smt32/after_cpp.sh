#!/bin/bash

SCRIPT_DIR="/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Core/Src"
cd "$SCRIPT_DIR" || exit 1
cp main.c main.cpp
rm main.c