<?php
// Database connection details
$servername = "localhost";
$username = "";
$password = "";
$database = "baby";

// Create database connection
$conn = new mysqli($servername, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Function to handle insert mode for status
function insertStatus($device_serial, $status) {
    global $conn;

    // Check if an entry already exists for the given device_serial
    $check_stmt = $conn->prepare("SELECT COUNT(*) FROM status WHERE device_serial = ?");
    $check_stmt->bind_param("s", $device_serial);
    $check_stmt->execute();
    $check_stmt->bind_result($entry_count);
    $check_stmt->fetch();
    $check_stmt->close();

    if ($entry_count > 0) {
        // Entry already exists, redirect to updateStatus
        return updateStatus($device_serial, $status);
    } else {
        // Insert a new entry
        $insert_stmt = $conn->prepare("INSERT INTO status (device_serial, status, datetime) VALUES (?, ?, NOW())");
        $insert_stmt->bind_param("ss", $device_serial, $status);

        if ($insert_stmt->execute()) {
            return $conn->insert_id;
        } else {
            return false;
        }
    }
}

// Function to handle update mode for status based on device_serial
function updateStatus($device_serial, $status) {
    global $conn;

    $stmt = $conn->prepare("UPDATE status SET status = ?, datetime = NOW() WHERE device_serial = ?");
    $stmt->bind_param("ss", $status, $device_serial);

    if ($stmt->execute()) {
        return true;
    } else {
        return false;
    }
}

// Function to handle find mode for status based on device_serial
function findStatus($device_serial) {
    global $conn;

    $stmt = $conn->prepare("SELECT * FROM status WHERE device_serial = ?");
    $stmt->bind_param("s", $device_serial);

    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $data = $result->fetch_all(MYSQLI_ASSOC);
        return json_encode($data);
    } else {
        return false;
    }
}

// Main logic
if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $mode = $_POST["mode"];

    switch ($mode) {
        case "insert":
            $device_serial = $_POST["device_serial"];
            $status = $_POST["status"];
            $result = insertStatus($device_serial, $status);
            
            if ($result !== false) {
                echo json_encode(["id" => $result]);
            } else {
                echo "Error inserting data";
            }
            break;

        case "update":
            $device_serial = $_POST["device_serial"];
            $status = $_POST["status"];
            $result = updateStatus($device_serial, $status);

            if ($result !== false) {
                echo json_encode(["success" => true]);
            } else {
                echo "Error updating data";
            }
            break;

        case "find":
            $device_serial = $_POST["device_serial"];
            $result = findStatus($device_serial);
            
            if ($result !== false) {
                echo $result;
            } else {
                echo "No entries found for the specified device_serial";
            }
            break;

        default:
            echo "Invalid mode";
    }
}

// Close the database connection
$conn->close();
?>