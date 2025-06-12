#!/bin/sh

cd $(dirname "$0")

. ./env.sh

echo "Starting backend..."
/usr/bin/java -Dlogging.file.name=logs/app.log -jar markusbro-admin.jar
