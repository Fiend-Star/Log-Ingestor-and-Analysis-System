#!/bin/bash

# Define the function to send requests
send_requests() {
    local rate=$1
    local duration=$2
    local endpoint="http://localhost:3000"
    local payload='E:\Professional\hiring-Fiend-Star-666\Log-Ingestor-Tester\log_entries.json'  # Specify the path to your JSON payload
    local total_records=$(jq length $payload)  # Get the total number of records in the JSON file

    echo "Sending $rate requests per second for $duration seconds to $endpoint"

    # Loop for the specified duration
    for ((i=0; i<duration; i++)); do
        # Loop to send requests at the specified rate
        for ((j=0; j<rate; j++)); do
            # Select a random record from the JSON file
            local random_record=$((RANDOM % total_records))
            local selected_payload=$(jq .[$random_record] $payload)

            curl -X POST -H "Content-Type: application/json" -d "$selected_payload" $endpoint &
        done
        sleep 1
    done
    wait
}

# Send requests at varying levels
send_requests 100 10    # 100 requests per second for 10 seconds
send_requests 1000 10   # 1000 requests per second for 10 seconds
send_requests 2000 10   # 2000 requests per second for 10 seconds
send_requests 4000 10   # 4000 requests per second for 10 seconds
send_requests 10000 10  # 10000 requests per second for 10 seconds

echo "All requests sent."
