import requests
import json
import time

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

def make_post_request_update(mode, device_serial, alert_id, status):
    url = "https://weicheng.app/baby_guardian/alert.php"
    payload = {
        "mode": mode,
        "device_serial": device_serial,
        "id": alert_id,
        "status": status
    }
    response = requests.post(url, data=payload)
    return response.text if response else None

def parse_response(response):
    try:
        data = json.loads(response)
        return data
    except json.JSONDecodeError:
        return None

def save_to_file(alert):
    if alert:
        with open("/home/baby/Desktop/soothing_intensity.txt", "w") as file:
            file.write(alert)
            print(f"Saved alert to soothing_intensity.txt")

def main():
    while True:
        # First POST request with mode "find" and status "u2d_sent"
        response_text = make_post_request("find", "1", "Soothing Intensity", "u2d_sent")
        if response_text:
            alert_term = parse_response(response_text)
            # Handle the case where the response is a list
            if(alert_term is not None):
                print(f"Received alert term: {response_text}")
                for item in alert_term:
                    alert_content = item.get("alert")
                    alert_id = item.get("id")
                    if alert_content:
                        print(f"Received alert term: {alert_content}, id: {alert_id}")

                        # Save to file if not empty and is a number
                        save_to_file(alert_content)

                        # Second POST request with mode "update" and status "u2d_received"
                        response_text = make_post_request_update("update", "1", alert_id, "u2d_received")
                        if response_text:
                            print("Update request successful.")
                        else:
                            print("Failed to send update request.")

        time.sleep(3)

if __name__ == "__main__":
    main()