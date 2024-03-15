#!/bin/bash

while true; do
    arecord -D $(arecord -l | grep -oP 'card [0-9]+:.*?USB PnP Sound Device' | awk '{gsub(/:/, ""); print "plughw:" $2}'),$(arecord -l | grep -oP 'device [0-9]+:.*?USB Audio \[USB Audio\]' | awk '{gsub(/:/, ""); print $2}' | tr ':' ',') -f cd -t wav -d 999999999 | \
    ffmpeg -f wav -i - -af lowpass=3000,highpass=200,afftdn=nf=-25 -acodec aac -strict experimental -f flv rtmp://localhost/live/sound

    pids=$(fuser -v /dev/snd/* 2>/dev/null | awk '{print $NF}' | tr '\n' ' ')
    if [ -n "$pids" ]; then
        echo "Killing processes using audio devices..."
        for pid in $pids; do
            echo "Killing PID: $pid"
            kill -9 "$pid"
        done
        echo "Processes killed."
    else
        echo "No processes found using audio devices."
    fi

    sleep 2
    pids=$(fuser -v /dev/snd/* 2>/dev/null | awk '{print $NF}' | tr '\n' ' ')
    if [ -n "$pids" ]; then
        echo "Killing processes using audio devices..."
        for pid in $pids; do
            echo "Killing PID: $pid"
            kill -9 "$pid"
        done
        echo "Processes killed."
    else
        echo "No processes found using audio devices."
    fi
done
