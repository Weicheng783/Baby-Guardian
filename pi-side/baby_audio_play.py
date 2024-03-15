import os
import requests
import subprocess
import json
import time
import sys

# Global variables
last_music_volume = 50
last_play_time = 0
ffplay_process = None  # Define ffplay_process globally
last_modified_time = 0  # Track the last modified time of the file
last_music_play = ""
first_time = 1

# Function to play the alert using ffplay
def play_alert(volume, play_time):
    global ffplay_process, last_music_volume, last_play_time, last_modified_time, last_music_play, first_time

    try:
        while True:
            try:
                # Check for music volume updates
                if os.path.exists("/home/baby/Desktop/music_volume.txt"):
                    # Get the last modified time of the file
                    current_modified_time = os.path.getmtime("/home/baby/Desktop/music_volume.txt")

                    if current_modified_time != last_modified_time:
                        last_modified_time = current_modified_time
                        with open("/home/baby/Desktop/music_volume.txt", "r") as file:
                            content = file.read().strip()

                        with open("/home/baby/Desktop/music_status.txt", 'w') as file:
                            file.write(content)

                        print("Music volume updated during playback:", content)
                        last_music_volume = content
                        if(first_time == 0):
                            sys.exit(0)
                        else:
                            first_time = 0
                        # Update the volume dynamically during playback
                        # update_volume(volume)

                # HTTP POST request to the specified URL
                response = requests.post("https://weicheng.app/baby_guardian/alert.php",
                                         data={"mode": "find", "device_serial": "1", "type": "Sound File", "status": "u2d_sent"})

                if response.status_code == 200 and response.text != "Not Found":
                    data = json.loads(response.text)

                    if os.path.exists("/home/baby/Desktop/music_volume.txt"):
                        # Get the last modified time of the file
                        current_modified_time = os.path.getmtime("/home/baby/Desktop/music_volume.txt")

                        if current_modified_time != last_modified_time:
                            last_modified_time = current_modified_time
                            with open("/home/baby/Desktop/music_volume.txt", "r") as file:
                                content = file.read().strip()
                            
                            with open("/home/baby/Desktop/music_status.txt", 'w') as file:
                                file.write(content)

                            print("Music volume updated during playback:", content)
                            # subprocess.run(["amixer", "-q", "-M", "sset", "PCM", f"{volume}%"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                            last_music_volume = content
                            # Update the volume dynamically during playback
                            # update_volume(volume)
                            # break
                            sys.exit(0)

                    if isinstance(data, list):
                        # Handle the case where the response is a list
                        for item in data:
                            alert_content = item.get("alert")
                            alert_id = item.get("id")

                            if alert_content and alert_id:
                                print("Playing alert with volume:", volume)
                                if(last_music_play == ""):
                                    ffplay_command = ["ffplay", "-volume", volume, "-af", f"volume=1"]

                                    # If play_time is not 0, add -ss to resume from the last play time
                                    if play_time != 0 and play_time is not None:
                                        ffplay_command.extend(["-ss", str(play_time)])

                                    ffplay_command.append(alert_content)
                                    last_music_play = ""
                                    # print(last_music_play)

                                    with open("/home/baby/Desktop/music_status.txt", 'w') as file:
                                        file.write("PLAYING: "+alert_content+" at volume: "+str(volume))

                                    # Kill existing ffplay process
                                    subprocess.run(["pkill","ffplay"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                                    print(ffplay_command)
                                    # Start a new ffplay process
                                    ffplay_process = subprocess.Popen(ffplay_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

                                    # Update the status using the retrieved 'id'
                                    update_status(alert_id)
                                else:
                                    ffplay_command = ["ffplay", "-volume", volume, "-af", f"volume=1"]
                                    # If play_time is not 0, add -ss to resume from the last play time
                                    if play_time != 0 and play_time is not None:
                                        ffplay_command.extend(["-ss", str(play_time)])
                                    ffplay_command.append(last_music_play)
                                    print(ffplay_command)
                                    subprocess.run(["pkill","ffplay"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)

                                    with open("/home/baby/Desktop/music_status.txt", 'w') as file:
                                        file.write("PLAYING: "+alert_content+" at volume: "+str(volume))
                                    # Kill existing ffplay process
                                    # kill_ffplay_process()
                                    # Start a new ffplay process
                                    ffplay_process = subprocess.Popen(ffplay_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                                    # Update the status using the retrieved 'id'
                                    update_status(alert_id)

                    elif isinstance(data, dict):
                        # Handle the case where the response is a dictionary
                        alert_content = data.get("alert")
                        alert_id = data.get("id")

                        if alert_content and alert_id:
                            print("Playing alert with volume:", volume)
                            if(last_music_play == ""):
                                ffplay_command = ["ffplay", "-volume", volume, "-af", f"volume=1"]

                                # If play_time is not 0, add -ss to resume from the last play time
                                if play_time != 0 and play_time is not None:
                                    ffplay_command.extend(["-ss", str(play_time)])

                                with open("/home/baby/Desktop/music_status.txt", 'w') as file:
                                    file.write("PLAYING: "+alert_content+" at volume: "+str(volume))

                                ffplay_command.append(alert_content)
                                last_music_play = ""
                                print(last_music_play)
                                print(ffplay_command)

                                # Kill existing ffplay process
                                subprocess.run(["pkill","ffplay"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)

                                # Start a new ffplay process
                                ffplay_process = subprocess.Popen(ffplay_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

                                # Update the status using the retrieved 'id'
                                update_status(alert_id)
                            else:
                                ffplay_command = ["ffplay", "-volume", volume, "-af", f"volume=1"]
                                # If play_time is not 0, add -ss to resume from the last play time
                                if play_time != 0 and play_time is not None:
                                    ffplay_command.extend(["-ss", str(play_time)])
                                ffplay_command.append(last_music_play)
                                print(ffplay_command)

                                with open("/home/baby/Desktop/music_status.txt", 'w') as file:
                                    file.write("PLAYING: "+alert_content+" at volume: "+str(volume))

                                subprocess.run(["pkill","ffplay"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                                # Kill existing ffplay process
                                # kill_ffplay_process()
                                # Start a new ffplay 
                                update_status(alert_id)

                # Set play_time to 0 if the thread stopped by itself
                play_time = 0
                time.sleep(2)  # Adjust the interval as needed

            except Exception as e:
                print("Error in play_alert:", e)
                time.sleep(2)  # Sleep for a while before retrying

    except Exception as e:
        # pass
        print("Error in play_alert:", e)

# Function to update the volume dynamically during playback
def update_volume(volume):
    pass
    # global ffplay_process
    # if ffplay_process and ffplay_process.poll() is None:
    #     # If the process is still running, update the volume
    #     subprocess.run(["amixer", "-q", "-M", "sset", "PCM", f"{volume}%"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)

# Function to kill the ffplay process using pkill
def kill_ffplay_process():
    global ffplay_process

    try:
        if ffplay_process and ffplay_process.poll() is None:
            # If the process is still running, terminate it
            ffplay_process.terminate()
            ffplay_process.wait()
    except Exception as e:
        print("Error killing ffplay process:", e)

# Function to update status using HTTP POST
def update_status(alert_id):
    try:
        # HTTP POST request to update the status
        update_response = requests.post("https://weicheng.app/baby_guardian/alert.php",
                                        data={"mode": "update", "id": alert_id, "status": "u2d_received"})

        if update_response.status_code == 200:
            print("Status updated successfully.")

    except Exception as e:
        print("Error updating status:", e)

# Function to check for music volume updates
def check_music_volume():
    global last_music_volume, last_play_time

    while True:
        try:
            if os.path.exists("/home/baby/Desktop/music_volume.txt"):
                with open("/home/baby/Desktop/music_volume.txt", "r") as file:
                    content = file.read().strip()

                if content != last_music_volume:
                    print("Music volume updated:", content)
                    last_music_volume = content
                    subprocess.run(["pkill","ffplay"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                    # Kill existing ffplay process
                    kill_ffplay_process()
                    # Start a new thread to play the alert
                    play_alert(last_music_volume, last_play_time)

            time.sleep(1)  # Adjust the interval as needed

        except Exception as e:
            print("Error in check_music_volume:", e)

if __name__ == "__main__":
    # Create and start the thread to check for music volume updates
    check_music_volume()

    try:
        # Keep the main thread running
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        # Handle Ctrl+C to gracefully exit the program
        # Kill ffplay process before exiting
        kill_ffplay_process()
        print("\nProgram terminated.")