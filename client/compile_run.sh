#!/bin/bash
set -e
javac -encoding UTF-8 -d out src/lab3/client/WordClient.java src/lab3/client/DictionaryClient.java src/lab3/client/FlightClient.java
echo "--- Компиляция успешна ---"
echo "Запуск словаря: java -cp out lab3.client.DictionaryClient"
echo "Запуск Word-клиента: java -cp out lab3.client.WordClient"
if [ "${1:-}" = "flights" ]; then
    exec java -cp out lab3.client.FlightClient
fi
echo "Окно рейсов: java -cp out lab3.client.FlightClient"
