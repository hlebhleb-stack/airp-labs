#!/bin/bash
set -e
javac -d out src/lab3/client/WordClient.java src/lab3/client/DictionaryClient.java
echo "--- Компиляция успешна ---"
echo "Запуск словаря: java -cp out lab3.client.DictionaryClient"
echo "Запуск Word-клиента: java -cp out lab3.client.WordClient"
