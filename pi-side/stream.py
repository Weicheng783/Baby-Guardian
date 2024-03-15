import subprocess
import time
import threading
import psutil

def check_internet_connection():
    try:
        subprocess.run(["ping", "-c", "1", "weicheng.app"], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True
    except subprocess.CalledProcessError:
        return False

def is_process_running(process_name):
    for process in psutil.process_iter(['pid', 'name', 'cmdline']):
        if process.info['name'] == process_name:
            return True
    return False

def execute_streaming_command():
    streaming_command = (
        "libcamera-vid --height 1080 --width 1920 --libav-audio 1 -t 0 -o - | "
        "ffmpeg -i - -c:v h264 -preset ultrafast -b:v 2M -c:a aac -b:a 192k -f flv "
        "rtmp://weicheng.app/live/baby_guardian_1 -vf fps=1/3,drawtext=text='%{localtime}':fontsize=24:fontcolor=white:x=10:y=10 "
        "/home/baby/Desktop/images/image_%03d.jpg -c:v copy -f segment -segment_time 60 -segment_format mp4 -reset_timestamps 1 -strftime 1 "
        "/home/baby/Desktop/videos/video_%Y%m%d%H%M%S.mp4"
    )

    restart_interval = 60  # 1 minute

    while True:
        if check_internet_connection():
            if is_process_running("libcamera-vid"):
                print("Force killing libcamera-vid process...")
                subprocess.run(["pkill", "-f", "libcamera-vid"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)

            print("Starting libcamera-vid process...")
            subprocess.Popen(streaming_command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

            time.sleep(restart_interval)
        else:
            print("No internet connection. Checking libcamera-vid process...")
            if is_process_running("libcamera-vid"):
                print("libcamera-vid process detected. Force killing...")
                subprocess.run(["pkill", "-f", "libcamera-vid"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            print(f"Waiting for {restart_interval} seconds before rechecking...")
            time.sleep(restart_interval)

if __name__ == "__main__":
    # Create a separate thread for the streaming command
    streaming_thread = threading.Thread(target=execute_streaming_command)
    streaming_thread.start()

    # Your main Python file execution can continue here
    # ...

    # Optionally, wait for the streaming thread to finish
    streaming_thread.join()