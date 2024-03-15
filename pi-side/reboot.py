import requests
import subprocess
import json
import time

def fetch_alerts():
    payload = {
        'mode': 'find',
        'device_serial': '1',
        'type': 'reboot',
        'status': 'u2d_sent'
    }

    response = requests.post('https://weicheng.app/baby_guardian/alert.php', data=payload)
    return response.json()

def update_alert(alert_id):
    payload = {
        'mode': 'update',
        'id': alert_id,
        'status': 'u2d_received'
    }

    requests.post('https://weicheng.app/baby_guardian/alert.php', data=payload)

def reboot_computer():
    command = ["ffplay", "-f", "lavfi", "-i", "sine=frequency=1000", "-af", "volume=2.5"]

    try:
        process = subprocess.Popen(command)
        time.sleep(2)
    except Exception as e:
        print(f"Error playing sound: {e}")
    finally:
        if process.poll() is None:
            process.terminate()
    subprocess.run(['sudo', 'reboot'])

def main():
    while True:
        try:
            # Fetch alerts
            alerts = fetch_alerts()

            # Check if there are any entries in the response
            if alerts:
                for alert in alerts:
                    # Update each entry
                    update_alert(alert['id'])

                # Reboot the computer
                reboot_computer()
        except Exception as e:
            pass
            # print(f"Error: {e}")

if __name__ == "__main__":
    main()