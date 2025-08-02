package com.netology.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int port;
    private final ExecutorService executorService;
    private final RequestHandler requestHandler;
    private ServerSocket serverSocket;
    private boolean isRunning = false;

    public HttpServer(int port) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(64);
        this.requestHandler = new RequestHandler();
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
                        requestHandler.handle(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void addHandler(String method, String path, Handler handler) {
        requestHandler.addHandler(method, path, handler);
    }

    public static void main(String[] args) {
        HttpServer server = new HttpServer(9999);
        
        // Добавляем обработчики
        server.addHandler("GET", "/messages", new MessagesHandler());
        server.addHandler("POST", "/messages", new MessagesHandler());
        
        server.start();
    }
} 