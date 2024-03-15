# Raspberry Pi Serial Communication to Arduino

import serial

# Define the serial port and baud rate
ser = serial.Serial('/dev/serial0', 115200, timeout=1)

# Open the serial connection
# ser.open()

# Send a message to Arduino
message_to_arduino = "Hello Arduino!"
ser.write(message_to_arduino.encode())

# Close the serial connection
ser.close()