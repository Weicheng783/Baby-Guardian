import os
import random
import subprocess
import time
import requests
import face.image_detect

def get_latest_modified_file(folder_path, last_modified_time):
    files = [f for f in os.listdir(folder_path) if f.endswith('.jpg')]
    files.sort(key=lambda x: os.path.getmtime(os.path.join(folder_path, x)), reverse=True)

    for file in files:
        modification_time = os.path.getmtime(os.path.join(folder_path, file))
        if modification_time != last_modified_time:
            return file, modification_time

    return None, None

def get_emotion_result(image_path):
    return face.image_detect.analyze_emotion(image_path)

def get_emotion_result_test():
    return face.image_detect.analyze_video_emotion()

def read_notification_frequency():
    try:
        with open("notification_freq.txt", "r") as file:
            return int(file.read().strip())
    except (FileNotFoundError, ValueError):
        return 5
    
def generate_random_emotion():
    emotions = {'crying': 0, 'happy': 1, 'quiet': 2, 'sleepy': 3, 'none': 4}
    random_emotion = random.choice(list(emotions.keys()))
    return random_emotion

def check_notification_frequency():
    # Initialize alert and freq with default values
    alert, freq = None, 20

    try:
        freq = read_notification_frequency()

        payload = {
            'mode': 'emotion_report_frequency',
            'device_serial': '1',
            'type': 'Negative Emotion Report Intensity',
            'status': 'u2d_sent'
        }
        response = requests.post('https://weicheng.app/baby_guardian/alert.php', data=payload)
        json_response = response.json()

        alert = json_response.get('alert', None)
        if(alert != None and freq != int(alert)):
            freq = int(alert)
            with open('notification_freq.txt', 'w') as file:
                # Write content to the file
                file.write(alert)
    except Exception as e:
        print(f"Error checking notification frequency: {e}")

    return alert, freq

def save_file_to_server(image_path):
    files = {'file': open(image_path, 'rb')}
    response = requests.post('https://weicheng.app/baby_guardian/file_saver.php', files=files)
    return response.text.strip()

def insert_alert(emotion_result, saved_file_result):
    alert_message = f"Your baby seems {emotion_result}, please check and take care."
    with open("/home/baby/Desktop/warning_status.txt", 'w') as file:
        file.write("WARN: "+alert_message)
    payload = {
        'mode': 'insert',
        'device_serial': '1',
        'alert': alert_message,
        'type': 'Negative Emotion',
        'status': 'd2u_sent',
        'addition': f'https://weicheng.app/baby_guardian/photos/{saved_file_result}'
    }
    response = requests.post('https://weicheng.app/baby_guardian/alert.php', data=payload)
    return response.text.strip()

def main():
    folder_path = '/home/baby/Desktop/images/'
    last_modified_time = None
    
    cryA = '/home/baby/Desktop/crying/a.png'
    cryB = '/home/baby/Desktop/crying/b.png'
    cryC = '/home/baby/Desktop/crying/c.png'
    cryD = '/home/baby/Desktop/crying/d.png'
    cryE = '/home/baby/Desktop/crying/e.png'
    cryF = '/home/baby/Desktop/crying/f.png'
    counter = 1

    while True:
        # Get the latest modified file
        latest_file, modification_time = get_latest_modified_file(folder_path, last_modified_time)
        alert, freq = check_notification_frequency()
        if latest_file:
            last_modified_time = modification_time

            # Get emotion result from the detector
            if(freq != 999 and freq != 989):
                emotion_result = get_emotion_result(os.path.join(folder_path, latest_file))
                counter = 1
            elif(freq == 999):
                emotion_result = "crying"
            else:
                emotion_result = random.choice(['happy','quiet','sleepy','none'])
            # emotion_result = get_emotion_result_test()
            # emotion_result = generate_random_emotion()
            print("emotion_result:", emotion_result)

            if emotion_result and emotion_result in ['crying', 'cry']:
                # Check user's notification frequency
                alert, freq = check_notification_frequency()

                if alert != 'none':
                    # Save the file to the server
                    if(freq != 999):
                        saved_file_result = save_file_to_server(os.path.join(folder_path, latest_file))
                    else:
                        the_chosen = random.choice([f for f in os.listdir("/home/baby/Desktop/crying/") if f.endswith('.png')])
                        the_chosen = os.path.join("/home/baby/Desktop/crying/", the_chosen)
                        if(counter == 1):
                            saved_file_result = save_file_to_server(the_chosen)
                            counter += 1
                        elif(counter == 2):
                            saved_file_result = save_file_to_server(the_chosen)
                            counter += 1
                        elif(counter == 3):
                            saved_file_result = save_file_to_server(the_chosen)
                            counter += 1
                        elif(counter == 4):
                            saved_file_result = save_file_to_server(the_chosen)
                            counter += 1
                        elif(counter == 5):
                            saved_file_result = save_file_to_server(the_chosen)
                            counter += 1
                        elif(counter == 6):
                            saved_file_result = save_file_to_server(the_chosen)
                            counter += 1
                        elif(counter == 7):
                            saved_file_result = save_file_to_server(os.path.join(folder_path, latest_file))
                            counter = 1
                        else:
                            saved_file_result = save_file_to_server(os.path.join(folder_path, latest_file))

                    print("file_result", saved_file_result)
                    
                    # Insert alert information to the server
                    insert_result = insert_alert(emotion_result, saved_file_result)
                    print("insert_result", insert_result)
                    try:
                        subprocess.run(["python", "/home/baby/Desktop/sound/sound_pred_final.py"], check=True)
                        print("Second script executed successfully.")
                    except subprocess.CalledProcessError as e:
                        print(f"Error executing second script: {e}")

            elif emotion_result and emotion_result in ['happy', 'quiet', 'sleepy', 'none']:
                with open("/home/baby/Desktop/warning_status.txt", 'w') as file:
                    file.write("CLEAR")

        # Adjust sleep time based on the notification frequency
        sleep_time = freq if freq else 20
        if(freq and (freq == 999 or freq == 989)): # 999 is the magic number for mock test
            sleep_time = 20
        print("freq:", freq)
        time.sleep(sleep_time)

if __name__ == "__main__":
    main()