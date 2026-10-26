#!/bin/bash
set -e
SRC="src/lab3/server/WordServer.java src/lab3/server/WordServlet.java src/lab3/server/DictionaryServlet.java"
javac -d out $SRC
echo "--- Компиляция успешна ---"
java -cp out lab3.server.WordServer
