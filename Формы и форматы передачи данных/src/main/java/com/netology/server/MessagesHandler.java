package com.netology.server;

import java.io.IOException;
import java.util.Map;

public class MessagesHandler implements Handler {
    
    @Override
    public void handle(Request request, Response response) throws IOException {
        if ("GET".equals(request.getMethod())) {
            handleGet(request, response);
        } else if ("POST".equals(request.getMethod())) {
            handlePost(request, response);
        } else {
            response.setStatus(405, "Method Not Allowed")
                   .setBody("Method " + request.getMethod() + " not allowed")
                   .send();
        }
    }

    private void handleGet(Request request, Response response) throws IOException {
        // Получаем Query параметры
        String lastParam = request.getQueryParam("last");
        String limitParam = request.getQueryParam("limit");
        
        // Формируем JSON ответ с информацией о запросе
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"method\": \"").append(request.getMethod()).append("\",\n");
        json.append("  \"path\": \"").append(request.getPath()).append("\",\n");
        json.append("  \"queryString\": \"").append(request.getQueryString()).append("\",\n");
        json.append("  \"queryParams\": {\n");
        
        Map<String, String> queryParams = request.getQueryParams();
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            json.append("    \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("\n  }\n");
        json.append("}");
        
        response.sendJson(json.toString());
    }

    private void handlePost(Request request, Response response) throws IOException {
        String contentType = request.getHeaders().get("Content-Type");
        
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            handleMultipartPost(request, response);
        } else {
            handleUrlEncodedPost(request, response);
        }
    }
    
    private void handleUrlEncodedPost(Request request, Response response) throws IOException {
        // Получаем POST параметры
        String message = request.getPostParam("message");
        String author = request.getPostParam("author");
        
        // Формируем JSON ответ с информацией о POST запросе
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"method\": \"").append(request.getMethod()).append("\",\n");
        json.append("  \"path\": \"").append(request.getPath()).append("\",\n");
        json.append("  \"contentType\": \"application/x-www-form-urlencoded\",\n");
        json.append("  \"postParams\": {\n");
        
        Map<String, String> postParams = request.getPostParams();
        boolean first = true;
        for (Map.Entry<String, String> entry : postParams.entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            json.append("    \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("\n  },\n");
        json.append("  \"body\": \"").append(request.getBody()).append("\"\n");
        json.append("}");
        
        response.sendJson(json.toString());
    }
    
    private void handleMultipartPost(Request request, Response response) throws IOException {
        // Формируем JSON ответ с информацией о multipart POST запросе
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"method\": \"").append(request.getMethod()).append("\",\n");
        json.append("  \"path\": \"").append(request.getPath()).append("\",\n");
        json.append("  \"contentType\": \"multipart/form-data\",\n");
        json.append("  \"parts\": {\n");
        
        Map<String, Part> parts = request.getParts();
        boolean first = true;
        for (Map.Entry<String, Part> entry : parts.entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            Part part = entry.getValue();
            json.append("    \"").append(entry.getKey()).append("\": {\n");
            json.append("      \"name\": \"").append(part.getName()).append("\",\n");
            json.append("      \"contentType\": \"").append(part.getContentType()).append("\",\n");
            if (part.isFile()) {
                json.append("      \"filename\": \"").append(part.getFilename()).append("\",\n");
                json.append("      \"size\": ").append(part.getSize()).append(",\n");
            }
            json.append("      \"isFile\": ").append(part.isFile()).append(",\n");
            json.append("      \"content\": \"").append(part.getStringContent()).append("\"\n");
            json.append("    }");
            first = false;
        }
        json.append("\n  }\n");
        json.append("}");
        
        response.sendJson(json.toString());
    }
} 