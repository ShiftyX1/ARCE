@echo off
echo ===========================================
echo      ARCE Engine Build Script
echo ===========================================
echo.

echo Очистка предыдущих сборок...
mvn clean

echo.
echo Компиляция и создание JAR файлов...
mvn package

echo.
echo ===========================================
if exist "target\arce-game.jar" (
    echo ✓ Игра собрана: target\arce-game.jar
) else (
    echo ✗ Ошибка сборки игры
)

if exist "target\arce-editor.jar" (
    echo ✓ Редактор собран: target\arce-editor.jar
) else (
    echo ✗ Ошибка сборки редактора
)
echo ===========================================

echo.
echo Использование:
echo   java -jar target\arce-game.jar    - запуск игры
echo   java -jar target\arce-editor.jar  - запуск редактора
echo.

pause 