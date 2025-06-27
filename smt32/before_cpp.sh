#!/bin/bash

SCRIPT_DIR="/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Core/Src"
cd "$SCRIPT_DIR" || exit 1
cp main.cpp main.c
rm main.cpp
