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
    
    // Check if user exists in guardian_user
    $checkUserQuery = "SELECT * FROM guardian_user WHERE username = '$username'";
    $userResult = $conn->query($checkUserQuery);
    
    if ($userResult->num_rows === 0) {
        // User does not exist
        echo json_encode(["error" => "User does not exist. Please check your credentials."]);
    } else {
        // User exists, fetch the device list
        $deviceListQuery = "SELECT serial_number, model_description, activation_date, owner_status FROM device WHERE registered_user_name = '$username'";
        $result = $conn->query($deviceListQuery);
        
        if ($result->num_rows > 0) {
            // Prepare JSON response
            $deviceList = [];
            while ($row = $result->fetch_assoc()) {
                $deviceList[] = [
                    "serial_number" => $row['serial_number'],
                    "model_description" => $row['model_description'],
                    "activation_date" => $row['activation_date'],
                    "owner_status" => $row['owner_status']
                ];
            }
            echo json_encode($deviceList);
        } else {
            // No devices found for the user
            echo json_encode(["message" => "No devices found for the user."]);
        }
    }
    
} else {
    // Handle if POST data is not set
    echo json_encode(["error" => "Invalid request."]);
}

// Close connection
$conn->close();
?>