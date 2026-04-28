package com.assignment;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class NameServiceApplication {

    private static final int PORT = 12345;
    // Volatile ensures the boolean is synced across threads instantly
    private static volatile boolean isRunning = true; 

    public static void main(String[] args) {
        System.out.println("Starting Name Service Server on port " + PORT + "...");
        
        // Ensures the Singleton registry is initialized
        ServiceRegistry registry = ServiceRegistry.getInstance();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is active and listening for nodes...");
            System.out.println("Type 'SHUTDOWN' in this terminal to stop the server.");

            // 1. Create a background Admin Thread to listen for the shutdown command
            Thread adminThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (isRunning) {
                    String input = scanner.nextLine();
                    if ("SHUTDOWN".equalsIgnoreCase(input.trim()) || "EXIT".equalsIgnoreCase(input.trim())) {
                        System.out.println("Shutdown command received. Closing server...");
                        isRunning = false;
                        try {
                            // Closing the socket breaks the blocking accept() method below
                            serverSocket.close(); 
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            });
            adminThread.start();

            // 2. The main loop
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connection established with node: " + clientSocket.getInetAddress());

                    ClientHandler handler = new ClientHandler(clientSocket);
                    Thread clientThread = new Thread(handler);
                    clientThread.start();
                    
                } catch (IOException e) {
                    // When serverSocket.close() is called, accept() throws an exception.
                    // If isRunning is false, we know it was an intentional shutdown.
                    if (!isRunning) {
                        System.out.println("Server socket successfully closed.");
                    } else {
                        System.err.println("Accept failed: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Server network exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Name Service Server has cleanly shut down.");
    }
}