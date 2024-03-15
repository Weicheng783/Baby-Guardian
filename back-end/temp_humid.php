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

// Function to handle insert mode for temp_humid using stored procedure
function insertTempHumidWithLimit($device_serial, $temp, $humid) {
    global $conn;

    $stmt = $conn->prepare("CALL insertTempHumidWithLimit(?, ?, ?, @inserted_id)");
    $stmt->bind_param("sss", $device_serial, $temp, $humid);

    if ($stmt->execute()) {
        $result = $conn->query("SELECT @inserted_id as inserted_id");
        $row = $result->fetch_assoc();
        return $row["inserted_id"];
    } else {
        return false;
    }
}

// Function to handle find mode for temp_humid based on device_serial
function findTempHumid($device_serial) {
    global $conn;

    $stmt = $conn->prepare("SELECT * FROM temp_humid WHERE device_serial = ?");
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
            $temp = $_POST["temp"];
            $humid = $_POST["humid"];
            $result = insertTempHumidWithLimit($device_serial, $temp, $humid);
            echo json_encode(["id" => $result]);
            break;

        case "find":
            $device_serial = isset($_POST["device_serial"]) ? $_POST["device_serial"] : null;
            $result = findTempHumid($device_serial);
            echo $result ? $result : "Not found";
            break;

        default:
            echo "Invalid mode";
    }
}

// Close the database connection
$conn->close();
?>