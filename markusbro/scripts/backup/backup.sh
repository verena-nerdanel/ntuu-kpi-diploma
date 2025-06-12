#!/bin/sh

DB_SCHEMA="markusbro"
DB_USERNAME="(username)"
DB_PASSWORD="(password)"

DATA_FOLDER="./data"
FILENAME=${DB_SCHEMA}_$(date +%F_%H%M%S).sql.gz

S3_BUCKET="markus-bro"
S3_FOLDER="backups/db"
S3_STORAGE_CLASS="GLACIER"

echo Dumping DB to file $DATA_FOLDER/$FILENAME
mkdir -p "$DATA_FOLDER"
/usr/bin/mysqldump -u $DB_USERNAME -p$DB_PASSWORD $DB_SCHEMA | gzip > "$DATA_FOLDER/$FILENAME"

echo Uploading to S3 $S3_BUCKET/$S3_FOLDER
aws s3api put-object --bucket "$S3_BUCKET" --storage-class "$S3_STORAGE_CLASS" --body "$DATA_FOLDER/$FILENAME" --key "$S3_FOLDER/$FILENAME"

echo Done
