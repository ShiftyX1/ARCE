#!/bin/bash

echo "==========================================="
echo "      ARCE Engine Build Script"
echo "==========================================="
echo

echo "Очистка предыдущих сборок..."
mvn clean

echo
echo "Компиляция и создание JAR файлов..."
mvn package

echo
echo "==========================================="
if [ -f "target/arce-game.jar" ]; then
    echo "✓ Игра собрана: target/arce-game.jar"
else
    echo "✗ Ошибка сборки игры"
fi

if [ -f "target/arce-editor.jar" ]; then
    echo "✓ Редактор собран: target/arce-editor.jar"
else
    echo "✗ Ошибка сборки редактора"
fi
echo "==========================================="

echo
echo "Использование:"
echo "  java -jar target/arce-game.jar    - запуск игры"
echo "  java -jar target/arce-editor.jar  - запуск редактора"
echo 