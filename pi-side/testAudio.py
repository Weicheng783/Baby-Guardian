import sounddevice as sd
import numpy as np
import matplotlib.pyplot as plt

def record_and_plot(duration=5, samplerate=44100):
    # Record audio
    audio_data = sd.rec(int(duration * samplerate), samplerate=samplerate, channels=1, dtype='float32')
    sd.wait()

    # Plot the waveform
    time_axis = np.arange(0, duration, 1/samplerate)
    plt.plot(time_axis, audio_data[:, 0])
    plt.title('Recorded Audio Waveform')
    plt.xlabel('Time (s)')
    plt.ylabel('Amplitude')
    plt.show()

if __name__ == "__main__":
    record_and_plot()