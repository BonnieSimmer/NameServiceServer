
package com.assignment;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port =12345;
        try {
            Socket socket = new Socket(serverAddress, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Enter command: ");
                String command = scanner.nextLine();

                out.println(command);

                String response = in.readLine();
                System.out.println("Server: " + response);

            }
        }
    catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}