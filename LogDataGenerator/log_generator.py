import datetime
import random

# Sample messages for log generation
sample_messages = [
    "User login successful",
    "Error: Database connection failed",
    "Data export initiated",
    "Warning: Memory usage high",
    "System shutdown scheduled",
    "User logout",
    "New user registered",
    "File upload completed",
    "System update available",
    "Backup process started"
]

# Function to select log level based on message
def select_log_level(message):
    if "Error" in message:
        return "ERROR"
    elif "Warning" in message:
        return "WARNING"
    else:
        return "INFO"

# Function to generate random hexadecimal strings
def random_hex(length=4):
    return ''.join(random.choices('0123456789abcdef', k=length))

# Function to generate realistic metadata
def realistic_metadata():
    ip = f"{random.randint(100, 255)}.{random.randint(50, 255)}.{random.randint(10, 255)}.{random.randint(1, 255)}"
    user_id = f"user-{random.randint(1000, 9999)}"
    return f"{{'ipAddress': '{ip}', 'userId': '{user_id}', 'parentResourceId': 'server-{random.randint(1000, 9999)}'}}"

# Function to generate a timestamp within the last 30 days, formatted for Cassandra
def timestamp_within_30_days():
    now = datetime.datetime.now()
    thirty_days_ago = now - datetime.timedelta(days=30)
    random_timestamp = thirty_days_ago + datetime.timedelta(seconds=random.randint(0, 30 * 24 * 3600))
    return random_timestamp.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3] + 'Z'

# Function to write a batch of insert statements to the file
def write_batch(file, batch):
    file.write("BEGIN UNLOGGED BATCH\n")
    for line in batch:
        file.write(line + '\n')
    file.write("APPLY BATCH;\n")

# Generating and saving the log entries
file_path = '/mnt/data/updated_realistic_log_entries.txt'
with open(file_path, 'w') as file:
    batch_size = 500  # Batch size
    batch = []
    trace_id = ""
    for i in range(400000):  # Generating 400,000 records
        message = random.choice(sample_messages)
        level = select_log_level(message)
        trace_id = f"{random_hex(3)}-{random_hex(3)}-{random_hex(3)}" if i % 3 == 0 else trace_id
        commit = random_hex()
        span_id = f"span-{random.randint(100, 999)}"
        timestamp = timestamp_within_30_days()
        resource_id = f'server-{random.randint(1000, 9999)}'
        metadata = realistic_metadata()

        line = f"INSERT INTO logKeySpace.logs (traceId, spanId, timestamp, level, message, resourceId, commit, metadata) VALUES ('{trace_id}', '{span_id}', '{timestamp}', '{level}', '{message}', '{resource_id}', '{commit}', {metadata});"
        batch.append(line)
        if len(batch) >= batch_size:
            write_batch(file, batch)
            batch.clear()

    # Write any remaining records
    if batch:
        write_batch(file, batch)

file_path

