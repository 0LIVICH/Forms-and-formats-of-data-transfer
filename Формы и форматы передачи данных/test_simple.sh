#!/bin/bash

echo "Testing Simple HTTP Server with Query Parameters"
echo "==============================================="

# Запускаем сервер в фоне
echo "Starting server..."
java SimpleHttpServer &
SERVER_PID=$!

# Ждем запуска сервера
sleep 2

echo ""
echo "1. Testing GET request with query parameters:"
echo "GET /messages?last=10&limit=20"
curl -s "http://localhost:9999/messages?last=10&limit=20" | jq '.' 2>/dev/null || curl -s "http://localhost:9999/messages?last=10&limit=20"

echo ""
echo "2. Testing GET request without query parameters:"
echo "GET /messages"
curl -s "http://localhost:9999/messages" | jq '.' 2>/dev/null || curl -s "http://localhost:9999/messages"

echo ""
echo "3. Testing POST request with form-urlencoded:"
echo "POST /messages with message=Hello&author=John"
curl -s -X POST "http://localhost:9999/messages" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "message=Hello&author=John" | jq '.' 2>/dev/null || curl -s -X POST "http://localhost:9999/messages" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "message=Hello&author=John"

echo ""
echo "4. Testing URL encoding:"
echo "GET /messages?name=John%20Doe&city=Moscow"
curl -s "http://localhost:9999/messages?name=John%20Doe&city=Moscow" | jq '.' 2>/dev/null || curl -s "http://localhost:9999/messages?name=John%20Doe&city=Moscow"

echo ""
echo "5. Testing non-existent endpoint:"
echo "GET /nonexistent"
curl -s "http://localhost:9999/nonexistent"

# Останавливаем сервер
echo ""
echo "Stopping server..."
kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null

echo "Tests completed!" 