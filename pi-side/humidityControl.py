import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)  # Use either BCM or BOARD numbering  
power_pin = 19  # Example - replace with your desired 5V GPIO pin number

GPIO.setup(power_pin, GPIO.OUT)

# To turn on:
GPIO.output(power_pin, GPIO.HIGH)  

 # To turn off:
# GPIO.output(power_pin, GPIO.LOW)  

# GPIO.cleanup() # Reset GPIO settings when done