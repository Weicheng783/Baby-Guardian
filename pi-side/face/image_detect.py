import os
import cv2
import numpy as np
from keras.preprocessing import image
import warnings
from keras.preprocessing.image import load_img, img_to_array 
from keras.models import load_model
import matplotlib.pyplot as plt

warnings.filterwarnings("ignore")

def analyze_emotion(image_address):
    # load model
    model = load_model("/home/baby/Desktop/face/best_model-2.h5")

    threshold = 0.2  # human face's confidence threshold

    # 加载预训练后的ResNetSSD的caffe模型
    prototxt_file = '/home/baby/Desktop/face/Resnet_SSD_deploy.prototxt'
    caffemodel_file = '/home/baby/Desktop/face/Res10_300x300_SSD_iter_140000.caffemodel'
    net = cv2.dnn.readNetFromCaffe(prototxt_file, caffeModel=caffemodel_file)
    print('MobileNetSSD caffe model loaded successfully')

    emotions = {'crying': 0, 'happy': 1, 'quiet': 2, 'sleep': 3}
    op = dict(zip(emotions.values(), emotions.keys()))

    # Load the input image
    test_img = cv2.imread(image_address)

    origin_h, origin_w = test_img.shape[:2]

    blob = cv2.dnn.blobFromImage(cv2.resize(test_img, (300, 300)), 1.0, (300, 300), (104.0, 177.0, 123.0))
    net.setInput(blob)
    detections = net.forward()

    for i in range(detections.shape[2]):
        confidence = detections[0, 0, i, 2]
        if confidence > threshold:
            # 取出bounding box的位置值并还原到原始image中
            bounding_box = detections[0, 0, i, 3:7] * np.array([origin_w, origin_h, origin_w, origin_h])
            x_start, y_start, x_end, y_end = bounding_box.astype('int')

            cv2.rectangle(test_img, (x_start, y_start), (x_end, y_end), (0, 0, 255), 7)

            face = test_img[y_start:y_end, x_start:x_end]
            face = cv2.resize(face, (224, 224))

            i = img_to_array(face) / 255
            input_arr = np.array([i])

            index = np.argmax(model.predict(input_arr))

            # pred 可以作为输出， 是crying，happy，quiet，sleep 字符串
            pred = op[index]

            return pred
            # cv2.putText(test_img, pred, (x_start, y_start), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)

    # resized_img = cv2.resize(test_img, (1000, 700))
    # cv2.imshow('Facial emotion analysis ', resized_img)
    # cv2.waitKey(0)
    # cv2.destroyAllWindows()

# Example usage:
# image_address = ''
# print(analyze_emotion(image_address))
        
def analyze_frame_emotion():
    frame_path = "sample/0001.png"
    progress_file = "progress.txt"
    model = load_model("/home/baby/Desktop/face/best_model-2.h5")

    threshold = 0.2  # human face's confidence threshold

    prototxt_file = '/home/baby/Desktop/face/Resnet_SSD_deploy.prototxt'
    caffemodel_file = '/home/baby/Desktop/face/Res10_300x300_SSD_iter_140000.caffemodel'
    net = cv2.dnn.readNetFromCaffe(prototxt_file, caffeModel=caffemodel_file)

    emotions = {'crying': 0, 'happy': 1, 'quiet': 2, 'sleep': 3}
    op = dict(zip(emotions.values(), emotions.keys()))

    frame_number = 1

    try:
        with open(progress_file, 'r') as file:
            frame_number = int(file.read())
    except FileNotFoundError:
        pass

    while frame_number <= 20:
        frame_path = f'sample/{frame_number:04d}.png'
        if not os.path.exists(frame_path):
            print(f"Frame {frame_number} not found. Exiting.")
            break

        frame = cv2.imread(frame_path)

        origin_h, origin_w = frame.shape[:2]

        blob = cv2.dnn.blobFromImage(cv2.resize(frame, (300, 300)), 1.0, (300, 300), (104.0, 177.0, 123.0))
        net.setInput(blob)
        detections = net.forward()

        for i in range(detections.shape[2]):
            confidence = detections[0, 0, i, 2]
            if confidence > threshold:
                bounding_box = detections[0, 0, i, 3:7] * np.array([origin_w, origin_h, origin_w, origin_h])
                x_start, y_start, x_end, y_end = bounding_box.astype('int')

                face = frame[y_start:y_end, x_start:x_end]
                face = cv2.resize(face, (224, 224))

                i = img_to_array(face) / 255
                input_arr = np.array([i])

                index = np.argmax(model.predict(input_arr))
                pred = op[index]

                result_string = f"Frame {frame_number}: {pred}"

                frame_number += 1
                with open(progress_file, 'w') as file:
                    file.write(str(frame_number))

                return result_string

print(analyze_frame_emotion())