$body = @{
    client_id = "vetclinic-app"
    client_secret = "vetclinic-secret"
    username = "admin"
    password = "admin"
    grant_type = "password"
}
$tokenResponse = Invoke-RestMethod -Uri "http://localhost:8180/realms/vetclinic/protocol/openid-connect/token" -Method Post -Body $body
$token = $tokenResponse.access_token
$headers = @{ Authorization = "Bearer $token" }

Write-Host "=== Testing Invoices Endpoint ===" -ForegroundColor Green
try {
    $invoices = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/invoices" -Headers $headers
    Write-Host "Found $($invoices.Count) invoices"
    if ($invoices.Count -gt 0) {
        $grouped = $invoices | Group-Object -Property status
        Write-Host "  Status distribution:"
        $grouped | ForEach-Object {
            Write-Host "    - $($_.Name): $($_.Count)"
        }
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Testing Certificates Endpoint ===" -ForegroundColor Green
try {
    $certs = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/certificates" -Headers $headers
    Write-Host "Found $($certs.Count) certificates"
    if ($certs.Count -gt 0) {
        $certs | Select-Object -First 3 | ForEach-Object {
            Write-Host "  - $($_.certificateNumber): $($_.patientName) - $($_.vaccineName)"
        }
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Testing GDPR Consents Endpoint ===" -ForegroundColor Green
try {
    $consents = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/consents" -Headers $headers
    Write-Host "Found $($consents.Count) consents"
    if ($consents.Count -gt 0) {
        $grouped = $consents | Group-Object -Property status
        Write-Host "  Status distribution:"
        $grouped | ForEach-Object {
            Write-Host "    - $($_.Name): $($_.Count)"
        }
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Testing Clients Endpoint ===" -ForegroundColor Green
try {
    $clients = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/clients" -Headers $headers
    Write-Host "Found $($clients.Count) clients"
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Testing Patients Endpoint ===" -ForegroundColor Green
try {
    $patients = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/patients" -Headers $headers
    Write-Host "Found $($patients.Count) patients"
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Testing Visits Endpoint ===" -ForegroundColor Green
try {
    $visits = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/visits" -Headers $headers
    Write-Host "Found $($visits.Count) visits"
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
