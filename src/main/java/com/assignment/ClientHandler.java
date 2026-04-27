package com.assignment;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ServiceRegistry registry;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.registry = ServiceRegistry.getInstance(); // Access the shared Singleton
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Received: " + request);
                
                if (request.trim().isEmpty()) continue;

                String[] tokens = request.split(" ");
                String command = tokens[0].toUpperCase();

                // Graceful exit handler
                if (command.equals("EXIT") || command.equals("QUIT")) {
                    out.println("ACK: Disconnecting from Name Service...");
                    System.out.println("Node requested disconnect.");
                    break; 
                }

                try {
                    switch (command) {
                        case "REGISTER":
                            if (tokens.length == 3) {
                                registry.register(tokens[1], tokens[2]);
                                out.println("ACK: Service " + tokens[1] + " registered successfully.");
                            } else {
                                out.println("ERROR: Invalid REGISTER format.");
                            }
                            break;

                        case "RESOLVE":
                            if (tokens.length == 2) {
                                String ip = registry.resolve(tokens[1]);
                                out.println("SUCCESS: " + ip);
                            } else {
                                out.println("ERROR: Invalid RESOLVE format.");
                            }
                            break;

                        case "DEREGISTER":
                            if (tokens.length == 2) {
                                registry.deregister(tokens[1]);
                                out.println("ACK: Service " + tokens[1] + " deregistered.");
                            } else {
                                out.println("ERROR: Invalid DEREGISTER format.");
                            }
                            break;

                        default:
                            out.println("ERROR: Unknown command.");
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    out.println("ERROR: " + e.getMessage());
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("IP address already registered")) {
                        out.println("ERROR: IP Already Registered"); 
                    } else if (e.getMessage().contains("Name not found")) {
                        out.println("ERROR: Not Found"); 
                    } else {
                        out.println("ERROR: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Client handler exception: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}