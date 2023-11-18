#!/bin/bash
echo 1048576 > /proc/sys/fs/aio-max-nr
scylla_io_setup
exec /usr/bin/scylla --developer-mode=1
 #--smp 2 --memory 4G
#--overprovisioned 1
#--developer-mode=1
