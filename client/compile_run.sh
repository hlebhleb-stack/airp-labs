#!/bin/bash
set -e
javac -encoding UTF-8 -d out src/lab3/client/WordClient.java src/lab3/client/DictionaryClient.java src/lab3/client/FlightClient.java
echo "--- Компиляция успешна ---"
echo "Запуск словаря: java -cp out lab3.client.DictionaryClient"
echo "Запуск Word-клиента: java -cp out lab3.client.WordClient"
echo "Запуск клиента рейсов: java -cp out lab3.client.FlightClient AMS 12"
