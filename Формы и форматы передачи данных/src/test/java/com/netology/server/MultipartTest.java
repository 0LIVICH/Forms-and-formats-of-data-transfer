package com.netology.server;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MultipartTest {

    @Test
    public void testMultipartFormData() throws IOException {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String requestData = 
            "POST /messages HTTP/1.1\r\n" +
            "Host: localhost:9999\r\n" +
            "Content-Type: multipart/form-data; boundary=" + boundary + "\r\n" +
            "Content-Length: 300\r\n" +
            "\r\n" +
            "--" + boundary + "\r\n" +
            "Content-Disposition: form-data; name=\"message\"\r\n" +
            "\r\n" +
            "Hello World\r\n" +
            "--" + boundary + "\r\n" +
            "Content-Disposition: form-data; name=\"author\"\r\n" +
            "\r\n" +
            "John Doe\r\n" +
            "--" + boundary + "\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "This is a test file content\r\n" +
            "--" + boundary + "--\r\n";
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestData.getBytes(StandardCharsets.UTF_8));
        Request request = Request.fromInputStream(inputStream);
        
        assertEquals("POST", request.getMethod());
        assertEquals("/messages", request.getPath());
        
        // Проверяем части
        Part messagePart = request.getPart("message");
        assertNotNull(messagePart);
        assertEquals("message", messagePart.getName());
        assertEquals("Hello World", messagePart.getStringContent());
        assertTrue(messagePart.isField());
        assertFalse(messagePart.isFile());
        
        Part authorPart = request.getPart("author");
        assertNotNull(authorPart);
        assertEquals("author", authorPart.getName());
        assertEquals("John Doe", authorPart.getStringContent());
        assertTrue(authorPart.isField());
        
        Part filePart = request.getPart("file");
        assertNotNull(filePart);
        assertEquals("file", filePart.getName());
        assertEquals("test.txt", filePart.getFilename());
        assertEquals("text/plain", filePart.getContentType());
        assertEquals("This is a test file content", filePart.getStringContent());
        assertTrue(filePart.isFile());
        assertFalse(filePart.isField());
        
        // Проверяем все части
        Map<String, Part> parts = request.getParts();
        assertEquals(3, parts.size());
        assertTrue(parts.containsKey("message"));
        assertTrue(parts.containsKey("author"));
        assertTrue(parts.containsKey("file"));
    }

    @Test
    public void testMultipartWithoutFiles() throws IOException {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String requestData = 
            "POST /messages HTTP/1.1\r\n" +
            "Host: localhost:9999\r\n" +
            "Content-Type: multipart/form-data; boundary=" + boundary + "\r\n" +
            "Content-Length: 200\r\n" +
            "\r\n" +
            "--" + boundary + "\r\n" +
            "Content-Disposition: form-data; name=\"title\"\r\n" +
            "\r\n" +
            "My Title\r\n" +
            "--" + boundary + "\r\n" +
            "Content-Disposition: form-data; name=\"content\"\r\n" +
            "\r\n" +
            "My Content\r\n" +
            "--" + boundary + "--\r\n";
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestData.getBytes(StandardCharsets.UTF_8));
        Request request = Request.fromInputStream(inputStream);
        
        Part titlePart = request.getPart("title");
        assertNotNull(titlePart);
        assertEquals("My Title", titlePart.getStringContent());
        assertTrue(titlePart.isField());
        
        Part contentPart = request.getPart("content");
        assertNotNull(contentPart);
        assertEquals("My Content", contentPart.getStringContent());
        assertTrue(contentPart.isField());
        
        Map<String, Part> parts = request.getParts();
        assertEquals(2, parts.size());
    }
} 