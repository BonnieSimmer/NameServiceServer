package com.assignment;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NameServiceApplication {

    // Set server port to 12345 
    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("Starting Name Service Server on port " + PORT + "...");
        
        // Ensures the Singleton registry is initialized when the server starts
        ServiceRegistry registry = ServiceRegistry.getInstance();

        // 1. Create the ServerSocket
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is active and listening for nodes...");

            // 2. Infinite loop to accept connections simultaneously 
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection established with node: " + clientSocket.getInetAddress());

                // 3. Dispatch the socket to a new thread (Non-blocking)
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }

        } catch (IOException e) {
            System.err.println("Server network exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
