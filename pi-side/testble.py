from bluepy.btle import Peripheral, BTLEException, DefaultDelegate

class MyDelegate(DefaultDelegate):
    def __init__(self):
        DefaultDelegate.__init__(self)

    def handleNotification(self, cHandle, data):
        print(f"Received data on handle {cHandle}: {data.decode()}")

def start_ble_server():
    # Define the UUIDs for services and characteristics
    file_transfer_service_uuid = "00001805-0000-1000-8000-00805f9b34fb"
    text_transfer_service_uuid = "00001806-0000-1000-8000-00805f9b34fb"
    image_transfer_service_uuid = "00001807-0000-1000-8000-00805f9b34fb"

    file_transfer_characteristic_uuid = "00002a2b-0000-1000-8000-00805f9b34fb"
    text_transfer_characteristic_uuid = "00002a2c-0000-1000-8000-00805f9b34fb"
    image_transfer_characteristic_uuid = "00002a2d-0000-1000-8000-00805f9b34fb"

    # Create Peripheral and set delegate
    try:
        p = Peripheral()
        p.connect('BC:7F:A4:04:72:06')  # Replace with your actual device address
        p.setDelegate(MyDelegate())

        # Discover services and characteristics
        file_transfer_service = None
        text_transfer_service = None
        image_transfer_service = None

        for service in p.getServices():
            if service.uuid == file_transfer_service_uuid:
                file_transfer_service = service
            elif service.uuid == text_transfer_service_uuid:
                text_transfer_service = service
            elif service.uuid == image_transfer_service_uuid:
                image_transfer_service = service

        if file_transfer_service and text_transfer_service and image_transfer_service:
            file_transfer_char = file_transfer_service.getCharacteristics(uuid=file_transfer_characteristic_uuid)[0]
            text_transfer_char = text_transfer_service.getCharacteristics(uuid=text_transfer_characteristic_uuid)[0]
            image_transfer_char = image_transfer_service.getCharacteristics(uuid=image_transfer_characteristic_uuid)[0]

            while True:
                if p.waitForNotifications(1.0):
                    continue

    except BTLEException as e:
        print(f"Error: {e}")

    finally:
        p.disconnect()

if __name__ == "__main__":
    start_ble_server()
