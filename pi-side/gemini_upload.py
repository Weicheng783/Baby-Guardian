import requests
import uuid
import os
from google.cloud import storage

def download_file(url, destination_folder):
    # Generate a random 32-bit string
    random_filename = str(uuid.uuid4().hex[:8])

    # Get the file extension from the URL
    file_extension = url.split('.')[-1]

    # Create the full destination path
    destination_path = os.path.join(destination_folder, f"{random_filename}.{file_extension}")

    # Make the HTTP request and download the file
    response = requests.get(url)
    if response.status_code == 200:
        with open(destination_path, 'wb') as file:
            file.write(response.content)
        print(f"File downloaded successfully: {destination_path}")
        bucket_name = "baby_guardian_images"
        destination_blob_name = "server_photos/"+random_filename+"."+file_extension

        upload_to_google_cloud_storage(destination_path, bucket_name, destination_blob_name)
    else:
        print(f"Failed to download file. Status code: {response.status_code}")

def upload_to_google_cloud_storage(local_file_path, bucket_name, destination_blob_name):
    """Uploads a file to Google Cloud Storage.

    Args:
        local_file_path (str): The path to the local file to be uploaded.
        bucket_name (str): The name of your Google Cloud Storage bucket.
        destination_blob_name (str): The name to give the uploaded file in the bucket.
    """
    client = storage.Client(project="baby-guardian")

    # Get the bucket
    bucket = client.get_bucket(bucket_name)

    # Create a blob with the desired destination name
    blob = bucket.blob(destination_blob_name)

    # Upload the local file to the bucket
    blob.upload_from_filename(local_file_path)

    print(f"File uploaded to Google Cloud Storage: {destination_blob_name}")

# Example usage
url_to_download = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png"
download_folder = "/home/ubuntu/server_photos"

download_file(url_to_download, download_folder)