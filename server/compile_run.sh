#!/bin/bash
set -e
SRC="src/lab3/server/WordServer.java src/lab3/server/WordServlet.java src/lab3/server/DictionaryServlet.java"
CP="lib/sqlite-jdbc.jar"
javac -cp "$CP" -d out $SRC
echo "--- Компиляция успешна ---"
java -cp "out:$CP" lab3.server.WordServer
