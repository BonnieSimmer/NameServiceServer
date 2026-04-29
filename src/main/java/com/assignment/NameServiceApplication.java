package com.assignment; 

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList; // NEW IMPORT

public class NameServiceApplication {

    private static final int PORT = 12345;
    private static volatile boolean isRunning = true; 
    private static ServerSocket serverSocket; 
    
    // NEW: A thread-safe list to track all active client sockets
    private static final CopyOnWriteArrayList<Socket> activeClients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("Starting Name Service Server on port " + PORT + "...");
        ServiceRegistry registry = ServiceRegistry.getInstance();

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is active and listening for nodes...");

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connection established with node: " + clientSocket.getInetAddress());

                    // NEW: Add the connected client to our tracking list
                    activeClients.add(clientSocket);

                    ClientHandler handler = new ClientHandler(clientSocket);
                    Thread clientThread = new Thread(handler);
                    clientThread.start();
                    
                } catch (IOException e) {
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

    public static void shutdownServer() {
        System.out.println("Remote shutdown initiated by an Admin node...");
        isRunning = false;
        try {
            // 1. Stop accepting new clients
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); 
            }
            
            // 2. Forcefully close all currently connected clients
            for (Socket client : activeClients) {
                if (client != null && !client.isClosed()) {
                    client.close(); // This instantly interrupts their threads!
                }
            }
            System.out.println("All active client connections have been severed.");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // NEW: Method to clean up the list if a client disconnects normally (EXIT)
    public static void removeClient(Socket clientSocket) {
        activeClients.remove(clientSocket);
    }
}