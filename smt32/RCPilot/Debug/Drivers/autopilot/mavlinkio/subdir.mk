################################################################################
# Automatically-generated file. Do not edit!
# Toolchain: GNU Tools for STM32 (13.3.rel1)
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../Drivers/autopilot/mavlinkio/MavlinkSender.cpp 

OBJS += \
./Drivers/autopilot/mavlinkio/MavlinkSender.o 

CPP_DEPS += \
./Drivers/autopilot/mavlinkio/MavlinkSender.d 


# Each subdirectory must supply rules for building sources it contributes
Drivers/autopilot/mavlinkio/%.o Drivers/autopilot/mavlinkio/%.su Drivers/autopilot/mavlinkio/%.cyclo: ../Drivers/autopilot/mavlinkio/%.cpp Drivers/autopilot/mavlinkio/subdir.mk
	arm-none-eabi-g++ "$<" -mcpu=cortex-m3 -std=gnu++14 -g3 -DDEBUG -DUSE_HAL_DRIVER -DSTM32F103xB -c -I../Core/Inc -I../Drivers/STM32F1xx_HAL_Driver/Inc/Legacy -I../Drivers/STM32F1xx_HAL_Driver/Inc -I../Drivers/CMSIS/Device/ST/STM32F1xx/Include -I../Drivers/CMSIS/Include -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/uart" -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/mavlink" -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/math" -I"/home/stalos/STM32CubeIDE/workspace_1.18.0/RCPilot/Drivers/autopilot/mavlinkio" -O0 -ffunction-sections -fdata-sections -fno-exceptions -fno-rtti -fno-use-cxa-atexit -Wall -fstack-usage -fcyclomatic-complexity -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" --specs=nano.specs -mfloat-abi=soft -mthumb -o "$@"

clean: clean-Drivers-2f-autopilot-2f-mavlinkio

clean-Drivers-2f-autopilot-2f-mavlinkio:
	-$(RM) ./Drivers/autopilot/mavlinkio/MavlinkSender.cyclo ./Drivers/autopilot/mavlinkio/MavlinkSender.d ./Drivers/autopilot/mavlinkio/MavlinkSender.o ./Drivers/autopilot/mavlinkio/MavlinkSender.su

.PHONY: clean-Drivers-2f-autopilot-2f-mavlinkio

