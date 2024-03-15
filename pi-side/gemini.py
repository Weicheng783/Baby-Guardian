import os
import sys
import traceback
from flask import Flask, request, jsonify
import vertexai
from vertexai.preview.generative_models import GenerativeModel
import requests
import uuid
from google.cloud import storage

app = Flask(__name__)
download_folder = "/home/ubuntu/server_photos"

# Step 1: Authentication & Initial Setup for Vertex AI
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "/home/ubuntu/apipi.json"
vertexai.init(project="", location="")

# Step 2: Create Vertex AI client
model = GenerativeModel("gemini-pro")

# New mode for image generation
def generate_text(image_uri: str, query: str = "") -> str:
    # Initialize Vertex AI
    vertexai.init(project="", location="")
    # Load the model
    multimodal_model = GenerativeModel("gemini-1.0-pro-vision")
    # Query the model
    response = multimodal_model.generate_content(
        [
            # Add an example image
            vertexai.generative_models.Part.from_uri(image_uri, mime_type="image/jpeg"),
            # Add an example query
            query,
        ]
    )
    print(response)
    return response.text

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

        return upload_to_google_cloud_storage(destination_path, bucket_name, destination_blob_name)
    else:
        return None

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

    return "gs://baby_guardian_images/"+destination_blob_name

@app.route('/gemini', methods=['POST'])
def gemini_endpoint():
    try:
        # Get the input, mode, and optional image parameters from the POST request
        input_string = request.json.get('input', '')
        mode = request.json.get('mode', '')

        if input_string == "":
            return jsonify({"error": "Input string is empty"})

        # Call the Gemini function based on the mode
        if mode == "image":
            image_uri = request.json.get('image_uri', '')
            gsPath = download_file(image_uri, download_folder)
            if(gsPath):
                #return jsonify({"Down": gsPath})
                print(gsPath)
                response_text = generate_text(gsPath, input_string)
                # print(response_text)
            else:
                return jsonify({"error": "Image Fetch Not Successful"})
        elif mode == "image_then":
            image_uri = request.json.get('image_uri', '')
            print(image_uri)
            response_text = generate_text(image_uri, input_string)
        elif mode == "full":
            response_text = call_gemini_full(input_string)
        else:
            response_text = call_gemini(input_string)

        return jsonify({"response": response_text})

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)})

def call_gemini(input_string):
    prompt = f"Please provide a 50 words comment regarding the following environmental data for a 6-month-old baby bassinet: {input_string}, how might this environment impact the baby?"

    # Call the Gemini model with the constructed prompt
    response = model.generate_content(prompt)

    return response.text

def call_gemini_full(input_string):
    # Call the Gemini model with the constructed prompt
    response = model.generate_content(input_string)

    return response.text

if __name__ == "__main__":
    # Log errors to a file
    sys.stderr = open('/home/ubuntu/python_errors.log', 'w')

    # Run Flask app on port 3252
    app.run(port=3252)