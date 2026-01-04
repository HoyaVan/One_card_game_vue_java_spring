# Stop any process using port 8080
Write-Host "Checking for processes on port 8080..."
$processes = netstat -ano | findstr :8080 | ForEach-Object {
    $parts = $_ -split '\s+'
    if ($parts.Length -gt 0) {
        $parts[-1]
    }
} | Select-Object -Unique

if ($processes) {
    Write-Host "Stopping processes on port 8080..."
    foreach ($pid in $processes) {
        if ($pid -match '^\d+$') {
            try {
                taskkill /F /PID $pid 2>$null
                Write-Host "Stopped process $pid"
            } catch {
                Write-Host "Could not stop process $pid"
            }
        }
    }
    Start-Sleep -Seconds 2
}

# Start the backend
Write-Host "Starting backend server..."
cmd /c mvnw.cmd spring-boot:run

