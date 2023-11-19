#!/bin/bash

# This script is executed when the container is started.
chmod +x /init_scylla.sh

# Set AIO max number of events
echo 2048576 > /proc/sys/fs/aio-max-nr

# Run Scylla I/O setup
scylla_io_setup

# Start Scylla in the background with additional options if needed
/docker-entrypoint.py --overprovisioned 1 --smp 1 --developer-mode=1 &

#/usr/bin/scylla --developer-mode=1 &

# Wait for ScyllaDB to be ready
until cqlsh -e "describe cluster"
do
    echo "Waiting for ScyllaDB to be ready..."
    sleep 10
done

echo "ScyllaDB is now ready."

#cqlsh -e "CREATE ROLE IF NOT EXISTS cassandra WITH PASSWORD = 'cassandra' AND LOGIN = TRUE;"
#
#cqlsh -e "ALTER ROLE cassandra WITH SUPERUSER = true;"

# Create keyspace
cqlsh -e "CREATE KEYSPACE IF NOT EXISTS logKeySpace WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 1 };"

# Create table
cqlsh -e "CREATE TABLE IF NOT EXISTS logKeySpace.logs (
    traceId text,
    spanId text,
    timestamp timestamp,
    level text,
    message text,
    resourceId text,
    commit text,
    metadata map<text, text>,
    PRIMARY KEY ((traceId, spanId), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);"

# Insert Sample Data
cqlsh -e "INSERT INTO logKeySpace.logs (traceId, spanId, timestamp, level, message, resourceId, commit, metadata) VALUES ('abc-xyz-123', 'span-456', '2023-09-15T08:00:00Z', 'error', 'Failed to connect to DB', 'server-1234', '5e5342f', {'parentResourceId': 'server-0987'});"

# Create Indexes
cqlsh -e "CREATE INDEX IF NOT EXISTS idx_level ON logKeySpace.logs (level);"
cqlsh -e "CREATE INDEX IF NOT EXISTS idx_resource_id ON logKeySpace.logs (resource_id);"
cqlsh -e "CREATE INDEX IF NOT EXISTS idx_commit ON logKeySpace.logs (commit);"
cqlsh -e "CREATE INDEX IF NOT EXISTS idx_parent_resource_id ON logKeySpace.logs (parent_resource_id);"


# Create Materialized View
cqlsh -e "CREATE MATERIALIZED VIEW IF NOT EXISTS logKeySpace.logs_by_timestamp AS SELECT * FROM logKeySpace.logs WHERE timestamp IS NOT NULL AND traceId IS NOT NULL AND spanId IS NOT NULL PRIMARY KEY (timestamp, traceId, spanId) WITH CLUSTERING ORDER BY (traceId ASC, spanId ASC);"

# add 100,000 rows worth of logs
cqlsh -f /cql_log_data.cql

# Set consistency level to QUORUM
cqlsh -e "CONSISTENCY ONE"

# Keep the container running
tail -f /dev/null
