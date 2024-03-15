<?php

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    
    // Retrieve the device serial number from the POST request
    $serialNumber = $_POST['serial_number'];
    
    // Validate the serial number (you might want to add more validation logic)
    if (empty($serialNumber)) {
        die('Invalid serial number');
    }
    
    // Create the target folder if it doesn't exist
    $targetFolder = "/var/www/html/baby_guardian/photos/{$serialNumber}";
    if (!file_exists($targetFolder)) {
        mkdir($targetFolder, 0777, true);
    }

    // Set permissions (you might want to adjust this based on your needs)
    chmod($targetFolder, 0777);

    // Retrieve the uploaded file
    $uploadedFile = $_FILES['image'];
    
    // Validate the uploaded file (you might want to add more validation logic)
    if ($uploadedFile['error'] !== UPLOAD_ERR_OK || !is_uploaded_file($uploadedFile['tmp_name'])) {
        die('Error uploading file');
    }
    
    // Move the uploaded file to the target folder
    $targetFilePath = "{$targetFolder}/latest.jpg";
    move_uploaded_file($uploadedFile['tmp_name'], $targetFilePath);
    
    echo 'File uploaded successfully!';
    
} else {
    // If the request method is not POST, return an error
    die('Invalid request method');
}

?>