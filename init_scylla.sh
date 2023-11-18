#!/bin/bash

echo 1048576 > /proc/sys/fs/aio-max-nr

scylla_io_setup

/usr/bin/scylla --developer-mode=1

until cqlsh -e "describe cluster"
do
    echo "Waiting for ScyllaDB to be ready..."
    sleep 10
done

# Create keyspace
cqlsh -e "CREATE KEYSPACE IF NOT EXISTS mykeyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 3};"

 #--smp 2 --memory 4G
#--overprovisioned 1
#--developer-mode=1
