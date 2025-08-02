package com.netology.server;

import java.io.IOException;

@FunctionalInterface
public interface Handler {
    void handle(Request request, Response response) throws IOException;
} 