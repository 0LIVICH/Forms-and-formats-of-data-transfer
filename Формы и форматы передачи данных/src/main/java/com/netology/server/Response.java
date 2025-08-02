package com.netology.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Response {
    private final OutputStream outputStream;
    private int statusCode = 200;
    private String statusText = "OK";
    private final StringBuilder headers = new StringBuilder();
    private final StringBuilder body = new StringBuilder();

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public Response setStatus(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        return this;
    }

    public Response addHeader(String name, String value) {
        headers.append(name).append(": ").append(value).append("\r\n");
        return this;
    }

    public Response setBody(String body) {
        this.body.setLength(0);
        this.body.append(body);
        return this;
    }

    public Response setBody(byte[] body) {
        this.body.setLength(0);
        this.body.append(new String(body, StandardCharsets.UTF_8));
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
        outputStream.write(response.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    public void sendJson(String json) throws IOException {
        addHeader("Content-Type", "application/json; charset=utf-8");
        setBody(json);
        send();
    }

    public void sendHtml(String html) throws IOException {
        addHeader("Content-Type", "text/html; charset=utf-8");
        setBody(html);
        send();
    }

    public void sendText(String text) throws IOException {
        addHeader("Content-Type", "text/plain; charset=utf-8");
        setBody(text);
        send();
    }
} 