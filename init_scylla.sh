#!/bin/bash

echo 1048576 > /proc/sys/fs/aio-max-nr

scylla_io_setup

/usr/bin/scylla --developer-mode=1 &  # Start Scylla in the background

# Wait for ScyllaDB to be ready
until cqlsh -e "describe cluster"
do
    echo "Waiting for ScyllaDB to be ready..."
    sleep 10
done

echo "ScyllaDB is now ready."

until cqlsh -e "describe cluster"
do
    echo "Waiting for ScyllaDB to be ready..."
    sleep 10
done

# Create keyspace
cqlsh -e "CREATE KEYSPACE IF NOT EXISTS logKeySpace WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy','DC1' : 3};"

tail -f /dev/null
#--smp 2 --memory 4G
#--overprovisioned 1
#--developer-mode=1
