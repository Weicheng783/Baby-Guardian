import os
import serial
import time
import requests
import RPi.GPIO as GPIO

ser = serial.Serial('/dev/ttyACM0', 9600)  # Replace '/dev/ttyACM0' with the actual port of your Arduino
power_pin = 6  # Replace with your desired GPIO pin number
humidity_optimal_file = "/home/baby/Desktop/tempHumid/humidity_optimal.txt"
ambient_object_file = "/home/baby/Desktop/tempHumid/ambient_object.txt"
weicheng_url = "https://weicheng.app/baby_guardian/temp_humid.php"
humidifier_on = False
original_optimal = 0.0
pi_msg = False
wifi_msg = False
wifi_file = "/home/baby/Desktop/wifi_status.txt"
must_warn = False
warn_file = "/home/baby/Desktop/warning_status.txt"
music_play = False
music_play_file = "/home/baby/Desktop/music_status.txt"
note_file = "/home/baby/Desktop/note_status.txt"
first_run = True

GPIO.setmode(GPIO.BCM)
GPIO.setup(power_pin, GPIO.OUT)
GPIO.output(power_pin, GPIO.LOW)

# Add timeout for reading lines from serial port
def read_serial_line(timeout=5):
    start_time = time.time()
    while time.time() - start_time < timeout:
        if ser.in_waiting > 0:
            return ser.readline().decode('utf-8').strip()
    # If timeout occurs, turn off GPIO and raise TimeoutError
    GPIO.output(power_pin, GPIO.LOW)  # Turn off GPIO
    raise TimeoutError("Serial read timeout")

def post_to_weicheng(temp, humid, matrix):
    payload = {
        "device_serial": "1",
        "mode": "insert",
        "temp": temp,
        "humid": humid
    }
    try:
        requests.post(weicheng_url, data=payload)
    except Exception as e:
        print(f"Error posting to Weicheng: {e}")

    url = "https://weicheng.app/baby_guardian/alert.php"
    payload = {
        "mode": "insert",
        "device_serial": "1",
        "type": "TEMP_MATRIX",
        "status": "matrix_sent",
        "alert": matrix,
        "addition": ""
    }
    try:
        requests.post(url, data=payload)
    except Exception as e:
        print(f"Error posting to Weicheng: {e}")

def read_humidity_optimal():
    try:
        with open(humidity_optimal_file, 'r') as file:
            return float(file.read().strip())
    except FileNotFoundError:
        return None
    except Exception as e:
        print(f"Error reading humidity_optimal file: {e}")
        return None

def read_wifi():
    try:
        with open(wifi_file, 'r') as file:
            return file.read().strip()
    except FileNotFoundError:
        return None
    except Exception as e:
        return None

def read_warning():
    try:
        with open(warn_file, 'r') as file:
            return file.read().strip()
    except FileNotFoundError:
        return None
    except Exception as e:
        return None

def read_music():
    try:
        with open(music_play_file, 'r') as file:
            return file.read().strip()
    except FileNotFoundError:
        return None
    except Exception as e:
        return None

def read_note():
    try:
        with open(note_file, 'r') as file:
            return file.read().strip()
    except FileNotFoundError:
        return None
    except Exception as e:
        return None

try:
    while True:
        try:
            # Read temperature
            while True:
                line = read_serial_line()
                if "Temperature =" in line:
                    temp_value = line.split("=")[1].strip().split(" ")[0]
                    if temp_value not in ['0', '0.00', 'nan']:
                        break
            print(f"Temperature: {temp_value} C")

            # Read humidity
            while True:
                line = read_serial_line()
                if "Humidity =" in line:
                    humid_value = line.split("=")[1].strip().split(" ")[0]
                    if humid_value not in ['0', '0.00', 'nan']:
                        break
            print(f"Humidity: {humid_value} %")

            # Read AMG8833 Temperature Matrix
            while True:
                line = read_serial_line()
                print(line)
                if "MATRIX =" in line:
                    matrix = line
                    break

            # Read ambient temperature
            # ambient_value = read_serial_line().split("=")[1].strip().split(" ")[0]
            # print(f"Ambient: {ambient_value} C")

            # # Read object temperature
            # object_value = read_serial_line().split("=")[1].strip().split(" ")[0]
            # print(f"Object: {object_value} C")

            # # Save ambient and object values to file
            # with open(ambient_object_file, 'w') as file:
            #     file.write(f"Ambient: {ambient_value} C\nObject: {object_value} C")

            # Post to weicheng
            post_to_weicheng(temp_value, humid_value, matrix)

            if(first_run):
                time.sleep(10)
                first_run = False

            # Read optimal humidity from file
            optimal_humidity = read_humidity_optimal()

            wifi_line = read_wifi()

            warning = read_warning()

            if(warning is not None):
                time.sleep(5)
                if("WARN" in warning):
                    if(must_warn == False):
                        ser.write(("[W] "+warning).encode())
                        must_warn = True
                else:
                    if(must_warn == True):
                        ser.write("[J] Nothing needs attention yet, enjoy your day! This is Baby Guardian, your baby assistent.".encode())
                        wifi_msg = False
                
                if os.path.exists(warn_file):
                    # Delete the file
                    os.remove(warn_file)

            music_line = read_music()

            if(music_line is not None):
                time.sleep(5)
                if("PLAYING" in music_line):
                    if(music_play == False):
                        ser.write(("[H] "+music_line).encode())
                        music_play = True
                else:
                    if(music_play == True):
                        ser.write(("[I] Music paused because the sound level adjusted to " + music_line + ". Enjoy your day! This is Baby Guardian, your baby assistent.").encode())
                        music_play = False

                if os.path.exists(music_play_file):
                    # Delete the file
                    os.remove(music_play_file)

            notes = read_note()
            if(notes is not None):
                time.sleep(5)
                ser.write(("[A] "+notes).encode())
                if os.path.exists(note_file):
                    # Delete the file
                    os.remove(note_file)

            if(not pi_msg):
                time.sleep(5)
                ser.write("[G] Pi Connected. Enjoy your day! This is Baby Guardian, your baby assistent.".encode())
                pi_msg = True

            if(optimal_humidity is not None and optimal_humidity != original_optimal):
                time.sleep(5)
                original_optimal = optimal_humidity
                ser.write(("[B] "+str(optimal_humidity)+" %").encode())

            # Control humidifier based on optimal humidity
            if optimal_humidity is not None and isinstance(optimal_humidity, (float, int)) and float(humid_value) < optimal_humidity:
                GPIO.output(power_pin, GPIO.HIGH)  # Turn on humidifier
                humidifier_on = True
                ser.write("[F] Humidifier On".encode())
            else:
                if(humidifier_on):
                    GPIO.output(power_pin, GPIO.LOW)  # Turn off humidifier
                    ser.write("[C] Humidifier Off".encode())
                    humidifier_on = False

            if(wifi_line is not None):
                time.sleep(5)
                if("Connected" in wifi_line):
                    if(wifi_msg == False):
                        ser.write("[D] WiFi Connected. Enjoy your day! This is Baby Guardian, your baby assistent.".encode())
                        wifi_msg = True
                else:
                    if(wifi_msg == True):
                        ser.write("[E] WiFi Disconnected. Please Check Wiring Connection between Arduino and Pi.".encode())
                        wifi_msg = False

                if os.path.exists(wifi_file):
                    # Delete the file
                    os.remove(wifi_file)

        except Exception as e:
            print(f"Error in main loop: {e}")

except KeyboardInterrupt:
    # Close the serial port and cleanup GPIO when the script is interrupted
    ser.close()
    GPIO.cleanup()
    print("Serial port closed and GPIO cleaned up.")
except Exception as e:
    print(f"Unexpected error: {e}")
    # You may add additional logging or handling for unexpected errors here.
    # The script will continue running unless it encounters a KeyboardInterrupt.
    # You might want to consider adding a delay before restarting the loop.
    time.sleep(5)  # Sleep for 5 seconds before restarting the loop.