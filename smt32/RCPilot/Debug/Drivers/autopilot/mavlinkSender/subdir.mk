################################################################################
# Automatically-generated file. Do not edit!
# Toolchain: GNU Tools for STM32 (13.3.rel1)
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../Drivers/autopilot/mavlinkSender/MavlinkSender.cpp 

OBJS += \
./Drivers/autopilot/mavlinkSender/MavlinkSender.o 

CPP_DEPS += \
./Drivers/autopilot/mavlinkSender/MavlinkSender.d 


# Each subdirectory must supply rules for building sources it contributes
Drivers/autopilot/mavlinkSender/%.o Drivers/autopilot/mavlinkSender/%.su Drivers/autopilot/mavlinkSender/%.cyclo: ../Drivers/autopilot/mavlinkSender/%.cpp Drivers/autopilot/mavlinkSender/subdir.mk
	arm-none-eabi-g++ "$<" -mcpu=cortex-m3 -std=gnu++14 -g3 -DDEBUG -DUSE_HAL_DRIVER -DSTM32F103xB -c -I../Core/Inc -I../Drivers/STM32F1xx_HAL_Driver/Inc/Legacy -I../Drivers/STM32F1xx_HAL_Driver/Inc -I../Drivers/CMSIS/Device/ST/STM32F1xx/Include -I../Drivers/CMSIS/Include -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/uart" -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/mavlink" -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/mavlinkSender" -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/math" -O0 -ffunction-sections -fdata-sections -fno-exceptions -fno-rtti -fno-use-cxa-atexit -Wall -fstack-usage -fcyclomatic-complexity -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" --specs=nano.specs -mfloat-abi=soft -mthumb -o "$@"

clean: clean-Drivers-2f-autopilot-2f-mavlinkSender

clean-Drivers-2f-autopilot-2f-mavlinkSender:
	-$(RM) ./Drivers/autopilot/mavlinkSender/MavlinkSender.cyclo ./Drivers/autopilot/mavlinkSender/MavlinkSender.d ./Drivers/autopilot/mavlinkSender/MavlinkSender.o ./Drivers/autopilot/mavlinkSender/MavlinkSender.su

.PHONY: clean-Drivers-2f-autopilot-2f-mavlinkSender

