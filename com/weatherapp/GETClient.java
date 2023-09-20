package com.weatherapp;

/*
 * Makes a HTTP GET request to the aggregation server and displays the weather data.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GETClient {
    public static void main(String[] args) {
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
            // Open a socket
            Socket socket = new Socket(hostName, portNumber);

            // Open an input stream and output stream to the socket
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        ) {
            // Read from and write to the stream according to the server's protocol
            String fromServer, fromUser;
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Bye."))
                    break;
                
                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }
            }
            // Close the streams and the sockets
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
