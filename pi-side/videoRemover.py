import os
from datetime import datetime, timedelta
import time

def remove_old_files(folder_path, minutes_threshold):
    current_time = datetime.now()
    threshold_time = current_time - timedelta(minutes=minutes_threshold)

    try:
        for filename in os.listdir(folder_path):
            file_path = os.path.join(folder_path, filename)
            if os.path.isfile(file_path):
                modified_time = datetime.fromtimestamp(os.path.getmtime(file_path))
                if modified_time < threshold_time:
                    os.remove(file_path)
                    print(f"Removed: {filename}")
    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    folder_path = "/home/baby/Desktop/videos/"
    minutes_threshold = 30

    while True:
        remove_old_files(folder_path, minutes_threshold)
        time.sleep(60)  # Check every minute (adjust as needed)