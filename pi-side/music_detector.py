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

def parse_response(response):
    try:
        data = json.loads(response)
        return data.get("alert")
    except json.JSONDecodeError:
        return None

def save_to_file(alert):
    if alert and alert.isdigit():
        with open("music.txt", "w") as file:
            file.write(alert)
            print(f"Saved alert to music.txt")

def main():
    while True:
        # First POST request with mode "find" and status "u2d_sent"
        response_text = make_post_request("find", "1", "Sound File", "u2d_sent")
        if response_text:
            alert_term = parse_response(response_text)
            if alert_term:
                print(f"Received alert term: {alert_term}")

                # Save to file if not empty and is a number
                save_to_file(alert_term)

                # Second POST request with mode "update" and status "u2d_received"
                response_text = make_post_request("update", "1", alert_term, "u2d_received")
                if response_text:
                    print("Update request successful.")
                else:
                    print("Failed to send update request.")

        time.sleep(3)

if __name__ == "__main__":
    main()