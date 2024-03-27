# Logic for Actuator Controls and Timing
# 2024 Team Baby Guardian

import RPi.GPIO as GPIO
import time
import os

# Setup GPIO pins
ENA = 25  # Enable pin for L298N channel
IN1 = 23  # Direction pin 1
IN2 = 24  # Direction pin 2
content = "0"

GPIO.setmode(GPIO.BCM)  # Use Broadcom pin numbering
GPIO.setup(ENA, GPIO.OUT)
GPIO.setup(IN1, GPIO.OUT)
GPIO.setup(IN2, GPIO.OUT)

# PWM setup
pwm = GPIO.PWM(ENA, 100)  # Set PWM frequency to 100 Hz
pwm.start(100)  # Start PWM with 100% duty cycle (full speed)

def extend_actuator():
    GPIO.output(IN1, GPIO.HIGH)  # Set IN1
    GPIO.output(IN2, GPIO.LOW)   # Clear IN2
    print("Extending actuator")

def retract_actuator():
    GPIO.output(IN1, GPIO.LOW)   # Clear IN1
    GPIO.output(IN2, GPIO.HIGH)  # Set IN2
    print("Retracting actuator")

def stop_actuator():
    GPIO.output(IN1, GPIO.LOW)   # Clear IN1
    GPIO.output(IN2, GPIO.LOW)   # Clear IN2
    print("Stopping actuator")

try:
    # Extend the actuator for 5 seconds
    # Retract the actuator for 5 seconds
    while True:
        if os.path.exists("/home/baby/Desktop/soothing_intensity.txt"):
            with open("/home/baby/Desktop/soothing_intensity.txt", "r") as file:
                content = file.read().strip()
        
        try:
            content_a = int(content)  # Assuming content is the cycle count
            if content_a > 0:
                for _ in range(content_a):
                    retract_actuator()
                    time.sleep(10)
                    
                    extend_actuator()
                    time.sleep(10)
        except ValueError:
            print("Please enter a valid integer for the duration.")
        except Exception as e:
            print("An error occurred:", e)
        
        # Stop the actuator
        # stop_actuator()
        time.sleep(5)
    
except KeyboardInterrupt:
    pwm.stop()  # Stop PWM
    GPIO.cleanup()  # Clean up GPIO

finally:
    pwm.stop()  # Ensure PWM is stopped on script exit
    GPIO.cleanup() # Clean up GPIO