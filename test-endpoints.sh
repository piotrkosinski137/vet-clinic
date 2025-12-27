#!/bin/bash
# API Endpoint Test Script for VetClinic

TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI1cW0taGhsNGNTajJZbVdGMmRoLWpMVDBHM2tfRW9PN2Z2QVFfN216eThFIn0.eyJleHAiOjE3Njk0NDYzNjEsImlhdCI6MTc2Njg1NDM2MSwianRpIjoiMDQyN2FjMzctZjJjNi00YWMyLTk3MWUtNjBmM2IzYjlmMjNkIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MTgwL3JlYWxtcy92ZXRjbGluaWMiLCJzdWIiOiIyZDk0YmIwMy1lMTQ2LTQ2NWEtOTQ4OS1lNWEzOTRkZjhiNDgiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ2ZXRjbGluaWMtYXBwIiwic2Vzc2lvbl9zdGF0ZSI6IjAxZGM0NWRjLWFlZTEtNGY4Ni05NzgzLTJmOGM2MWIzNjQ3ZSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDgwIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ2ZXQiLCJhY2NvdW50YW50IiwicmVjZXB0aW9uaXN0IiwiYWRtaW4iLCJ1c2VyIl19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiIwMWRjNDVkYy1hZWUxLTRmODYtOTc4My0yZjhjNjFiMzY0N2UiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IkFkbWluIFVzZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbiIsImdpdmVuX25hbWUiOiJBZG1pbiIsImZhbWlseV9uYW1lIjoiVXNlciIsImNsaW5pY19pZCI6IjAwMDAwMDAwLTAwMDAtMDAwMC0wMDAwLTAwMDAwMDAwMDAwMSIsImVtYWlsIjoiYWRtaW5AdmV0Y2xpbmljLmNvbSJ9.XMuG3UdUxxUIDSeSXgO0Fn3NwwMPczLiTs8Kjm73fkskXOof2Hw67jwA-yohlOUqDtBBaeofKLtRTbZTagXbYogWZx_ij2l4SGP41ofdPaXPqTdTpSTT9puVj8MFDJc2K8wqPsOZQ-0iaXoMXhB9CjmLSsqI9cbRxXJ3mkkVjWKglq8hs5gujGvgiQdy5PVRhiQW0ATsMyct5rcCgDqdeZmvP6oI_JK47cxXqXOUmCOgcRpeGL6nemAIcI4AJZ6vO4lFlWoWtUBeOhHuCZxH7vAUd8PebPk3-gx9ppCDVQLrfqTkLJdzz4DZrYpAaSXoHUWIaO8PRQyHcQbS0mY6tw"
BASE="http://localhost:8080/api/v1"

PASS=0
FAIL=0
RESULTS=""

test_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected=$4
    local desc=$5

    if [ -z "$data" ]; then
        resp=$(curl -s -w "HTTP_CODE:%{http_code}" -X $method "$BASE$endpoint" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json")
    else
        resp=$(curl -s -w "HTTP_CODE:%{http_code}" -X $method "$BASE$endpoint" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$data")
    fi

    code=$(echo "$resp" | grep -o 'HTTP_CODE:[0-9]*' | cut -d: -f2)
    body=$(echo "$resp" | sed 's/HTTP_CODE:[0-9]*$//')

    if [[ "$code" == "$expected" ]]; then
        echo "[PASS] $method $endpoint ($code) - $desc"
        PASS=$((PASS+1))
    else
        echo "[FAIL] $method $endpoint (got $code, expected $expected) - $desc"
        echo "       Body: $(echo "$body" | head -c 150)"
        FAIL=$((FAIL+1))
    fi
}

echo "=============================================="
echo "       VetClinic API Endpoint Tests"
echo "=============================================="

# ==================== PATIENTS ====================
echo ""
echo "--- PATIENTS API ---"
test_api "GET" "/patients" "" "200" "List patients"

# Get a patient ID
PATIENTS=$(curl -s "$BASE/patients" -H "Authorization: Bearer $TOKEN")
PID=$(echo "$PATIENTS" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "    Found patient ID: $PID"

if [ -n "$PID" ]; then
    test_api "GET" "/patients/$PID" "" "200" "Get patient by ID"
fi

test_api "POST" "/patients" '{"name":"TestPet","species":"DOG","breed":"Beagle"}' "201" "Create patient"
test_api "GET" "/patients/search?name=Test" "" "200" "Search patients"

# ==================== CLIENTS ====================
echo ""
echo "--- CLIENTS API ---"
test_api "GET" "/clients" "" "200" "List clients"

CLIENTS=$(curl -s "$BASE/clients" -H "Authorization: Bearer $TOKEN")
CID=$(echo "$CLIENTS" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "    Found client ID: $CID"

if [ -n "$CID" ]; then
    test_api "GET" "/clients/$CID" "" "200" "Get client by ID"
    test_api "GET" "/clients/$CID/debt" "" "200" "Get client debt"
fi

test_api "POST" "/clients" '{"firstName":"Test","lastName":"User","email":"testuser@test.com"}' "201" "Create client"
test_api "GET" "/clients/search?lastName=Test" "" "200" "Search clients"

# ==================== VISITS ====================
echo ""
echo "--- VISITS API ---"
test_api "GET" "/visits" "" "200" "List visits"

VISITS=$(curl -s "$BASE/visits" -H "Authorization: Bearer $TOKEN")
VID=$(echo "$VISITS" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "    Found visit ID: $VID"

if [ -n "$VID" ]; then
    test_api "GET" "/visits/$VID" "" "200" "Get visit by ID"
    test_api "PATCH" "/visits/$VID" '{"notes":"Test update"}' "200" "Update visit"
fi

if [ -n "$PID" ]; then
    test_api "GET" "/visits?patientId=$PID" "" "200" "Filter visits by patient"
    VISIT_DATE=$(date -u +"%Y-%m-%dT10:00:00Z")
    test_api "POST" "/visits" "{\"patientId\":\"$PID\",\"visitDate\":\"$VISIT_DATE\",\"status\":\"SCHEDULED\",\"visitType\":\"CONSULTATION\"}" "201" "Create visit"
fi

# ==================== VETERINARIANS ====================
echo ""
echo "--- VETERINARIANS API ---"
test_api "GET" "/veterinarians" "" "200" "List veterinarians"

VETS=$(curl -s "$BASE/veterinarians" -H "Authorization: Bearer $TOKEN")
VETID=$(echo "$VETS" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "    Found vet ID: $VETID"

if [ -n "$VETID" ]; then
    test_api "GET" "/veterinarians/$VETID" "" "200" "Get vet by ID"
fi

test_api "POST" "/veterinarians" '{"firstName":"Dr","lastName":"Test","email":"drtest@clinic.com","licenseNumber":"TEST123"}' "201" "Create veterinarian"

# ==================== PRICE LIST ====================
echo ""
echo "--- PRICE LIST API ---"
test_api "GET" "/price-list" "" "200" "List price items"
test_api "GET" "/price-list?active=true" "" "200" "List active items"
test_api "GET" "/price-list?category=MEDICATION" "" "200" "Filter by category"

PRICES=$(curl -s "$BASE/price-list" -H "Authorization: Bearer $TOKEN")
PRICEID=$(echo "$PRICES" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "    Found price item ID: $PRICEID"

if [ -n "$PRICEID" ]; then
    test_api "GET" "/price-list/$PRICEID" "" "200" "Get price item by ID"
fi

test_api "POST" "/price-list" '{"name":"Test Item","category":"SERVICE","costPrice":25.00,"sellPrice":50.00}' "201" "Create price item"

# ==================== INVOICES ====================
echo ""
echo "--- INVOICES API ---"
test_api "GET" "/invoices" "" "200" "List invoices"

INVOICES=$(curl -s "$BASE/invoices" -H "Authorization: Bearer $TOKEN")
INVID=$(echo "$INVOICES" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "    Found invoice ID: $INVID"

if [ -n "$INVID" ]; then
    test_api "GET" "/invoices/$INVID" "" "200" "Get invoice by ID"
    test_api "GET" "/invoices/$INVID/payments" "" "200" "Get invoice payments"
fi

if [ -n "$CID" ]; then
    test_api "POST" "/invoices" "{\"clientId\":\"$CID\",\"items\":[{\"name\":\"Test\",\"quantity\":1,\"unitPrice\":100,\"total\":100}]}" "201" "Create invoice"
fi

# ==================== CERTIFICATES ====================
echo ""
echo "--- CERTIFICATES API ---"
test_api "GET" "/certificates" "" "200" "List certificates"

CERTS=$(curl -s "$BASE/certificates" -H "Authorization: Bearer $TOKEN")
CERTID=$(echo "$CERTS" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "    Found certificate ID: $CERTID"

if [ -n "$CERTID" ]; then
    test_api "GET" "/certificates/$CERTID" "" "200" "Get certificate by ID"
fi

if [ -n "$PID" ] && [ -n "$CID" ]; then
    test_api "POST" "/certificates" "{\"patientId\":\"$PID\",\"patientName\":\"Test\",\"clientId\":\"$CID\",\"clientName\":\"Test Client\",\"certificateType\":\"RABIES\",\"vaccineName\":\"Rabies Vaccine\",\"veterinarianName\":\"Dr. Test\"}" "201" "Create certificate"
fi

# ==================== CONSENTS ====================
echo ""
echo "--- GDPR CONSENTS API ---"
test_api "GET" "/consents" "" "200" "List consents"

if [ -n "$CID" ]; then
    test_api "GET" "/consents?clientId=$CID" "" "200" "Get consents by client"
    test_api "POST" "/consents" "{\"clientId\":\"$CID\",\"consentType\":\"DATA_PROCESSING\",\"consentText\":\"I consent to data processing\",\"ipAddress\":\"127.0.0.1\"}" "201" "Create consent"
fi

# ==================== DASHBOARD ====================
echo ""
echo "--- DASHBOARD API ---"
test_api "GET" "/dashboard/stats" "" "200" "Dashboard stats"
test_api "GET" "/dashboard/income" "" "200" "Income stats"

# ==================== AUDIT LOGS ====================
echo ""
echo "--- AUDIT LOGS API ---"
test_api "GET" "/audit-logs" "" "200" "List audit logs"

# ==================== INVENTORY ====================
echo ""
echo "--- INVENTORY API ---"
test_api "GET" "/inventory/transactions" "" "200" "List inventory transactions"
test_api "GET" "/inventory/low-stock" "" "200" "Get low stock items"

echo ""
echo "=============================================="
echo "            TEST SUMMARY"
echo "=============================================="
echo "PASSED: $PASS"
echo "FAILED: $FAIL"
echo "TOTAL:  $((PASS+FAIL))"
echo "=============================================="
