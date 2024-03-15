import matplotlib.pyplot as plt
import pandas as pd
import os
import librosa
import librosa.display
import resampy
import numpy as np
import tensorflow as tf
from sklearn.preprocessing import LabelEncoder
from tensorflow.keras.utils import to_categorical

model = tf.keras.models.load_model('babysound_classification_tf_cnn.h5')
class_labels = ["awake","diaper","hug","hungry","sleepy","uncomfortable"]
predict_classes = class_labels
le = LabelEncoder()
yy = to_categorical(le.fit_transform(class_labels))

def judgelabel(labelname):
  if labelname=='sleepy':
    return 4
  elif labelname=='hug':
    return 2
  elif labelname=='uncomfortable':
    return 5
  elif labelname=='diaper':
    return 1
  elif labelname=='awake':
    return 0
  elif labelname=='hungry':
    return 3

# max_pad_len = 174
max_pad_len = 500
num_rows = 40
# num_columns = 174
num_columns = 500   
num_channels = 1

def extract_features(file_name):
    try:
        audio, sample_rate = librosa.load(file_name, res_type='kaiser_fast') 
        mfccs = librosa.feature.mfcc(y=audio, sr=sample_rate, n_mfcc=40)
        pad_width = max_pad_len - mfccs.shape[1]
        mfccs = np.pad(mfccs, pad_width=((0, 0), (0, pad_width)), mode='constant')
    except Exception as e:
        print("Error encountered while parsing file: ", e)
        # print("Error encountered while parsing file: ", file_name)
        return None 
    return mfccs

def print_prediction_sta(file_name):
    prediction_feature = extract_features(file_name) 
    prediction_feature = prediction_feature.reshape(1, num_rows, num_columns, num_channels)
    predict_x=model.predict(prediction_feature) 
    classes_x=np.argmax(predict_x,axis=1)
    predicted_class = le.inverse_transform(classes_x) 
    return class_labels[classes_x[0]]

filename = 'work/train/diaper/diaper_101_test.wav'
print(print_prediction_sta(filename))