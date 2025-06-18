# Инструкции по сборке ARCE Engine

## Требования

- **Java 17** или выше
- **Apache Maven 3.6** или выше

## Быстрая сборка

### Windows
```cmd
build.bat
```

### Linux/macOS
```bash
chmod +x build.sh
./build.sh
```

## Ручная сборка через Maven

### 1. Сборка всех JAR файлов
```bash
mvn clean package
```

### 2. Сборка только игры
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.arce.Main"
```

### 3. Сборка только редактора
```bash
mvn clean compile  
mvn exec:java -Dexec.mainClass="com.arce.editor.LevelEditor"
```

## Результаты сборки

После успешной сборки в папке `target/` будут созданы:

- **`arce-game.jar`** - Исполняемый JAR файл игры
- **`arce-editor.jar`** - Исполняемый JAR файл редактора уровней

## Запуск

### Игра
```bash
java -jar target/arce-game.jar
```

### Редактор уровней
```bash
java -jar target/arce-editor.jar
```

## Профили Maven

В проекте настроены следующие профили:

- **game** - Запуск игры: `mvn exec:java -Pgame`
- **editor** - Запуск редактора: `mvn exec:java -Peditor`
- **demo** - Демо режим: `mvn exec:java -Pdemo`
- **console** - Консольная демка: `mvn exec:java -Pconsole`

## Структура ресурсов

Убедитесь, что следующие папки находятся в том же каталоге, что и JAR файлы:

```
├── arce-game.jar
├── arce-editor.jar
├── assets/          # Текстуры и ресурсы
│   └── textures/
└── maps/           # Файлы карт
```

## Возможные проблемы

### Ошибка "Java version"
Убедитесь, что используете Java 17 или выше:
```bash
java -version
```

### Ошибка "Maven not found"
Установите Apache Maven и добавьте его в PATH:
- Windows: Скачайте с https://maven.apache.org/
- Linux: `sudo apt install maven`
- macOS: `brew install maven`

### Отсутствуют ресурсы
Скопируйте папки `assets/` и `maps/` в ту же директорию, где находятся JAR файлы. 