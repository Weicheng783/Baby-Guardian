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
    $modelDescription = sanitizeInput($_POST['model_description']);
    $ownerStatus = sanitizeInput($_POST['owner_status']); // Add this line
    
    // Check if user exists in guardian_user
    $checkUserQuery = "SELECT * FROM guardian_user WHERE username = '$username' LIMIT 1";
    $userResult = $conn->query($checkUserQuery);
    
    if ($userResult->num_rows === 0) {
        // User does not exist
        echo "User does not exist. Please check your credentials.";
    } else {
        // User exists, fetch information for the given serial number
        $fetchSerialInfoQuery = "SELECT * FROM device WHERE serial_number = '$serialNumber' LIMIT 1";
        $serialInfoResult = $conn->query($fetchSerialInfoQuery);

        if ($serialInfoResult->num_rows > 0) {
            // Serial number already exists
            $row = $serialInfoResult->fetch_assoc();
            
            if (!empty($row['registered_user_name'])) {
                // Serial number registered to another user
                if ($ownerStatus === "friend") {
                    // Use INSERT ... ON DUPLICATE KEY UPDATE to handle duplicates more efficiently
                    $copyEntryQuery = "INSERT INTO device (serial_number, model_description, registered_user_name, owner_status, activation_date) 
                                       SELECT '$serialNumber', model_description, '$username', 'friend', activation_date 
                                       FROM device 
                                       WHERE serial_number = '$serialNumber' 
                                       ON DUPLICATE KEY UPDATE owner_status = VALUES(owner_status)";
                    
                    $conn->query($copyEntryQuery);

                    // Check if the query affected any rows (i.e., if the serial number was copied or updated)
                    if ($conn->affected_rows > 0) {
                        echo "Serial number copied/updated for friend.";
                    } else {
                        echo "Serial number already registered to the friend.";
                    }
                } else {
                    // Serial number already registered to another user as owner
                    echo "Serial number already registered to another user as owner. Registration rejected.";
                }
            } else {
                // Update the registered_user_name and owner_status if owner_status is not filled
                $updateUserQuery = "UPDATE device SET registered_user_name = '$username', owner_status = '$ownerStatus' WHERE serial_number = '$serialNumber'";
                $conn->query($updateUserQuery);
                echo "Serial number updated with the registered user and owner_status.";
            }
        } else {
            // Serial number does not exist, insert a new entry
            $insertQuery = "INSERT INTO device (serial_number, model_description, registered_user_name, owner_status, activation_date) 
                            VALUES ('$serialNumber', '$modelDescription', '$username', 'owner', NOW())";
            $conn->query($insertQuery);
            echo "Serial number registered successfully with owner_status set to 'owner'.";
        }
    }
    
} else {
    // Handle if POST data is not set
    echo "Invalid request.";
}

// Close connection
$conn->close();
?>