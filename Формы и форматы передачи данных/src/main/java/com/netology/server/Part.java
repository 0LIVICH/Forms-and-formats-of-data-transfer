package com.netology.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Part {
    private final String name;
    private final String contentType;
    private final String filename;
    private final byte[] content;
    private final Map<String, String> headers;

    public Part(String name, String contentType, String filename, byte[] content) {
        this.name = name;
        this.contentType = contentType;
        this.filename = filename;
        this.content = content;
        this.headers = new HashMap<>();
    }

    public Part(String name, String contentType, String filename, byte[] content, Map<String, String> headers) {
        this.name = name;
        this.contentType = contentType;
        this.filename = filename;
        this.content = content;
        this.headers = new HashMap<>(headers);
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getContent() {
        return content;
    }

    public String getStringContent() {
        return new String(content, java.nio.charset.StandardCharsets.UTF_8);
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public boolean isFile() {
        return filename != null && !filename.isEmpty();
    }

    public boolean isField() {
        return !isFile();
    }

    public long getSize() {
        return content != null ? content.length : 0;
    }
} 