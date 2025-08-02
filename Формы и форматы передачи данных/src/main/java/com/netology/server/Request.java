package com.netology.server;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final String queryString;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Map<String, String> postParams;
    private final String body;
    private final Map<String, Part> parts;

    public Request(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.queryParams = new HashMap<>();
        this.postParams = new HashMap<>();
        this.parts = new HashMap<>();
        
        // Разделяем путь и query string
        String[] pathAndQuery = path.split("\\?", 2);
        this.path = pathAndQuery[0];
        this.queryString = pathAndQuery.length > 1 ? pathAndQuery[1] : "";
        
        // Парсим query параметры
        parseQueryParams();
        
        // Парсим POST параметры если это POST запрос
        if ("POST".equals(method)) {
            parsePostParams();
        }
    }

    private void parseQueryParams() {
        if (queryString.isEmpty()) {
            return;
        }
        
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
            for (NameValuePair param : params) {
                queryParams.put(param.getName(), param.getValue());
            }
        } catch (Exception e) {
            // Игнорируем ошибки парсинга
        }
    }

    private void parsePostParams() {
        String contentType = headers.get("Content-Type");
        if (contentType == null) {
            return;
        }

        if (contentType.startsWith("application/x-www-form-urlencoded")) {
            parseUrlEncodedBody();
        } else if (contentType.startsWith("multipart/form-data")) {
            parseMultipartBody();
        }
    }

    private void parseUrlEncodedBody() {
        if (body == null || body.isEmpty()) {
            return;
        }
        
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
            for (NameValuePair param : params) {
                postParams.put(param.getName(), param.getValue());
            }
        } catch (Exception e) {
            // Игнорируем ошибки парсинга
        }
    }

    private void parseMultipartBody() {
        if (body == null || body.isEmpty()) {
            return;
        }
        
        try {
            String boundary = extractBoundary(headers.get("Content-Type"));
            if (boundary == null) {
                return;
            }
            
            // Разбиваем тело на части по boundary
            String[] parts = body.split("--" + boundary);
            
            for (String part : parts) {
                if (part.trim().isEmpty() || part.contains("--")) {
                    continue; // Пропускаем пустые части и финальный boundary
                }
                
                parseMultipartPart(part);
            }
        } catch (Exception e) {
            // Игнорируем ошибки парсинга
        }
    }
    
    private String extractBoundary(String contentType) {
        if (contentType == null) {
            return null;
        }
        
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return part.substring("boundary=".length()).replace("\"", "");
            }
        }
        return null;
    }
    
    private void parseMultipartPart(String partData) {
        try {
            // Разделяем заголовки и содержимое
            String[] sections = partData.split("\r\n\r\n", 2);
            if (sections.length < 2) {
                return;
            }
            
            String headersSection = sections[0];
            String content = sections[1].trim();
            
            // Парсим заголовки
            Map<String, String> partHeaders = new HashMap<>();
            String name = null;
            String filename = null;
            String contentType = "text/plain";
            
            String[] headerLines = headersSection.split("\r\n");
            for (String line : headerLines) {
                if (line.startsWith("Content-Disposition:")) {
                    // Извлекаем name и filename
                    String[] params = line.split(";");
                    for (String param : params) {
                        param = param.trim();
                        if (param.startsWith("name=")) {
                            name = param.substring("name=".length()).replace("\"", "");
                        } else if (param.startsWith("filename=")) {
                            filename = param.substring("filename=".length()).replace("\"", "");
                        }
                    }
                } else if (line.startsWith("Content-Type:")) {
                    contentType = line.substring("Content-Type:".length()).trim();
                }
            }
            
            if (name != null) {
                byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
                Part part = new Part(name, contentType, filename, contentBytes, partHeaders);
                parts.put(name, part);
            }
        } catch (Exception e) {
            // Игнорируем ошибки парсинга
        }
    }

    // Методы для работы с Query параметрами
    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return new HashMap<>(queryParams);
    }

    // Методы для работы с POST параметрами
    public String getPostParam(String name) {
        return postParams.get(name);
    }

    public Map<String, String> getPostParams() {
        return new HashMap<>(postParams);
    }

    // Методы для работы с multipart частями
    public Part getPart(String name) {
        return parts.get(name);
    }

    public Map<String, Part> getParts() {
        return new HashMap<>(parts);
    }

    // Геттеры
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryString() {
        return queryString;
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public String getBody() {
        return body;
    }

    public static Request fromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        
        // Читаем первую строку (Request Line)
        String requestLine = reader.readLine();
        if (requestLine == null) {
            throw new IOException("Empty request");
        }
        
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid request line: " + requestLine);
        }
        
        String method = parts[0];
        String path = parts[1];
        
        // Читаем заголовки
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(name, value);
            }
        }
        
        // Читаем тело запроса
        StringBuilder body = new StringBuilder();
        int contentLength = 0;
        try {
            contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
        } catch (NumberFormatException e) {
            // Игнорируем
        }
        
        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            int bytesRead = reader.read(buffer, 0, contentLength);
            if (bytesRead > 0) {
                body.append(buffer, 0, bytesRead);
            }
        }
        
        return new Request(method, path, headers, body.toString());
    }
} 