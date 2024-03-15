<?php
// Check if a file is sent through POST
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_FILES['file'])) {
    $uploadDir = '/var/www/html/baby_guardian/photos/';
    
    // Generate a random string with 36 bits
    $randomString = bin2hex(random_bytes(18)); // 18 bytes = 36 hex characters
    
    // Get the file extension
    $fileExtension = pathinfo($_FILES['file']['name'], PATHINFO_EXTENSION);
    
    // Create the destination path
    $destinationPath = $uploadDir . $randomString . '.' . $fileExtension;
    
    // Move the uploaded file to the destination path
    if (move_uploaded_file($_FILES['file']['tmp_name'], $destinationPath)) {
        // File successfully uploaded
        $combinedString = $randomString . '.' . $fileExtension;
        echo $combinedString;
    } else {
        // Failed to move the uploaded file
        http_response_code(500);
        echo "Failed to save the file.";
    }
} else {
    // No file sent or invalid request
    http_response_code(400);
    echo "Invalid request.";
}
?>