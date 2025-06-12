#!/bin/sh

echo "Loading common env parameters..."

# DB
export APP_DB_URL="jdbc:mysql://localhost:3306/markusbro?serverTimezone=UTC&autoReconnect=true&failOverReadOnly=false&maxReconnects=3&useUnicode=true&characterEncoding=UTF8"
export APP_DB_USERNAME=(username)
export APP_DB_PASSWORD=(password)
export APP_DB_DRIVER=com.mysql.cj.jdbc.Driver
