/*
 * Manages and stores weather information, sends clients weather data, and accepts weather updates from content servers.
 * Stores weather information persistently, only removing it when the content server who provided it is no longer in contact, 
 * or when the weather data is too old (not one of the most recent 20 updates).
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;

    public AggregationServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                System.out.println("Listening for a connection");

                // Call accept() to receive the next connection
                Socket socket = serverSocket.accept();

                // Pass the socket to the RequestHandler thread for processing
                RequestHandler requestHandler = new RequestHandler(socket);
                requestHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java AggregationServer <port number>");
            System.exit(0);
        }

        int portNumber = Integer.parseInt(args[0]);
        System.out.println("Starting AggregationServer on port " + portNumber);

        AggregationServer server = new AggregationServer(portNumber);
        server.startServer();

        // Automatically shutdown in 1 minute
        try {
            Thread.sleep(60000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.stopServer();

        // int portNumber = Integer.parseInt(args[0]);

        // try (
        //     ServerSocket serverSocket = new ServerSocket(portNumber);
        //     Socket clientSocket = serverSocket.accept();

        //     // Get socket's input and output stream and open readers and writers on them
        //     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        //     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // ) {
        //     String inputLine, outputLine;

        //     // Initiate conversation with client by writing to the socket
        //     WeatherProtocol wp = new WeatherProtocol();
        //     outputLine = wp.processInput(null);
        //     out.println(outputLine);

        //     // Communicate with the client by reading from and writing to the socket
        //     while ((inputLine = in.readLine()) != null) {
        //         outputLine = wp.processInput(inputLine);
        //         out.println(outputLine);
        //         if (outputLine.equals("Bye."))
        //             break;
        //     }
        // }
    }
 }

 class RequestHandler extends Thread {
    private Socket socket;

    RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Received a connection");

            // Get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Write out our header to the client
            out.println("Aggregation Server 1.0");
            out.flush();

            String inputLine, outputLine;
            // Initiate conversation with client by writing to the socket
            WeatherProtocol wp = new WeatherProtocol();
            outputLine = wp.processInput(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                // Use WeatherProtocol to parse the input
                outputLine = wp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }

            // Close our connection
            in.close();
            out.close();
            socket.close();

            System.out.println("Connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 }