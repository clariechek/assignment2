package com.weatherapp;

/*
 * Manages and stores weather information, sends clients weather data, and accepts weather updates from content servers.
 * Stores weather information persistently, only removing it when the content server who provided it is no longer in contact, 
 * or when the weather data is too old (not one of the most recent 20 updates).
 */

import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AggregationServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;
    private List<Integer> connectedContentServers = new ArrayList<Integer>();
    private HashMap<Integer, JSONObject> weatherData = new HashMap<Integer, JSONObject>();
    private LamportClock lamportClock = null;

    public AggregationServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            
            // Initialise pid allocator to 1 (pid 0 belongs to Aggregation Server)
            checkIfFileExists("pid.txt");
            FileWriter file = new FileWriter("pid.txt");
            file.write("1");
            file.close();

            // Initialise lamport clock
            lamportClock = new LamportClock(0);
            checkIfFileExists("LC_AS.txt");
            file = new FileWriter("LC_AS.txt");
            file.write("0");
            file.close();

            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
        // Delete pid allocator
        File file = new File("pid.txt");
        file.delete();
        this.interrupt();
    }

    public void checkIfFileExists(String path) throws IOException {
        File file = new File(path);
        boolean fileExists = file.exists();
        if (!fileExists) {
            file.createNewFile();
            System.out.println("File created " + path);
        } else {
            System.out.println("File already exists");
        }
        // return fileExists;
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
                RequestHandler requestHandler = new RequestHandler(socket, this.connectedContentServers, this.weatherData);
                requestHandler.start();
                // ContentServerAndWeatherData contentServerAndWeatherData = requestHandler.getConnectedContentServerAndWeatherData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int portNumber = 4567;
        if (args.length != 0) {
            portNumber = Integer.parseInt(args[0]);
        }
        
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
    }
 }

 class RequestHandler extends Thread {
    private Socket socket;
    private List<Integer> connectedContentServers = new ArrayList<Integer>();
    private HashMap<Integer, JSONObject> weatherData = new HashMap<Integer, JSONObject>();

    RequestHandler(Socket socket, List<Integer> connectedContentServers, HashMap<Integer, JSONObject> weatherData) {
        this.socket = socket;
        this.connectedContentServers = connectedContentServers;
        this.weatherData = weatherData;
    }

    public void checkIfFileExists(String path) throws IOException {
        File file = new File(path);
        boolean fileExists = file.exists();
        if (!fileExists) {
            file.createNewFile();
            System.out.println("File created " + path);
        } else {
            System.out.println("File already exists");
        }
        // return fileExists;
    }

    // Method that parses each entry in fileName into a JSON object
    private static JSONObject loadFromFile(String fileName) {
        // Create a parser
        JSONParser parser = new JSONParser();
        JSONObject jsonObjects = null;

        // Parse the file
        try {
            jsonObjects = (JSONObject) parser.parse(new FileReader(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObjects;
    }

    private void addConnectedContentServer(int contentServerId) {
        connectedContentServers.add(contentServerId);
    }

    private void removeConnectedContentServer(int contentServerId) {
        connectedContentServers.remove(contentServerId);
    }

    @Override
    public void run() {
        String cs_file = null;
        int contentServerId = -1;
        int stationID = -1;

        try {
            System.out.println("Received a connection");

            // Get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // OutputStream out = socket.getOutputStream();

            // Write out header to the client
            out.println("Aggregation Server 1.0");
            out.flush();

            boolean isHeaderLine = true, foundID = false, invalidJsonFormat = false, getReq = false, putReq = false, invalidReq = false, firstConnection = true;
            JSONObject jsonObject = null;
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Handle request header
                if (isHeaderLine) {
                    if (inputLine.contains("GET")) {
                        getReq = true;
                        System.out.println(inputLine);

                        // Get content server id from request
                        inputLine = in.readLine();
                        System.out.println(inputLine);
                        String[] splitInputLine = inputLine.split(":");
                        stationID = Integer.parseInt(splitInputLine[1]);
                    } else if (inputLine.contains("PUT")) {
                        putReq = true;
                        System.out.println(inputLine);
                        while ((inputLine = in.readLine()) != null) {
                            System.out.println(inputLine);
                            if (inputLine.contains("Content-Length:")) {
                                inputLine = in.readLine();
                                System.out.println(inputLine);
                                break;
                            }
                        }
                    } else {
                        invalidReq = true;
                        break;
                    }
                    isHeaderLine = false;
                    continue;
                }

                // Handle PUT request
                if (putReq) {
                    // Print out the content of the request
                    System.out.println(inputLine);

                    // Parse request into JSON object
                    JSONParser parser = new JSONParser();
                    try {
                        jsonObject = (JSONObject) parser.parse(inputLine);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Check if JSON object is valid and contains content server id.
                    String cs_id = jsonObject.get("content_server_id").toString();
                    if (cs_id != null) {
                        // If valid JSON, create intermediate file to store data in case crash occurs.
                        foundID = true;
                        cs_file = "cs_" + cs_id + ".json";
                        checkIfFileExists(cs_file);
                        FileWriter file = new FileWriter(cs_file);
                        file.write(jsonObject.toJSONString());
                        file.close();
                        contentServerId = Integer.parseInt(cs_id);
                    } else {
                        // Otherwise, reject feed with no id or invalid JSON format as error.
                        foundID = false;
                        break;
                    }
                    
                    // Check if this is the content server's first connection
                    if (connectedContentServers.contains(contentServerId)) {
                        // Content server already connected
                        firstConnection = false;
                    } else {
                        // Content server not connected
                        firstConnection = true;
                        addConnectedContentServer(contentServerId);
                    }
                }

                // Reply content server with HTTP response
                if (getReq) {
                    System.out.println(inputLine);
                    out.println("HTTP/1.1 200 OK");

                    // Return current JSON data
                    out.println(weatherData.get(stationID).toJSONString());
                } else if (putReq && foundID) {
                    if (firstConnection) {
                        out.println("HTTP/1.1 201 HTTP Created");
                    } else {
                        out.println("HTTP/1.1 200 OK");
                    }
                } else if (putReq && !foundID || putReq && invalidJsonFormat){
                    // Reject feed with no id or invalid JSON format as error
                    out.println("HTTP/1.1 500 Internal server error");
                } else if (invalidReq) {
                    out.println("HTTP/1.1 400 Bad request");
                } else if (!putReq && !getReq && !invalidReq) {
                    out.println("HTTP/1.1 204 No content");
                }
                // out.println("HTTP/1.1 200 OK\r\n");
                // out.println("\r\n");
                // out.println("\r\n");

                // Close the connection if it is a GET request
                if (getReq) {
                    break;
                }

                // Parse intermediate file into JSON Array
                JSONObject newContentServerData = loadFromFile(cs_file);
                
                // Update weather.json file and delete intermediate file
                try {
                    checkIfFileExists("weather.json");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // Check if weather.json is empty
                JSONArray newWeatherJsonData = new JSONArray();
                File weatherJsonFile = new File("weather.json");
                if (weatherJsonFile.length() == 0) {
                    // Add new data to new JSON Array
                    newWeatherJsonData.add(newContentServerData);
                    
                } else {
                    JSONParser parser = new JSONParser();
                    JSONArray currentWeatherJsonFile = null;

                    try {
                        currentWeatherJsonFile = (JSONArray) parser.parse(new FileReader("weather.json"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // JSONArray weatherJson = loadFromFile("weather.json");

                    // Add existing data to new JSON Array except for the one with the same content server id if there is one.
                    Iterator i = currentWeatherJsonFile.iterator();
                    for (Object o : currentWeatherJsonFile) {
                        i.next();
                        jsonObject = (JSONObject) o;
                        if (!(jsonObject.get("content_server_id")).equals(contentServerId)) {
                        newWeatherJsonData.add(jsonObject);
                        }
                    }
                    // Add new data to new JSON Array
                    newWeatherJsonData.add(newContentServerData);
                }

                // Delete intermediate file
                File file = new File(cs_file);
                file.delete();

                // Update weather.json with new data
                try {
                    FileWriter newWeatherJsonFile = new FileWriter("weather.json");
                    newWeatherJsonFile.write(newWeatherJsonData.toJSONString());
                    newWeatherJsonFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Update weather data variable
                if (weatherData.containsKey(contentServerId)) {
                    weatherData.remove(contentServerId);
                    weatherData.put(contentServerId, newContentServerData);
                } else {
                    weatherData.put(contentServerId, newContentServerData);
                }

                // TODO: Update server replica
            }
            // Close the connection
            in.close();
            out.close();
            socket.close();

            System.out.println("Connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}