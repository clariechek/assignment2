/*
 * Manages and stores weather information, sends clients weather data, and accepts weather updates from content servers.
 * Stores weather information persistently, only removing it when the content server who provided it is no longer in contact, 
 * or when the weather data is too old (not one of the most recent 20 updates).
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer {
    public static void main(String[] args) {
        int portNumber = Integer.parseInt(args[0]);

        try (
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();

            // Get socket's input and output stream and open readers and writers on them
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine, outputLine;

            // Initiate conversation with client by writing to the socket
            WeatherProtocol wp = new WeatherProtocol();
            outputLine = wp.processInput(null);
            out.println(outputLine);

            // Communicate with the client by reading from and writing to the socket
            while ((inputLine = in.readLine()) != null) {
                outputLine = wp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
        }
    }
 }