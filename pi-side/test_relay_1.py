import RPi.GPIO as GPIO
import time

# Setup GPIO pins
ENA = 25  # Enable pin for L298N channel
IN1 = 23  # Direction pin 1
IN2 = 24  # Direction pin 2

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
    extend_actuator()
    time.sleep(10)
    
    # Retract the actuator for 5 seconds
    retract_actuator()
    time.sleep(10)
    
    # Stop the actuator
    stop_actuator()
    
except KeyboardInterrupt:
    pwm.stop()  # Stop PWM
    GPIO.cleanup()  # Clean up GPIO

finally:
    pwm.stop()  # Ensure PWM is stopped on script exit
    GPIO.cleanup() # Clean up GPIO