#!/bin/bash
set -e

PG_VERSION=$(ls /etc/postgresql)
DATADIR=/var/lib/postgresql/$PG_VERSION/main

# The postgresql apt package's postinst already created this cluster
# directory (via pg_createcluster) when the image was built; it just isn't
# running yet, since there's no init system during a container build.
pg_ctlcluster "$PG_VERSION" main start

until pg_isready -h 127.0.0.1 -p 5432 -q; do
    sleep 1
done

if [ ! -f "$DATADIR/.initialized" ]; then
    echo "First boot: setting postgres password and loading schema..."
    su postgres -c "psql -c \"ALTER USER postgres WITH PASSWORD 'root';\""
    su postgres -c "psql -c 'CREATE DATABASE onlinehealthsystem;'"
    PGPASSWORD=root psql -h 127.0.0.1 -U postgres -d onlinehealthsystem -f /app/schema.sql
    touch "$DATADIR/.initialized"
fi

java -jar /app/app.war &
APP_PID=$!

trap "kill $APP_PID 2>/dev/null; pg_ctlcluster $PG_VERSION main stop" TERM INT
wait $APP_PID
