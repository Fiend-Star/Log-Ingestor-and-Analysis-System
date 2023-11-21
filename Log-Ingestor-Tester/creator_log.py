import json
import random
import time
from datetime import datetime, timedelta

# Sample messages for log entries
sample_messages = [
    "Connection successful",
    "Error reading database",
    "User login successful",
    "Failed to load resource",
    "Server started",
    "Memory usage exceeded",
    "Invalid user input",
    "File uploaded successfully",
    "Timeout while connecting to service",
    "Data processed successfully"
]

# Function to select log level based on message
def select_log_level(message):
    if "successful" in message or "started" in message:
        return "INFO"
    elif "exceeded" in message or "Invalid" in message or "Error" in message:
        return "WARNING"
    else:
        return "ERROR"

# Function to generate a random hexadecimal string
def random_hex(length=6):
    return ''.join(random.choices('0123456789abcdef', k=length))

# Function to generate a timestamp within the last 30 days
def timestamp_within_30_days():
    random_days_ago = random.randint(0, 30)
    random_date = datetime.now() - timedelta(days=random_days_ago)
    return random_date.strftime("%Y-%m-%d %H:%M:%S")

# Function to generate a single log entry
def generate_log_entry():
    message = random.choice(sample_messages)
    level = select_log_level(message)
    trace_id = f"{random_hex(3)}-{random_hex(3)}-{random_hex(3)}"
    commit = random_hex()
    span_id = f"span-{random.randint(100, 999)}"
    timestamp = timestamp_within_30_days()
    resource_id = f'server-{random.randint(1000, 9999)}'
    metadata = {
        'ipAddress': f"{random.randint(100, 255)}.{random.randint(50, 255)}.{random.randint(10, 255)}.{random.randint(1, 255)}",
        'userId': f"user-{random.randint(1000, 9999)}",
        'parentResourceId': f'server-{random.randint(1000, 9999)}'
    }

    return {
        'traceId': trace_id,
        'spanId': span_id,
        'timestamp': timestamp,
        'level': level,
        'message': message,
        'resourceId': resource_id,
        'commit': commit,
        'metadata': metadata
    }

# Generating 300,000 log entries
log_entries = [generate_log_entry() for _ in range(300000)]

# Saving to a JSON file
json_file_path = '/mnt/data/log_entries.json'
with open(json_file_path, 'w') as file:
    json.dump(log_entries, file, indent=4)

json_file_path
