#!/bin/bash
set -e
SRC="src/lab3/server/WordServer.java src/lab3/server/WordServlet.java src/lab3/server/DictionaryServlet.java src/lab3/server/FlightServlet.java"
JSOUP="lib/jsoup-1.24.1.jar"
if [ ! -s "$JSOUP" ]; then
    download=$(mktemp "${JSOUP}.XXXXXX")
    trap 'rm -f "$download"' EXIT
    curl -fLsS --max-time 60 \
        https://repo.maven.apache.org/maven2/org/jsoup/jsoup/1.24.1/jsoup-1.24.1.jar \
        -o "$download"
    mv "$download" "$JSOUP"
    trap - EXIT
fi
CP="lib/sqlite-jdbc.jar:$JSOUP"
javac -encoding UTF-8 -cp "$CP" -d out $SRC
echo "--- Компиляция успешна ---"
exec java -cp "out:$CP" lab3.server.WordServer
