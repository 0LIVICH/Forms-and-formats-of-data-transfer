# HTTP Server с поддержкой Query параметров

Этот проект представляет собой HTTP сервер на Java с поддержкой обработки Query параметров и POST параметров.

## Функциональность

### 1. Query параметры (основная задача) ✅
- Поддержка GET запросов с Query параметрами
- Методы `getQueryParam(String name)` и `getQueryParams()`
- Автоматическое декодирование URL-encoded параметров

### 2. POST параметры (задача со звездочкой) ✅
- Поддержка application/x-www-form-urlencoded
- Методы `getPostParam(String name)` и `getPostParams()`

### 3. Multipart/form-data (задача со звездочкой) ⚠️
- Базовая поддержка в полной версии с Maven
- Упрощенная версия без внешних зависимостей

## Быстрый старт

### Упрощенная версия (без Maven)

1. Скомпилируйте сервер:
```bash
javac SimpleHttpServer.java
```

2. Запустите сервер:
```bash
java SimpleHttpServer
```

Сервер запустится на порту 9999.

### Полная версия (с Maven)

1. Установите Maven (если не установлен):
```bash
brew install maven
```

2. Скомпилируйте проект:
```bash
mvn compile
```

3. Запустите сервер:
```bash
mvn exec:java -Dexec.mainClass="com.netology.server.HttpServer"
```

## Примеры использования

### GET запрос с Query параметрами
```bash
curl "http://localhost:9999/messages?last=10&limit=20"
```

Ответ:
```json
{
  "method": "GET",
  "path": "/messages",
  "queryString": "last=10&limit=20",
  "queryParams": {
    "last": "10",
    "limit": "20"
  }
}
```

### GET запрос без Query параметров
```bash
curl "http://localhost:9999/messages"
```

Ответ:
```json
{
  "method": "GET",
  "path": "/messages",
  "queryString": "",
  "queryParams": {
  }
}
```

### POST запрос с form-urlencoded
```bash
curl -X POST "http://localhost:9999/messages" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "message=Hello&author=John"
```

Ответ:
```json
{
  "method": "POST",
  "path": "/messages",
  "postParams": {
    "message": "Hello",
    "author": "John"
  },
  "body": "message=Hello&author=John"
}
```

### Тестирование несуществующего endpoint
```bash
curl "http://localhost:9999/nonexistent"
```

Ответ:
```
Handler not found for GET /nonexistent
```

## API

### HttpRequest класс (упрощенная версия)

#### Query параметры
- `String getQueryParam(String name)` - получить значение Query параметра
- `Map<String, String> getQueryParams()` - получить все Query параметры

#### POST параметры
- `String getPostParam(String name)` - получить значение POST параметра
- `Map<String, String> getPostParams()` - получить все POST параметры

#### Геттеры
- `String getMethod()` - HTTP метод
- `String getPath()` - путь запроса (без Query параметров)
- `String getQueryString()` - строка Query параметров
- `Map<String, String> getHeaders()` - заголовки запроса
- `String getBody()` - тело запроса

### Request класс (полная версия)

Дополнительно поддерживает:
- `Part getPart(String name)` - получить часть multipart запроса
- `Map<String, Part> getParts()` - получить все части multipart запроса

## Архитектурные решения

### Упрощенная версия
1. **Минимальные зависимости**: Только стандартная Java библиотека
2. **Простой парсинг**: Ручной парсинг Query и POST параметров
3. **URL декодирование**: Использование `java.net.URLDecoder`
4. **Многопоточность**: `ExecutorService` для обработки запросов

### Полная версия
1. **Apache HttpComponents**: Для корректного парсинга URL-encoded параметров
2. **Apache Commons FileUpload**: Для multipart/form-data
3. **Модульная архитектура**: Разделение на отдельные классы
4. **Расширяемость**: Система обработчиков

## Тестирование

### Автоматические тесты
```bash
# Для полной версии
mvn test
```

### Ручное тестирование
```bash
# Запустите сервер
java SimpleHttpServer

# В другом терминале протестируйте
curl "http://localhost:9999/messages?last=10&limit=20"
curl -X POST "http://localhost:9999/messages" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "message=Hello&author=John"
```

## Структура проекта

```
├── SimpleHttpServer.java          # Упрощенная версия (без зависимостей)
├── src/main/java/com/netology/server/
│   ├── HttpServer.java           # Основной класс сервера
│   ├── Request.java              # Класс для работы с HTTP запросами
│   ├── Response.java             # Класс для формирования HTTP ответов
│   ├── Handler.java              # Интерфейс для обработчиков
│   ├── RequestHandler.java       # Маршрутизатор запросов
│   ├── MessagesHandler.java      # Пример обработчика
│   └── Part.java                # Класс для multipart частей
├── src/test/java/com/netology/server/
│   ├── RequestTest.java          # Тесты для Query параметров
│   └── MultipartTest.java       # Тесты для multipart
├── pom.xml                       # Maven конфигурация
└── README.md                     # Документация
```

## Особенности реализации

### Query параметры
- Автоматическое разделение пути и Query строки
- Декодирование URL-encoded значений
- Поддержка множественных параметров с одинаковыми именами

### POST параметры
- Поддержка application/x-www-form-urlencoded
- Автоматическое декодирование значений
- Обработка пустых значений

### Обработка ошибок
- Корректная обработка некорректных запросов
- Возврат соответствующих HTTP статус кодов
- Логирование ошибок

### Производительность
- Многопоточная обработка запросов
- Неблокирующие операции ввода-вывода
- Эффективное управление памятью 