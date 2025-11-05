#!/bin/bash
# Test script to verify CODA output has exactly 128 characters per line

echo "Testing CODA output format..."
echo "=============================="

# Make a test request to the API
RESPONSE=$(curl -s -X POST http://localhost:8080/api/coda/json \
  -H "Content-Type: application/json" \
  -H "Accept: text/plain" \
  -d '{
  "bankName": "BELFIUS",
  "account": "BE68 5390 0754 7034",
  "currency": "EUR",
  "date": "2025-11-03",
  "opening": 1200.00,
  "transactions": [
    {
      "bookingDate": "2025-11-03",
      "type": "CREDIT",
      "amount": 2441.20,
      "counterpartyName": "AZA BELGIUM SA",
      "counterpartyAccount": "BE84390060159859",
      "description": "Payment received",
      "reference": "+++017/5086/22021+++"
    }
  ]
}')

echo "$RESPONSE" | while IFS= read -r line; do
    len=${#line}
    echo "Line length: $len | $line"
    if [ $len -ne 128 ]; then
        echo "ERROR: Line is not 128 characters!"
    fi
done

echo ""
echo "Test complete."

