import subprocess
import time
import os
import random
import requests  # Import the requests library

def read_notification_frequency():
    try:
        with open("/home/baby/Desktop/notification_freq.txt", "r") as file:
            return int(file.read().strip())
    except (FileNotFoundError, ValueError):
        return 5

def random_wav_file(folder_address):
    # List all files in the folder
    files = [f for f in os.listdir(folder_address) if os.path.isfile(os.path.join(folder_address, f))]
    
    # Filter files with ".wav" suffix
    wav_files = [f for f in files if f.endswith('.wav')]
    
    if not wav_files:
        return None  # No .wav files found in the folder
    
    # Choose a random .wav file
    random_wav_file = random.choice(wav_files)
    
    # Return the full file name including the folder address
    return os.path.join(folder_address, random_wav_file)

# Function to record 5 seconds of audio from RTMP source and save it as "temp_audio.wav"
def record_audio():
    rtmp_source = "rtmp://localhost/live/sound"
    output_file = "/home/baby/Desktop/sound/temp_audio.wav"

    # Run ffmpeg command to record audio
    command = [
        "ffmpeg",
        "-i", rtmp_source,
        "-t", "5",  # Record for 5 seconds
        "-ac", "1",  # Mono audio
        "-ar", "44100",  # Sample rate
        "-y",  # Overwrite output file if exists
        output_file
    ]

    try:
        subprocess.run(command, check=True)
        print("Audio recording complete.")
    except subprocess.CalledProcessError as e:
        print(f"Error during audio recording: {e}")

# Function to judge label based on class name
def judgelabel(labelname):
    if labelname == 'sleepy':
        return 4
    elif labelname == 'hug':
        return 2
    elif labelname == 'uncomfortable':
        return 5
    elif labelname == 'diaper':
        return 1
    elif labelname == 'awake':
        return 0
    elif labelname == 'hungry':
        return 3

# Function to extract audio features
def extract_features(file_name):
    try:
        audio, sample_rate = librosa.load(file_name, res_type='kaiser_fast')
        mfccs = librosa.feature.mfcc(y=audio, sr=sample_rate, n_mfcc=40)
        pad_width = max_pad_len - mfccs.shape[1]
        mfccs = np.pad(mfccs, pad_width=((0, 0), (0, pad_width)), mode='constant')
    except Exception as e:
        print("Error encountered while parsing file: ", e)
        return None
    return mfccs

# Function to perform prediction
def print_prediction_sta(file_name):
    prediction_feature = extract_features(file_name)
    prediction_feature = prediction_feature.reshape(1, num_rows, num_columns, num_channels)
    predict_x = model.predict(prediction_feature)
    classes_x = np.argmax(predict_x, axis=1)
    predicted_class = le.inverse_transform(classes_x)
    return class_labels[classes_x[0]]

# Function to send HTTP POST request
def send_alert(result):
    url = "https://weicheng.app/baby_guardian/alert.php"
    payload = {
        "mode": "insert",
        "device_serial": "1",
        "alert": f"Your baby's crying might related to: {result}, please take care and check.",
        "type": "Baby Crying",
        "status": "d2u_sent",
        "addition": ""
    }

    try:
        response = requests.post(url, data=payload)
        response.raise_for_status()
        print("HTTP POST request sent successfully.")
    except requests.RequestException as e:
        print(f"Error sending HTTP POST request: {e}")

class_labels = ["awake", "diaper", "hug", "hungry", "sleepy", "uncomfortable"]

if(read_notification_frequency() != 999):
    import resampy
    import numpy as np
    import matplotlib.pyplot as plt
    import pandas as pd
    import tensorflow as tf
    import librosa
    import librosa.display
    from sklearn.preprocessing import LabelEncoder
    from tensorflow.keras.utils import to_categorical
    # Load pre-trained model and set parameters
    model = tf.keras.models.load_model('/home/baby/Desktop/sound/babysound_classification_tf_cnn.h5')
    le = LabelEncoder()
    yy = to_categorical(le.fit_transform(class_labels))
    max_pad_len = 500
    num_rows = 40
    num_columns = 500
    num_channels = 1

    # Record 5 seconds of audio
    record_audio()

    # Perform prediction on the recorded audio
    filename = '/home/baby/Desktop/sound/temp_audio.wav'
    result = print_prediction_sta(filename)
    print("Predicted class:", result)
else:
    import pygame
    pygame.init()
    # Using Magic Number 999 for Show Casing / Demo / Mock Test
    the_chosen = random.choice(class_labels)
    the_chosen_wav = random_wav_file("/home/baby/Desktop/sound/work/train/" + the_chosen)
    # subprocess.run(["pkill","ffplay"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    # ffplay_command = ["ffplay", "-volume", "100", "-af", f"volume=1"]
    # print(the_chosen_wav)
    # pygame.mixer.Sound(the_chosen_wav).play()
    # ffplay_command.append(the_chosen_wav)
    # ffplay_process = subprocess.Popen(ffplay_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    result = the_chosen

# Send HTTP POST request with the result
send_alert(result)