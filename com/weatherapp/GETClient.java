package com.weatherapp;

/*
 * Makes a HTTP GET request to the aggregation server and displays the weather data.
 */

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class GETClient {
    public static void main(String[] args) {
        // Get server name and port number from "http://servername.domain.domain:portnumber", and station ID.
        URI uri;
        String serverName = "localhost";
        int portNumber = 4567;
        try {
            uri = new URI(args[0]);
            // URL url = uri.toURL();
            serverName = uri.getHost();
            System.out.println("Server name: " + uri.getHost() + " Port number: " + uri.getPort() + " Station ID: " + args[1]);
            if (uri.getPort() == -1) {
                portNumber = 4567;
            } else {
                portNumber = uri.getPort();
            } 
        } catch (URISyntaxException e) {
            System.out.println("Exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        int stationID = Integer.parseInt(args[1]);
        // String hostName = args[0];
        // int portNumber = Integer.parseInt(args[1]);
        // int stationID = Integer.parseInt(args[2]);

        try {
            // Open a socket
            Socket socket = new Socket(serverName, portNumber);

            // Open an input stream and output stream to the socket
            PrintStream out = new PrintStream(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            // GET request
            out.println("GET / HTTP/1.1");
            out.println("Station ID:" + stationID);
            out.println();

            // Check server response
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                if (serverResponse.equals("HTTP/1.1 200 OK")) {
                    System.out.println("Server: " + serverResponse);
                    serverResponse = in.readLine();
                    break;
                }
            }

            // Parse and display weather data
            JSONParser parser = new JSONParser();
            try {
                JSONObject serverData = (JSONObject) parser.parse(serverResponse);
                System.out.println("Weather data for station: " + serverData.get("content_server_id"));

                JSONArray weatherData = (JSONArray) serverData.get("weather_data");
                for (Object data : weatherData) {
                    JSONObject jsonObject = (JSONObject) data;

                    System.out.println("Id: " + jsonObject.get("id"));
                    System.out.println("Name: " + jsonObject.get("name"));
                    System.out.println("State: " + jsonObject.get("state"));
                    System.out.println("Time Zone: " + jsonObject.get("time_zone"));
                    System.out.println("Latitude: " + jsonObject.get("latitude"));
                    System.out.println("Longitude: " + jsonObject.get("longitude"));
                    System.out.println("Local Date Time: " + jsonObject.get("local_date_time"));
                    System.out.println("Local Date Time (Full): " + jsonObject.get("local_date_time_full"));
                    System.out.println("Air Temperature: " + jsonObject.get("air_temp"));
                    System.out.println("Apparent Temperature: " + jsonObject.get("apparent_t"));
                    System.out.println("Cloud: " + jsonObject.get("cloud"));
                    System.out.println("Dew Point: " + jsonObject.get("dewpt"));
                    System.out.println("Pressure: " + jsonObject.get("press"));
                    System.out.println("Relative Humidity: " + jsonObject.get("rel_hum"));
                    System.out.println("Wind Direction: " + jsonObject.get("wind_dir"));
                    System.out.println("Wind Speed (kmh): " + jsonObject.get("wind_spd_kmh"));
                    System.out.println("Wind Speed (kt): " + jsonObject.get("wind_spd_kt"));
                    System.out.println();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Close streams
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
