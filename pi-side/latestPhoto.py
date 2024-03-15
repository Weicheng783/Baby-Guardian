import os
import time
import requests

# Function to find the latest modified file in a folder
def find_latest_file(folder_path):
    files = [f for f in os.listdir(folder_path) if os.path.isfile(os.path.join(folder_path, f))]
    if not files:
        return None
    latest_file = max(files, key=lambda f: os.path.getmtime(os.path.join(folder_path, f)))
    return os.path.join(folder_path, latest_file)

# Function to get the serial number from a file path
def get_serial_number(file_path):
    # You may need to implement your logic to extract the serial number
    # For this example, I assume the serial number is in the file name
    # file_name = os.path.basename(file_path)
    # serial_number = file_name.split('_')[0]  # Adjust this based on your file naming convention
    return "1"

# Function to perform HTTP POST request to upload a file
def upload_file(file_path, serial_number):
    url = 'https://weicheng.app/baby_guardian/latestPhoto.php'  # Replace with the actual URL of your PHP script
    files = {'image': open(file_path, 'rb')}
    data = {'serial_number': serial_number}

    try:
        response = requests.post(url, files=files, data=data)
        response.raise_for_status()  # Raise an HTTPError for bad responses
        print(response.text)
    except requests.exceptions.RequestException as e:
        # Handle exceptions here
        print(f"Error uploading file: {e}")
        # You may want to add logging or other error-handling mechanisms here

# Main loop to check for the latest file and upload it every 30 seconds
folder_path = '/home/baby/Desktop/images'  # Replace with the actual path to your files
while True:
    latest_file_path = find_latest_file(folder_path)
    if latest_file_path:
        serial_number = get_serial_number(latest_file_path)
        if serial_number == '1':
            upload_file(latest_file_path, serial_number)
    time.sleep(30)