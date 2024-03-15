import subprocess
import os
import time
import requests
from pyzbar.pyzbar import decode
from PIL import Image

def check_internet_connection():
    try:
        subprocess.check_output(["ping", "-c", "1", "weicheng.app"])
        print("Connected to the internet.")
        return True
    except subprocess.CalledProcessError:
        print("No internet connection or cannot reach weicheng.app.")
        return False

def reset_and_clear_internet():
    print("Resetting and clearing internet settings...")
    # subprocess.run(["nmcli", "radio", "wifi", "off"])
    subprocess.run(["nmcli", "device", "disconnect", "wlan0"])  # Adjust the interface name if needed

    # Remove all saved WiFi connections
    # saved_connections = subprocess.check_output(["nmcli", "-t", "-f", "SSID", "connection", "show"]).decode("utf-8").strip().split("\n")
    # for connection in saved_connections:
    #     ssid = connection.split(":")[1]
    #     subprocess.run(["nmcli", "connection", "delete", "id", ssid])

    # subprocess.run(["nmcli", "radio", "wifi", "on"])
    # print("Internet settings reset and cleared. Rebooting...")
    # subprocess.run(["reboot"])

def configure_wifi(ssid, password):
    subprocess.run(["nmcli", "device", "wifi", "connect", ssid, "password", password])

def capture_and_save_picture(folder_path, picture_name):
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)

    picture_path = os.path.join(folder_path, picture_name)

    subprocess.run(["libcamera-still", "-o", picture_path])

    while not os.path.exists(picture_path):
        time.sleep(1)

    print(f"Picture '{picture_name}' taken and saved in '{folder_path}'.")
    return picture_path

def play_sound(duration, frequency):
    subprocess.run(["sudo", "amixer", "-q", "-M", "sset", "PCM", "100%"], check=True)
    command = ["ffplay", "-f", "lavfi", "-i", "sine=frequency="+str(frequency), "-af", "volume=2.5"]
    
    try:
        process = subprocess.Popen(command)
        time.sleep(duration)
    except Exception as e:
        print(f"Error playing sound: {e}")
    finally:
        if process.poll() is None:
            process.terminate()

def decode_qr_code(picture_path):
    with Image.open(picture_path) as img:
        decoded_objects = decode(img)
        if decoded_objects:
            print("QR Code content:")
            # play_sound(2, 800)
            for obj in decoded_objects:
                qr_data = obj.data.decode('utf-8')
                print(f"Data: {qr_data}")
                print(f"Type: {obj.type}")
                return qr_data
        else:
            print("No QR code found in the picture.")
            return None

def parse_wifi_info(qr_data):
    wifi_info = {}
    elements = qr_data.split(',')
    for element in elements:
        key, value = element.split('=')
        wifi_info[key] = value
    return wifi_info

def send_post_request_register(username):
    url = "https://weicheng.app/baby_guardian/register.php"
    data = {
        "username": username,
        "serial_number": "1",
        "model_description": "The Baby Guardian Uno"
    }
    try:
        response = requests.post(url, data=data)
        if response.status_code == 200:
            print("POST request sent successfully.")
        else:
            print(f"POST request failed with status code: {response.status_code}")
    except requests.RequestException as e:
        print(f"Error sending POST request: {e}")

def send_post_request():
    url = "https://weicheng.app/baby_guardian/queryDevice.php"
    data = {
        "serial_number": "1",
    }
    try:
        response = requests.post(url, data=data)
        if response.status_code == 200:
            result = response.json()
            isUserAssociated = result.get('isUserAssociated', False)
            print(f"User associated with the device: {isUserAssociated}")

            if not isUserAssociated:
                play_sound(3, 500)
                reset_and_clear_internet()
        else:
            print(f"POST request failed with status code: {response.status_code}")
    except requests.RequestException as e:
        print(f"Error sending POST request: {e}")

if __name__ == "__main__":
    picture_folder = "/home/baby/Desktop/pictures"
    picture_name = "captured_picture.jpg"
    run_no_internet_connection_part = False
    internet_connection_good = False
    first_time = True

    while True:
        if not internet_connection_good and (run_no_internet_connection_part or not check_internet_connection()):
            picture_path = capture_and_save_picture(picture_folder, picture_name)
            qr_data = decode_qr_code(picture_path)
            with open("/home/baby/Desktop/wifi_status.txt", 'w') as file:
                file.write("NONE")

            if qr_data:
                wifi_info = parse_wifi_info(qr_data)
                ssid = wifi_info.get('ssid')
                password = wifi_info.get('password')
                username = wifi_info.get('username')

                if ssid and password and username:
                    configure_wifi(ssid, password)
                    print(f"Configuring WiFi for SSID: {ssid}")
                    # time.sleep(10)  # Adjust the sleep duration based on your system's connection time

                    if check_internet_connection():
                        print("WiFi connection established.")
                        internet_connection_good = True
                        
                        # play_sound(2, 1000)
                        send_post_request_register(username)
                        if(first_time):
                            send_post_request()
                            first_time = False
                    else:
                        print("WiFi connection failed.")
                        with open("/home/baby/Desktop/wifi_status.txt", 'w') as file:
                            file.write("NONE")
        else:
            print("Internet connection is good. Sleeping for 60 seconds.")
            with open("/home/baby/Desktop/wifi_status.txt", 'w') as file:
                file.write("Connected")
            if(first_time):
                send_post_request()
                first_time = False
            time.sleep(30)