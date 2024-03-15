import cv2
import qrcode
import re
import time

# Function to generate QR code
def generate_qr_code(data):
    qr = qrcode.QRCode(
        version=1,
        error_correction=qrcode.constants.ERROR_CORRECT_L,
        box_size=20,
        border=4,
    )
    qr.add_data(data)
    qr.make(fit=True)

    img = qr.make_image(fill_color="black", back_color="white")
    img.save("qrcode.png")

# Function to scan QR code
def scan_qr_code(camera_source):
    cap = cv2.VideoCapture(camera_source)

    if not cap.isOpened():
        print(f"Error: Could not open camera {camera_source}.")
        return

    cap.set(3, 1280)  # Set width
    cap.set(4, 720)   # Set height

    while True:
        ret, frame = cap.read()

        if not ret or frame.shape[0] <= 0 or frame.shape[1] <= 0:
            print("Error: Failed to capture frame.")
            break

        cv2.imshow('QR Code Scanner', frame)

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        resized_gray = cv2.resize(gray, None, fx=2, fy=2)

        qr_code_detector = cv2.QRCodeDetector()

        retval, decoded_info, points, straight_qrcode = qr_code_detector.detectAndDecodeMulti(resized_gray)

        if retval:
            print("QR Code Found:")
            print(decoded_info)
            break

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

# Function to parse QR code data
def parse_qr_code_data(qr_data):
    pattern = re.compile(r'(\w+)=(\w+)')
    matches = pattern.findall(qr_data)
    parsed_data = dict(matches)
    return parsed_data

if __name__ == "__main__":
    product_serial = "1"
    model_description = "the first"

    print("Scanning for QR code...")

    # Try using the Raspberry Pi camera module
    scan_qr_code(0)

    # If unsuccessful, switch to USB camera
    scan_qr_code(1)

    print("QR code scanned. Using fixed strings:")
    print(f"Product Serial: {product_serial}")
    print(f"Model Description: {model_description}")