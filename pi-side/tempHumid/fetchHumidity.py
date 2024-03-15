import requests
import time

weicheng_url = "https://weicheng.app/baby_guardian/alert.php"
humidity_optimal_file = "/home/baby/Desktop/tempHumid/humidity_optimal.txt"

def fetch_alerts():
    payload = {
        "device_serial": "1",
        "mode": "find",
        "type": "Humidifier Intensity",
        "status": "u2d_sent"
    }
    try:
        response = requests.post(weicheng_url, data=payload)
        return response.json()
    except Exception as e:
        # print(f"Error fetching alerts: {e}")
        return []

def update_alert(id):
    payload = {
        "mode": "update",
        "id": id,
        "status": "u2d_received"
    }
    try:
        requests.post(weicheng_url, data=payload)
    except Exception as e:
        print(f"Error updating alert status: {e}")

def process_alerts(alerts):
    for alert in alerts:
        try:
            alert_id = alert.get("id")
            alert_text = alert.get("alert")
            
            # Save alert to file
            if alert_text is not None and alert_text == "off":
                print(f"Set Always {alert_value}")
                with open(humidity_optimal_file, 'w') as file:
                    file.write("off")
                # Update alert status
                update_alert(alert_id)
                
            elif alert_text is not None and alert_text.replace('.', '').isdigit():
                alert_value = float(alert_text)
                if(alert_value >= 0 and alert_value <= 100):
                    pass
                else:
                    alert_value = float(max(0, min(100, alert_value)))  # Ensure the value is between 0 and 100
                with open(humidity_optimal_file, 'w') as file:
                    file.write(str(alert_value))  # Save as an integer

                print(f"Alert value updated: {alert_value}")

                # Update alert status
                update_alert(alert_id)

            elif not alert_text:
                # If the alert is an empty string, set to 0 and update status
                with open(humidity_optimal_file, 'w') as file:
                    file.write("0")

                print("Alert value set to 0")

                # Update alert status
                update_alert(alert_id)

        except Exception as e:
            # print(f"Error processing alert: {e}")
            pass

if __name__ == "__main__":
    try:
        while True:
            alerts = fetch_alerts()
            if alerts:
                process_alerts(alerts)
            else:
                # print("No alerts found.")
                pass
            
            # Wait for 3 seconds before the next iteration
            time.sleep(3)

    except KeyboardInterrupt:
        print("Script interrupted.")
    except Exception as e:
        print(f"Unexpected error: {e}")