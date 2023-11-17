#!/bin/sh
# wait-for-postgres.sh

set -e

host="$1"
username="$2"
password="$3"
database="$4"
shift 4
cmd="$@"

until PGPASSWORD="$password" psql -h "$host" -U "$username" -d "$database" -c '\q'; do
  >&2 echo "PostgreSQL is unavailable - sleeping"
  sleep 1
done

>&2 echo "PostgreSQL is up - executing command"
exec $cmd
