package com.assignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ServiceRegistry registry;
    // Tracks the role of this specific connection
    private boolean isAdmin = false; 

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.registry = ServiceRegistry.getInstance();
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                if (request.trim().isEmpty()) continue;
                System.out.println("Received: " + request);

                String[] tokens = request.split(" ");
                String command = tokens[0].toUpperCase();

                // 1. Role Assignment Interceptor
                if (command.equals("ROLE")) {
                    if (tokens.length == 2) {
                        isAdmin = tokens[1].equalsIgnoreCase("admin");
                        out.println("ACK: Role set to " + tokens[1].toLowerCase());
                    } else {
                        out.println("ERROR: Invalid ROLE format.");
                    }
                    continue; // Skip the rest of the loop and wait for next command
                }

                // 2. Remote Shutdown Interceptor
                if (command.equals("SHUTDOWN")) {
                    if (isAdmin) {
                        out.println("ACK: Server is shutting down...");
                        NameServiceApplication.shutdownServer(); // Calls the main app
                        break; // Exits this thread
                    } else {
                        out.println("ERROR: Permission denied. Only admins can shutdown the server.");
                        continue;
                    }
                }

                // 3. Graceful Exit Handler
                if (command.equals("EXIT") || command.equals("QUIT")) {
                    out.println("ACK: Disconnecting from Name Service...");
                    System.out.println("Node requested disconnect.");
                    break; 
                }

                // 4. Standard Assignment Protocol
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
               
                NameServiceApplication.removeClient(clientSocket); 
                
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
       