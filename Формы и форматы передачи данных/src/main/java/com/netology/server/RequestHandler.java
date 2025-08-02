package com.netology.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler {
    private final Map<String, Handler> handlers = new HashMap<>();

    public void addHandler(String method, String path, Handler handler) {
        String key = method + ":" + path;
        handlers.put(key, handler);
    }

    public void handle(Socket socket) throws IOException {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            
            // Парсим запрос
            Request request = Request.fromInputStream(inputStream);
            Response response = new Response(outputStream);
            
            // Ищем обработчик
            String key = request.getMethod() + ":" + request.getPath();
            Handler handler = handlers.get(key);
            
            if (handler != null) {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(500, "Internal Server Error")
                           .setBody("Internal Server Error: " + e.getMessage())
                           .send();
                }
            } else {
                // Обработчик не найден
                response.setStatus(404, "Not Found")
                       .setBody("Handler not found for " + request.getMethod() + " " + request.getPath())
                       .send();
            }
        } catch (IOException e) {
            // Ошибка при обработке запроса
            try (OutputStream outputStream = socket.getOutputStream()) {
                Response response = new Response(outputStream);
                response.setStatus(400, "Bad Request")
                       .setBody("Bad Request: " + e.getMessage())
                       .send();
            }
        }
    }
} 