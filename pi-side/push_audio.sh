#!/bin/bash

while true; do
    ffmpeg -i rtmp://localhost/live/sound -af "volume=2" -c:a aac -f flv rtmp://weicheng.app/live/baby_guardian_audio_1

    sleep 2
done
