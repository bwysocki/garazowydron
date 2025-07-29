################################################################################
# Automatically-generated file. Do not edit!
# Toolchain: GNU Tools for STM32 (13.3.rel1)
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../Drivers/autopilot/uart/HalUartDriver.cpp \
../Drivers/autopilot/uart/UartRxQueue.cpp \
../Drivers/autopilot/uart/UartTxQueue.cpp 

OBJS += \
./Drivers/autopilot/uart/HalUartDriver.o \
./Drivers/autopilot/uart/UartRxQueue.o \
./Drivers/autopilot/uart/UartTxQueue.o 

CPP_DEPS += \
./Drivers/autopilot/uart/HalUartDriver.d \
./Drivers/autopilot/uart/UartRxQueue.d \
./Drivers/autopilot/uart/UartTxQueue.d 


# Each subdirectory must supply rules for building sources it contributes
Drivers/autopilot/uart/%.o Drivers/autopilot/uart/%.su Drivers/autopilot/uart/%.cyclo: ../Drivers/autopilot/uart/%.cpp Drivers/autopilot/uart/subdir.mk
	arm-none-eabi-g++ "$<" -mcpu=cortex-m3 -std=gnu++14 -g3 -DDEBUG -DUSE_HAL_DRIVER -DSTM32F103xB -c -I../Core/Inc -I../Drivers/STM32F1xx_HAL_Driver/Inc/Legacy -I../Drivers/STM32F1xx_HAL_Driver/Inc -I../Drivers/CMSIS/Device/ST/STM32F1xx/Include -I../Drivers/CMSIS/Include -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/uart" -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/mavlink" -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/math" -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/mavlinkio" -O0 -ffunction-sections -fdata-sections -fno-exceptions -fno-rtti -fno-use-cxa-atexit -Wall -fstack-usage -fcyclomatic-complexity -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" --specs=nano.specs -mfloat-abi=soft -mthumb -o "$@"

clean: clean-Drivers-2f-autopilot-2f-uart

clean-Drivers-2f-autopilot-2f-uart:
	-$(RM) ./Drivers/autopilot/uart/HalUartDriver.cyclo ./Drivers/autopilot/uart/HalUartDriver.d ./Drivers/autopilot/uart/HalUartDriver.o ./Drivers/autopilot/uart/HalUartDriver.su ./Drivers/autopilot/uart/UartRxQueue.cyclo ./Drivers/autopilot/uart/UartRxQueue.d ./Drivers/autopilot/uart/UartRxQueue.o ./Drivers/autopilot/uart/UartRxQueue.su ./Drivers/autopilot/uart/UartTxQueue.cyclo ./Drivers/autopilot/uart/UartTxQueue.d ./Drivers/autopilot/uart/UartTxQueue.o ./Drivers/autopilot/uart/UartTxQueue.su

.PHONY: clean-Drivers-2f-autopilot-2f-uart

