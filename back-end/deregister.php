<?php
// Database connection parameters
$host = 'localhost';
$user = '';
$password = '';
$database = 'baby';

// Create connection
$conn = new mysqli($host, $user, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Function to sanitize user input
function sanitizeInput($input) {
    return htmlspecialchars(stripslashes(trim($input)));
}

// Check if POST data is set
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // Sanitize and get user input
    $username = sanitizeInput($_POST['username']);
    $serialNumber = sanitizeInput($_POST['serial_number']);
    $ownerStatus = sanitizeInput($_POST['owner_status']); // New field

    // Check if user exists in guardian_user
    $checkUserQuery = "SELECT * FROM guardian_user WHERE username = '$username'";
    $userResult = $conn->query($checkUserQuery);

    if ($userResult->num_rows === 0) {
        // User does not exist
        echo "User does not exist. Please check your credentials.";
    } else {
        // User exists, handle owner_status
        if ($ownerStatus === 'friend') {
            // Delete friend entries associated with the serial numbered device
            $deleteFriendsQuery = "DELETE FROM device WHERE serial_number = '$serialNumber' AND owner_status = 'friend'";
            $conn->query($deleteFriendsQuery);
        } elseif ($ownerStatus === '') {
            // Delete all friend entries and update username field to null
            $deleteFriendsQuery = "DELETE FROM device WHERE serial_number = '$serialNumber'";
            $conn->query($deleteFriendsQuery);

            // Update the username field to null
            $updateUserQuery = "UPDATE device SET registered_user_name = NULL WHERE serial_number = '$serialNumber' AND registered_user_name = '$username'";
            $result = $conn->query($updateUserQuery);

            if ($result === TRUE) {
                echo "Device removed from the user's account successfully.";
            } else {
                echo "Failed to remove device. Please check the serial number and try again.";
            }
        } else {
            echo "Invalid owner status.";
        }
    }

} else {
    // Handle if POST data is not set
    echo "Invalid request.";
}

// Close connection
$conn->close();
?>