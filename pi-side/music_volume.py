import requests
import json
import time
import os
import platform
import subprocess

def make_post_request(mode, device_serial, alert_type, status):
    url = "https://weicheng.app/baby_guardian/alert.php"
    payload = {
        "mode": mode,
        "device_serial": device_serial,
        "type": alert_type,
        "status": status
    }
    response = requests.post(url, data=payload)
    return response.text if response else None

def make_post_request_2(mode, id, status):
    url = "https://weicheng.app/baby_guardian/alert.php"
    payload = {
        "mode": mode,
        "id": id,
        "status": status
    }
    response = requests.post(url, data=payload)
    return response.text if response else None

def parse_response(response):
    try:
        data = json.loads(response)
        if isinstance(data, list) and data:
            return data[0]
    except json.JSONDecodeError:
        return None

def save_to_file(alert):
    try:
        if "alert" in alert:
            volume_value = float(alert["alert"])
            # Round down to the nearest integer
            rounded_value = max(0, min(100, int(volume_value)))
            with open("music_volume.txt", "w") as file:
                file.write(str(rounded_value))
                print(f"Saved alert to music_volume.txt")
    except (AttributeError, TypeError):
        pass  # Ignore if alert is not a dictionary or doesn't contain "alert" key

def set_speaker_volume_windows(volume):
    try:
        from comtypes import CLSCTX_ALL
        from ctypes import cast, POINTER
        from pycaw.pycaw import AudioUtilities, IAudioEndpointVolume

        devices = AudioUtilities.GetSpeakers()
        interface = devices.Activate(
            IAudioEndpointVolume._iid_, CLSCTX_ALL, None
        )
        volume_interface = cast(interface, POINTER(IAudioEndpointVolume))
        volume_interface.SetMasterVolumeLevelScalar(volume / 100, None)
    except ImportError as e:
        print(f"Error: {e}")
        print("pycaw library is required for setting volume on Windows.")
    except Exception as e:
        print(f"Error setting volume on Windows: {e}")

def set_speaker_volume_linux(volume):
    try:
        subprocess.run(["sudo", "amixer", "-q", "-M", "sset", "PCM", f"{volume}%"], check=True)
        command = ["ffplay", "-f", "lavfi", "-i", "sine=frequency=1000", "-af", "volume=2.5"]
    
        try:
            process = subprocess.Popen(command)
            time.sleep(2)
        except Exception as e:
            print(f"Error playing sound: {e}")
        finally:
            if process.poll() is None:
                process.terminate()
    except subprocess.CalledProcessError as e:
        print(f"Error: {e}")
        print("Failed to set volume using amixer.")
    except Exception as e:
        print(f"Error setting volume on Linux: {e}")

def set_speaker_volume(volume):
    try:
        if platform.system() == "Linux":
            set_speaker_volume_linux(volume)
        else:
            print("Unsupported operating system for setting volume.")
    except Exception as e:
        print(f"Error setting speaker volume: {e}")

def main():
    try:
        # Read the int value from music_volume.txt if it exists
        if os.path.exists("music_volume.txt"):
            with open("music_volume.txt", "r") as file:
                volume_value = file.read().strip()
                # print(volume_value)
                if volume_value.isdigit() or volume_value.isdecimal():
                    set_speaker_volume(int(volume_value))
                    print(f"Set speaker volume to {volume_value} from music_volume.txt")

        while True:
            # First POST request with mode "find" and status "u2d_sent"
            response_text = make_post_request("find", "1", "Sound Level", "u2d_sent")
            if response_text:
                alert_term = parse_response(response_text)
                if alert_term:
                    print(f"Received alert term: {alert_term}")

                    # Save to file if not empty and is a number
                    save_to_file(alert_term)

                    # Set speaker volume to the received alert term
                    if "alert" in alert_term and alert_term["alert"].isdigit():
                        set_speaker_volume(int(alert_term["alert"]))

                    # Second POST request with mode "update" and status "u2d_received"
                    response_text = make_post_request_2("update", alert_term["id"], "u2d_received")
                    if response_text:
                        print("Update request successful.")
                    else:
                        print("Failed to send update request.")

            time.sleep(3)

    except Exception as e:
        print(f"An unexpected error occurred: {e}")

if __name__ == "__main__":
    main()