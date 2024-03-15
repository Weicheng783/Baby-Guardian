<?php
// Database connection parameters
$host = "localhost";
$username = "";
$password = "";
$database = "baby";

// Establish a database connection
$conn = new mysqli($host, $username, $password, $database);

// Check the connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Function to encrypt the password (use a strong hashing algorithm)
function encryptPassword($password) {
    return password_hash($password, PASSWORD_BCRYPT);
}

// Function to check if a user exists in the database
function userExists($conn, $username) {
    $stmt = $conn->prepare("SELECT userpassword FROM guardian_user WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->bind_result($hashedPassword);
    $stmt->fetch();
    $stmt->close();

    return $hashedPassword !== null;
}

// Function to register a new user
function registerUser($conn, $username, $encryptedPassword) {
    $stmt = $conn->prepare("INSERT INTO guardian_user (username, userpassword) VALUES (?, ?)");
    $stmt->bind_param("ss", $username, $encryptedPassword);
    $stmt->execute();
    $stmt->close();
}

// Handle login request
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Get data from the request
    $username = $_POST['username'];
    $password = $_POST['password'];

    // Check if the user exists in the database
    if (userExists($conn, $username)) {
        // User exists, check the password
        $stmt = $conn->prepare("SELECT userpassword FROM guardian_user WHERE username = ?");
        $stmt->bind_param("s", $username);
        $stmt->execute();
        $stmt->bind_result($hashedPassword);
        $stmt->fetch();
        $stmt->close();

        if (password_verify($password, $hashedPassword)) {
            // Passwords match, send a success response
            $response = array('success' => true, 'message' => 'Login successful');
            echo json_encode($response);
        } else {
            // Passwords do not match, send an error response
            $response = array('success' => false, 'message' => 'Incorrect password');
            echo json_encode($response);
        }
    } else {
        // User does not exist, register the user and send a success response
        $encryptedPassword = encryptPassword($password);
        registerUser($conn, $username, $encryptedPassword);
        $response = array('success' => true, 'message' => 'User registered and login successful');
        echo json_encode($response);
    }
} else {
    // Invalid request method
    $response = array('success' => false, 'message' => 'Invalid request method');
    echo json_encode($response);
}

// Close the database connection
$conn->close();
?>