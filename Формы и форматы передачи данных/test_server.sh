#!/bin/bash

echo "Testing HTTP Server with Query Parameters"
echo "========================================"

# Запускаем сервер в фоне
echo "Starting server..."
mvn exec:java -Dexec.mainClass="com.netology.server.HttpServer" &
SERVER_PID=$!

# Ждем запуска сервера
sleep 3

echo ""
echo "1. Testing GET request with query parameters:"
echo "GET /messages?last=10&limit=20"
curl -s "http://localhost:9999/messages?last=10&limit=20" | jq '.'

echo ""
echo "2. Testing GET request without query parameters:"
echo "GET /messages"
curl -s "http://localhost:9999/messages" | jq '.'

echo ""
echo "3. Testing POST request with form-urlencoded:"
echo "POST /messages with message=Hello&author=John"
curl -s -X POST "http://localhost:9999/messages" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "message=Hello&author=John" | jq '.'

echo ""
echo "4. Testing POST request with multipart/form-data:"
echo "POST /messages with multipart data"
curl -s -X POST "http://localhost:9999/messages" \
     -F "message=Hello World" \
     -F "author=John Doe" \
     -F "file=@test_server.sh" | jq '.'

echo ""
echo "5. Testing non-existent endpoint:"
echo "GET /nonexistent"
curl -s "http://localhost:9999/nonexistent" | jq '.'

# Останавливаем сервер
echo ""
echo "Stopping server..."
kill $SERVER_PID
wait $SERVER_PID 2>/dev/null

echo "Tests completed!" 