#!/bin/bash

# Define the function to send requests
send_requests() {
    local rate=$1
    local duration=$2
    local endpoint="http://localhost:3000"
    local payload_file='E:/Professional/hiring-Fiend-Star-666/Log-Ingestor-Tester/log_entries.json'  # Fixed path format
    local total_records=$(jq length "$payload_file")
    
    echo "Sending $rate requests per second for $duration seconds to $endpoint"
    
    # Calculate how many requests to send per batch to achieve the desired rate
    # For high rates, we'll send multiple requests in smaller time intervals
    local interval=0.05  # 50ms intervals for more granular control
    local batch_size=$(echo "$rate * $interval" | bc | awk '{printf "%.0f", $1}')
    
    # Loop for the specified duration
    local end_time=$(($(date +%s) + duration))
    
    while [ $(date +%s) -lt $end_time ]; do
        local batch_start=$(date +%s.%N)
        
        # Send a batch of requests
        for ((j=0; j<batch_size; j++)); do
            # Select a random record from the JSON file
            local random_record=$((RANDOM % total_records))
            local selected_payload=$(jq -c ".[$random_record]" "$payload_file")
            
            # Use curl in background but limit concurrent processes
            (curl -s -X POST -H "Content-Type: application/json" -d "$selected_payload" "$endpoint" > /dev/null) &
            
            # If we have too many background processes, wait for some to complete
            if [ $(jobs -r | wc -l) -gt 100 ]; then
                wait -n
            fi
        done
        
        # Calculate how long the batch took and sleep if needed
        local batch_end=$(date +%s.%N)
        local elapsed=$(echo "$batch_end - $batch_start" | bc)
        local sleep_time=$(echo "$interval - $elapsed" | bc)
        
        # Only sleep if positive (we're ahead of schedule)
        if (( $(echo "$sleep_time > 0" | bc -l) )); then
            sleep $sleep_time
        fi
    done
    
    # Wait for any remaining background processes to complete
    wait
    echo "Completed sending at $rate requests per second"
}

echo "Starting load test..."

# Send requests at varying levels with pause between each level
send_requests 100 10    # 100 requests per second for 10 seconds
echo "Pausing for system recovery..."
sleep 5

send_requests 1000 10   # 1000 requests per second for 10 seconds
echo "Pausing for system recovery..."
sleep 5

send_requests 2000 10   # 2000 requests per second for 10 seconds
echo "Pausing for system recovery..."
sleep 5

send_requests 4000 10   # 4000 requests per second for 10 seconds
echo "Pausing for system recovery..."
sleep 5

send_requests 10000 10  # 10000 requests per second for 10 seconds

echo "All load tests completed."
