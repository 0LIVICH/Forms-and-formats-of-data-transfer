import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SimpleHttpServer {
    private final int port;
    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private boolean isRunning = false;

    public SimpleHttpServer(int port) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(64);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Server started on port " + port);

            while (isRunning) {
                Socket socket = serverSocket.accept();
                executorService.submit(() -> {
                    try {
                        handleRequest(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket socket) throws IOException {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            
            // Парсим запрос
            HttpRequest request = parseRequest(inputStream);
            HttpResponse response = new HttpResponse(outputStream);
            
            // Обрабатываем запрос
            if ("/messages".equals(request.getPath())) {
                handleMessages(request, response);
            } else {
                response.setStatus(404, "Not Found")
                       .setBody("Handler not found for " + request.getMethod() + " " + request.getPath())
                       .send();
            }
        }
    }

    private void handleMessages(HttpRequest request, HttpResponse response) throws IOException {
        if ("GET".equals(request.getMethod())) {
            handleGetMessages(request, response);
        } else if ("POST".equals(request.getMethod())) {
            handlePostMessages(request, response);
        } else {
            response.setStatus(405, "Method Not Allowed")
                   .setBody("Method " + request.getMethod() + " not allowed")
                   .send();
        }
    }

    private void handleGetMessages(HttpRequest request, HttpResponse response) throws IOException {
        // Получаем Query параметры
        String lastParam = request.getQueryParam("last");
        String limitParam = request.getQueryParam("limit");
        
        // Формируем JSON ответ
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

    private void handlePostMessages(HttpRequest request, HttpResponse response) throws IOException {
        // Получаем POST параметры
        String message = request.getPostParam("message");
        String author = request.getPostParam("author");
        
        // Формируем JSON ответ
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"method\": \"").append(request.getMethod()).append("\",\n");
        json.append("  \"path\": \"").append(request.getPath()).append("\",\n");
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

    private HttpRequest parseRequest(InputStream inputStream) throws IOException {
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
        
        return new HttpRequest(method, path, headers, body.toString());
    }

    public void stop() {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }

    public static void main(String[] args) {
        SimpleHttpServer server = new SimpleHttpServer(9999);
        server.start();
    }
}

class HttpRequest {
    private final String method;
    private final String path;
    private final String queryString;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Map<String, String> postParams;
    private final String body;

    public HttpRequest(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.queryParams = new HashMap<>();
        this.postParams = new HashMap<>();
        
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
        
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                String key = decodeUrl(keyValue[0]);
                String value = decodeUrl(keyValue[1]);
                queryParams.put(key, value);
            }
        }
    }

    private void parsePostParams() {
        String contentType = headers.get("Content-Type");
        if (contentType == null || !contentType.startsWith("application/x-www-form-urlencoded")) {
            return;
        }
        
        if (body == null || body.isEmpty()) {
            return;
        }
        
        String[] params = body.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                String key = decodeUrl(keyValue[0]);
                String value = decodeUrl(keyValue[1]);
                postParams.put(key, value);
            }
        }
    }

    private String decodeUrl(String encoded) {
        try {
            return java.net.URLDecoder.decode(encoded, "UTF-8");
        } catch (Exception e) {
            return encoded;
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
}

class HttpResponse {
    private final OutputStream outputStream;
    private int statusCode = 200;
    private String statusText = "OK";
    private final StringBuilder headers = new StringBuilder();
    private final StringBuilder body = new StringBuilder();

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public HttpResponse setStatus(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        return this;
    }

    public HttpResponse addHeader(String name, String value) {
        headers.append(name).append(": ").append(value).append("\r\n");
        return this;
    }

    public HttpResponse setBody(String body) {
        this.body.setLength(0);
        this.body.append(body);
        return this;
    }

    public void send() throws IOException {
        // Формируем ответ
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");
        
        // Добавляем заголовки
        if (!headers.toString().contains("Content-Type")) {
            headers.insert(0, "Content-Type: text/plain; charset=utf-8\r\n");
        }
        if (!headers.toString().contains("Content-Length")) {
            headers.insert(0, "Content-Length: " + body.length() + "\r\n");
        }
        response.append(headers);
        
        // Добавляем пустую строку между заголовками и телом
        response.append("\r\n");
        
        // Добавляем тело ответа
        response.append(body);
        
        // Отправляем ответ
        outputStream.write(response.toString().getBytes("UTF-8"));
        outputStream.flush();
    }

    public void sendJson(String json) throws IOException {
        addHeader("Content-Type", "application/json; charset=utf-8");
        setBody(json);
        send();
    }
} 