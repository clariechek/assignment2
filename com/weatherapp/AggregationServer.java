package com.weatherapp;

/*
 * Manages and stores weather information, sends clients weather data, and accepts weather updates from content servers.
 * Stores weather information persistently, only removing it when the content server who provided it is no longer in contact, 
 * or when the weather data is too old (not one of the most recent 20 updates).
 */

import java.util.*;
import java.io.*;
// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.PrintWriter;
// import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.simple.ItemList;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
// import org.json.simple.parser.ParseException;
import org.json.simple.parser.Yytoken;

public class AggregationServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;
    private List<Integer> connectedContentServers = new ArrayList<Integer>();
    private HashMap<Integer, JSONArray> weatherData = new HashMap<Integer, JSONArray>();

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

    // Method that parses each entry in fileName into a JSON object and stores them in a JSON array.
    private static JSONArray loadFromFile(String fileName) {
        // Create a parser
        JSONParser parser = new JSONParser();
        JSONArray jsonObjects = null;

        // Parse the file
        try {
            jsonObjects = (JSONArray) parser.parse(new FileReader(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObjects;
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
                RequestHandler requestHandler = new RequestHandler(socket, this.connectedContentServers);
                requestHandler.start();
                ContentServerAndWeatherData contentServerAndWeatherData = requestHandler.getConnectedContentServerAndWeatherData();
                
                if (contentServerAndWeatherData != null) {
                    int contentServerId = contentServerAndWeatherData.getContentServerId();
                    String cs_file = contentServerAndWeatherData.getCsFile();

                    // Update connected content servers
                    connectedContentServers = contentServerAndWeatherData.getConnectedContentServers();

                    // Parse intermediate file into JSON Array
                    JSONArray jsonObjects = loadFromFile(cs_file);

                    // Add JSON Array to weather data
                    if (weatherData.containsKey(contentServerId)) {
                        weatherData.remove(contentServerId);
                        weatherData.put(contentServerId, jsonObjects);
                    } else {
                        weatherData.put(contentServerId, jsonObjects);
                    }

                    // TODO: Update server replica

                    // Store JSON Array into weather data file and delete intermediate file
                    checkIfFileExists("weather.json");
                    JSONArray weatherJson = loadFromFile("weather.json");

                    // Check if contains content server id
                    boolean foundID = false;
                    Iterator i = jsonObjects.iterator();
                    for (Object o : jsonObjects) {
                        i.next();
                        JSONObject jsonObject = (JSONObject) o;

                        if ((jsonObject.get("content_server_id")).equals(contentServerId)) {
                            // If yes, remove it and add new JSON Array
                            weatherJson.remove(contentServerId);
                            weatherJson.add(contentServerId, jsonObjects);
                            foundID = true;
                        }

                        if (!foundID) {
                            // If no, add new JSON Array
                            weatherJson.add(contentServerId, jsonObjects);
                        }
                    }

                    // Delete intermediate file
                    File file = new File(cs_file);
                    file.delete();

                    // Update weather.json with new data
                    Path path = Paths.get("weather.json");
                    try {
                        Files.writeString(path, jsonObjects.toJSONString(), StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        System.out.println("Invalid path: " + path);
                        e.printStackTrace();
                    }
                }
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
    private List<Integer> connectedContentServers = new ArrayList<Integer>();
    private ContentServerAndWeatherData contentServerAndWeatherData;

    RequestHandler(Socket socket, List<Integer> connectedContentServers) {
        this.socket = socket;
        this.connectedContentServers = connectedContentServers;
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

    private void addConnectedContentServer(int contentServerId) {
        connectedContentServers.add(contentServerId);
    }

    private void removeConnectedContentServer(int contentServerId) {
        connectedContentServers.remove(contentServerId);
    }  

    public ContentServerAndWeatherData getConnectedContentServerAndWeatherData() {
        return contentServerAndWeatherData;
    }

    @Override
    public void run() {
        try {
            System.out.println("Received a connection");

            // Get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // OutputStream out = socket.getOutputStream();

            // Write out our header to the client
            out.println("Aggregation Server 1.0");
            out.flush();

            String inputLine, outputLine, cs_file = "";
            // // Initiate conversation with client by writing to the socket
            // WeatherProtocol wp = new WeatherProtocol();
            // // outputLine = wp.processInput(null);
            // // out.println(outputLine);

            boolean isHeaderLine = true, isFirstLine = true, foundID = false, invalidJsonFormat = false, getReq = false, putReq = false, invalidReq = false, startWeatherData = false, firstConnection = true, firstWrite = true;
            int contentServerId = -1;
            int numberOfEntries = -1;
            int index = 0;
            JSONObject jsonObject = null;
            WeatherEntry[] entries = null;
            while ((inputLine = in.readLine()) != null) {
                // Check request type
                if (isHeaderLine) {
                    if (inputLine.contains("GET")) {
                        getReq = true;
                        System.out.println(inputLine);
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

                // Handle put request
                if (putReq) {
                    System.out.println("Data: " + inputLine);
                    JSONParser parser = new JSONParser();
                    try {
                        jsonObject = (JSONObject) parser.parse(inputLine);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String cs_id = jsonObject.get("content_server_id").toString();
                    System.out.println("Content server id:" + cs_id);
                    cs_file = "cs_" + cs_id + ".json";
                    checkIfFileExists(cs_file);
                    FileWriter file = new FileWriter(cs_file);
                    file.write(jsonObject.toJSONString());
                    file.close();
                    contentServerId = Integer.parseInt(cs_id);
                    // System.out.println(inputLine);
                    // if (inputLine.contains("content_server_id")) {
                    //     // Store content server id and start storing weather data
                    //     String splitline = inputLine.split(":")[1];
                    //     contentServerId = Integer.parseInt(splitline.substring(1, splitline.length() - 1));
                    //     // contentServerId = Integer.parseInt(inputLine.substring(19));
                    //     startWeatherData = true;

                    //     // Create intermediate file to store data
                    //     cs_file = "cs_" + contentServerId + ".json";
                    //     checkIfFileExists(cs_file);

                        

                    //     // Write content server id to intermediate file
                    //     // Path path = Paths.get(cs_file);
                    //     // try {
                    //     //     Files.writeString(path, "[" + "\n", StandardCharsets.UTF_8);
                    //     //     Files.writeString(path, "   {" + "\n", StandardCharsets.UTF_8);
                    //     //     Files.writeString(path, "       \"content_server_id\":" + contentServerId + ",\n", StandardCharsets.UTF_8);
                    //     //     Files.writeString(path, "       \"weather_data\":" + "\n", StandardCharsets.UTF_8);
                    //     //     Files.writeString(path, "       [" + "\n", StandardCharsets.UTF_8);
                    //     // } catch (IOException e) {
                    //     //     System.out.println("Invalid path: " + path);
                    //     //     e.printStackTrace();
                    //     // }

                    //     // Check if this is the content server's first connection
                    //     if (connectedContentServers.contains(contentServerId)) {
                    //         // Content server already connected
                    //         firstConnection = false;
                    //     } else {
                    //         // Content server not connected
                    //         firstConnection = true;
                    //         addConnectedContentServer(contentServerId);
                    //     }

                    //     inputLine = in.readLine();
                    //     if (inputLine.contains("number_of_entries")) {
                    //         // Store number of entries and initialise WeatherEntry array
                    //         splitline = inputLine.split(":")[1];
                    //         numberOfEntries = Integer.parseInt(splitline.substring(1, splitline.length() - 1));
                    //         entries = new WeatherEntry[numberOfEntries];
                    //     }
                    //     inputLine = in.readLine();
                    //     continue;
                    // }
                    
                    // if (startWeatherData) {
                    //     if (isFirstLine) {
                    //         // Check if contains id. If yes, then create new WeatherEntry object and add data. Otherwise, return error.
                    //         if (!inputLine.contains("id")) {
                    //             foundID = false;
                    //             break;
                    //         } else {
                    //             entries[index] = new WeatherEntry();
                    //             String[] splitLine = inputLine.split(":");
                    //             String fieldData = splitLine[1];
                    //             entries[index].setId(fieldData);
                    //         }
                    //         isFirstLine = false;
                    //         continue;
                    //     }

                    //     if (inputLine.contains("name")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         String fieldData = splitLine[1];
                    //         entries[index].setName(fieldData);
                    //         // System.out.println("name: " + fieldData);
                    //     } else if (inputLine.contains("state")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         String fieldData = splitLine[1];
                    //         entries[index].setState(fieldData);
                    //         // System.out.println("state: " + fieldData);
                    //     } else if (inputLine.contains("time_zone")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         String fieldData = splitLine[1];
                    //         entries[index].setTime_zone(fieldData);
                    //         // System.out.println("time_zone: " + fieldData);
                    //     } else if (inputLine.contains("lat")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         Double fieldData = Double.parseDouble(splitLine[1]);
                    //         entries[index].setLat(fieldData);
                    //         // System.out.println("lat: " + fieldData);
                    //     } else if (inputLine.contains("lon")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         Double fieldData = Double.parseDouble(splitLine[1]);
                    //         entries[index].setLon(fieldData);
                    //         // System.out.println("lon: " + fieldData);
                    //     } else if (inputLine.contains("local_date_time")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         String fieldData = splitLine[1];
                    //         entries[index].setLocal_date_time(fieldData);
                    //         // System.out.println("local_date_time: " + fieldData);
                    //     } else if (inputLine.contains("local_date_time_full")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         String fieldData = splitLine[1];
                    //         entries[index].setLocal_date_time_full(fieldData);
                    //         // System.out.println("local_date_time_full: " + fieldData);
                    //     } else if (inputLine.contains("air_temp")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         Double fieldData = Double.parseDouble(splitLine[1]);
                    //         entries[index].setAir_temp(fieldData);
                    //         // System.out.println("air_temp: " + fieldData);
                    //     } else if (inputLine.contains("apparent_t")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         Double fieldData = Double.parseDouble(splitLine[1]);
                    //         entries[index].setApparent_t(fieldData);
                    //         // System.out.println("apparent_t: " + fieldData);
                    //     } else if (inputLine.contains("cloud")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         String fieldData = splitLine[1];
                    //         entries[index].setCloud(fieldData);
                    //         // System.out.println("cloud: " + fieldData);
                    //     } else if (inputLine.contains("dewpt")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         Double fieldData = Double.parseDouble(splitLine[1]);
                    //         entries[index].setDewpt(fieldData);
                    //         // System.out.println("dewpt: " + fieldData);
                    //     } else if (inputLine.contains("press")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         Double fieldData = Double.parseDouble(splitLine[1]);
                    //         entries[index].setPress(fieldData);
                    //         // System.out.println("press: " + fieldData);
                    //     } else if (inputLine.contains("rel_hum")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         Long fieldData = Long.parseLong(splitLine[1]);
                    //         entries[index].setRel_hum(fieldData);
                    //         // System.out.println("rel_hum: " + fieldData);
                    //     } else if (inputLine.contains("wind_dir")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         String fieldData = splitLine[1];
                    //         entries[index].setWind_dir(fieldData);
                    //         // System.out.println("wind_dir: " + fieldData);
                    //     } else if (inputLine.contains("wind_spd_kmh")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         Long fieldData = Long.parseLong(splitLine[1]);
                    //         entries[index].setWind_spd_kmh(fieldData);
                    //         // System.out.println("wind_spd_kmh: " + fieldData);
                    //     } else if (inputLine.contains("wind_spd_kt")) {
                    //         String[] splitLine = inputLine.split(":");
                    //         Long fieldData = Long.parseLong(splitLine[1]);
                    //         entries[index].setWind_spd_kt(fieldData);
                    //         // System.out.println("wind_spd_kt: " + fieldData);
            
                    //         // Increment counter and update isFirstLine for next entry
                    //         index++;
                    //         isFirstLine = true;
                    //     } else {
                    //         // Invalid field
                    //         invalidJsonFormat = true;
                    //         break;
                    //     }

                    //     // Create JSON Array from WeatherEntry objects
                    //     JSONArray jsonObjects = new JSONArray();
                    //     for (WeatherEntry entry : entries) {
                    //         if (entry == null) {
                    //             break;
                    //         }

                    //         String id = entry.getId();
                    //         String name = entry.getName();
                    //         String state = entry.getState();
                    //         String time_zone = entry.getTime_zone();
                    //         double lat = entry.getLat();
                    //         double lon = entry.getLon();
                    //         String local_date_time = entry.getLocal_date_time();
                    //         String local_date_time_full = entry.getLocal_date_time_full();
                    //         double air_temp = entry.getAir_temp();
                    //         double apparent_t = entry.getApparent_t();
                    //         String cloud = entry.getCloud();
                    //         double dewpt = entry.getDewpt();
                    //         double press = entry.getPress();
                    //         long rel_hum = entry.getRel_hum();
                    //         String wind_dir = entry.getWind_dir();
                    //         long wind_spd_kmh = entry.getWind_spd_kmh();
                    //         long wind_spd_kt = entry.getWind_spd_kt();

                    //         JSONObject jsonObject = new JSONObject();
                    //         jsonObject.put("id", id);
                    //         jsonObject.put("name", name);
                    //         jsonObject.put("state", state);
                    //         jsonObject.put("time_zone", time_zone);
                    //         jsonObject.put("lat", lat);
                    //         jsonObject.put("lon", lon);
                    //         jsonObject.put("local_date_time", local_date_time);
                    //         jsonObject.put("local_date_time_full", local_date_time_full);
                    //         jsonObject.put("air_temp", air_temp);
                    //         jsonObject.put("apparent_t", apparent_t);
                    //         jsonObject.put("cloud", cloud);
                    //         jsonObject.put("dewpt", dewpt);
                    //         jsonObject.put("press", press);
                    //         jsonObject.put("rel_hum", rel_hum);
                    //         jsonObject.put("wind_dir", wind_dir);
                    //         jsonObject.put("wind_spd_kmh", wind_spd_kmh);
                    //         jsonObject.put("wind_spd_kt", wind_spd_kt);
                    //         jsonObjects.add(jsonObject);
                    //     }

                    //     JSONObject contentServerData = new JSONObject();
                    //     contentServerData.put("content_server_id", contentServerId);
                    //     contentServerData.put("number_of_entries", numberOfEntries);
                    //     contentServerData.put("weather_data", jsonObjects);

                    //     // Write JSON Array to intermediate file
                    //     FileWriter file = new FileWriter(cs_file);
                    //     file.write(contentServerData.toJSONString());
                    //     file.close();


                        // try {
                        //     if (firstWrite) {
                        //         if (inputLine.equals("{") || inputLine.equals("},")) {
                        //             Files.writeString(path, "           " + inputLine + "\n", StandardCharsets.UTF_8);
                        //         } else {
                        //             Files.writeString(path, "               " + inputLine + "\n", StandardCharsets.UTF_8);
                        //         }
                        //     } else {
                        //         if (inputLine.equals("}")) {
                        //             Files.writeString(path, "           " + inputLine + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        //             Files.writeString(path, "       ]" + inputLine + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        //             Files.writeString(path, "   }" + inputLine + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        //             Files.writeString(path, "]" + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        //         } else if (inputLine.equals("{") || inputLine.equals("},")) {
                        //             Files.writeString(path, "           " + inputLine + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        //         } else {
                        //             Files.writeString(path, "               " + inputLine + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        //         }
                        //     }
                        // } catch (IOException e) {
                        //     System.out.println("Invalid path: " + path);
                        //     e.printStackTrace();
                        // }
                        // // Files.write(path, inputLine.getBytes(StandardCharsets.UTF_8));
                    }
                    // // Use WeatherProtocol to parse the input
                    // outputLine = wp.processInput(inputLine);
                    // out.println(outputLine);
                    // if (outputLine.equals("Bye."))
                    //     break;
                    // if (inputLine.equals("}")) {
                    //     break;
                    // }
                // }
            }

            contentServerAndWeatherData = new ContentServerAndWeatherData(connectedContentServers, cs_file, contentServerId);

            // TODO: Status 201 HTTP Created
            if (putReq && foundID) {
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

 class ContentServerAndWeatherData {
    private List<Integer> connectedContentServers;
    // private HashMap<int, WeatherData> weatherData;
    String cs_file;
    int contentServerId;

    public ContentServerAndWeatherData(List<Integer> connectedContentServers, String cs_file, int contentServerId) {
        this.connectedContentServers = connectedContentServers;
        // this.weatherData = weatherData;
        this.cs_file = cs_file;
        this.contentServerId = contentServerId;
    }
    
    public List<Integer> getConnectedContentServers() {
        return this.connectedContentServers;
    }
    
    // public HashMap<int, WeatherData> getWeatherData() {
        //     return this.weatherData;
        // }
        
    public String getCsFile() {
        return this.cs_file;
    }
    
    public int getContentServerId() {
        return this.contentServerId;
    }
}