#!/bin/bash
set -e
javac -d out src/lab3/client/WordClient.java
echo "--- Компиляция успешна ---"
java -cp out lab3.client.WordClient
