package com.netology.server;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RequestTest {

    @Test
    public void testQueryParamParsing() throws IOException {
        String requestData = 
            "GET /messages?last=10&limit=20 HTTP/1.1\r\n" +
            "Host: localhost:9999\r\n" +
            "User-Agent: curl/7.68.0\r\n" +
            "\r\n";
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestData.getBytes(StandardCharsets.UTF_8));
        Request request = Request.fromInputStream(inputStream);
        
        assertEquals("GET", request.getMethod());
        assertEquals("/messages", request.getPath());
        assertEquals("last=10&limit=20", request.getQueryString());
        
        assertEquals("10", request.getQueryParam("last"));
        assertEquals("20", request.getQueryParam("limit"));
        assertNull(request.getQueryParam("nonexistent"));
        
        Map<String, String> queryParams = request.getQueryParams();
        assertEquals(2, queryParams.size());
        assertEquals("10", queryParams.get("last"));
        assertEquals("20", queryParams.get("limit"));
    }

    @Test
    public void testRequestWithoutQueryParams() throws IOException {
        String requestData = 
            "GET /messages HTTP/1.1\r\n" +
            "Host: localhost:9999\r\n" +
            "\r\n";
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestData.getBytes(StandardCharsets.UTF_8));
        Request request = Request.fromInputStream(inputStream);
        
        assertEquals("GET", request.getMethod());
        assertEquals("/messages", request.getPath());
        assertEquals("", request.getQueryString());
        
        Map<String, String> queryParams = request.getQueryParams();
        assertTrue(queryParams.isEmpty());
    }

    @Test
    public void testPostWithUrlEncodedBody() throws IOException {
        String requestData = 
            "POST /messages HTTP/1.1\r\n" +
            "Host: localhost:9999\r\n" +
            "Content-Type: application/x-www-form-urlencoded\r\n" +
            "Content-Length: 25\r\n" +
            "\r\n" +
            "message=Hello&author=John";
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestData.getBytes(StandardCharsets.UTF_8));
        Request request = Request.fromInputStream(inputStream);
        
        assertEquals("POST", request.getMethod());
        assertEquals("/messages", request.getPath());
        
        assertEquals("Hello", request.getPostParam("message"));
        assertEquals("John", request.getPostParam("author"));
        
        Map<String, String> postParams = request.getPostParams();
        assertEquals(2, postParams.size());
        assertEquals("Hello", postParams.get("message"));
        assertEquals("John", postParams.get("author"));
    }

    @Test
    public void testUrlEncoding() throws IOException {
        String requestData = 
            "GET /messages?name=John%20Doe&city=Moscow HTTP/1.1\r\n" +
            "Host: localhost:9999\r\n" +
            "\r\n";
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestData.getBytes(StandardCharsets.UTF_8));
        Request request = Request.fromInputStream(inputStream);
        
        assertEquals("John Doe", request.getQueryParam("name"));
        assertEquals("Moscow", request.getQueryParam("city"));
    }
} 