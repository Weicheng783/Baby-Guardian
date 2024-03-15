<?php

// Database connection information
$servername = "localhost";
$username = "";
$password = "";
$dbname = "baby";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Function to check if a user is associated with a device
function isUserAssociated($serial_number) {
    global $conn;

    // Sanitize the input to prevent SQL injection
    $serial_number = $conn->real_escape_string($serial_number);

    // SQL query to check if a user is associated with the given serial_number
    $sql = "SELECT * FROM device WHERE serial_number = '$serial_number' AND owner_status = 'owner'";

    $result = $conn->query($sql);

    // Check if there is a user associated with the device
    if ($result->num_rows > 0) {
        return true; // User is associated
    } else {
        return false; // No user associated
    }
}

// Check if the 'serial_number' parameter is set in the POST request
if (isset($_POST['serial_number'])) {
    $serial_number = $_POST['serial_number'];

    // Call the function to check if a user is associated with the device
    $isUserAssociated = isUserAssociated($serial_number);

    // Return the result as JSON
    header('Content-Type: application/json');
    echo json_encode(array('isUserAssociated' => $isUserAssociated));
} else {
    // If 'serial_number' parameter is not set, return an error message
    echo "Error: 'serial_number' parameter is missing.";
}

// Close the database connection
$conn->close();

?>