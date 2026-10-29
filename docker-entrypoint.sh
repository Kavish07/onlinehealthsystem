#!/bin/bash
set -e

DATADIR=/var/lib/mysql

if [ ! -d "$DATADIR/mysql" ]; then
    echo "Initializing MySQL data directory..."
    mysqld --initialize-insecure --datadir="$DATADIR" --user=mysql
fi

mysqld --datadir="$DATADIR" --user=mysql --bind-address=127.0.0.1 &
MYSQL_PID=$!

until mysqladmin ping -h127.0.0.1 --silent; do
    sleep 1
done

if [ ! -f "$DATADIR/.initialized" ]; then
    echo "First boot: setting root password and loading schema..."
    mysql -uroot <<-SQL
        ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';
        CREATE DATABASE IF NOT EXISTS onlinehealthsystem;
SQL
    mysql -uroot -proot onlinehealthsystem < /app/schema.sql
    touch "$DATADIR/.initialized"
fi

java -jar /app/app.war &
APP_PID=$!

trap "kill $APP_PID $MYSQL_PID 2>/dev/null" TERM INT
wait $APP_PID
