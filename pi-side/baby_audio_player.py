import sounddevice as sd
import soundfile as sf
import requests
import os
import tempfile
import random
import string

def play_internet_sound(url, device=None):
    # Download the audio file from the internet
    response = requests.get(url)

    # Create a temporary file with a random name
    temp_filename = generate_random_filename()
    with open(temp_filename, 'wb') as temp_file:
        temp_file.write(response.content)

    # Read the audio file using soundfile
    audio_data, samplerate = sf.read(temp_filename)

    # Play the audio to the specified device
    sd.play(audio_data, samplerate=samplerate, device=device)
    sd.wait()

    # Delete the temporary file
    os.remove(temp_filename)

def generate_random_filename():
    random_string = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
    return os.path.join(tempfile.gettempdir(), f"temp_audio_{random_string}.wav")

def print_available_devices():
    devices = sd.query_devices()
    print("Available Sound Devices:")
    
    for i, device in enumerate(devices):
        print(f"{i}: {device['name']} - {device['hostapi']}")

if __name__ == "__main__":
    print_available_devices()

    device_index = int(input("Enter the index of the desired output device: "))
    internet_sound_url = "https://file-examples.com/storage/fedf16213165ce2d096e19a/2017/11/file_example_MP3_700KB.mp3"

    try:
        play_internet_sound(internet_sound_url, device=device_index)
    except Exception as e:
        print(f"Error: {e}")