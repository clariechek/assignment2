package com.weatherapp;

/*
 * Reads weather data from a local file, converts it to JSON format, and makes a HTTP PUT request to the 
 * aggregation server to upload new weather data to the aggregation server.
 */

// import org.json.JSONTokener;
// import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
// import org.json.CDL;
// import org.json.Cookie;
// import org.json.CookieList;
// import org.json.HTTP;
// import org.json.HTTPTokener;
// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONML;
// import org.json.JSONObject;
// import org.json.JSONString;
// import org.json.JSONStringer;
// import org.json.JSONTokener;
// import org.json.JSONWriter;
// import org.json.XML;
// import org.json.XMLTokener;

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
// import net.sf.json.JSONSerializer;
// import org.apache.commons.io.IOUtils; 

public class ContentServer {
    private int id;

    private ContentServer() {}

    public void setContentServerID(int id) {
        this.id = id;
    }

    public int getContentServerID() {
        return this.id;
    }

    // Method that counts the number of entries in the file.
    private static int countNumberOfEntries(String fileName) {
        int count = 0;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("id:")) {
                    count++;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // Method that parses each entry in fileName into a JSON object and stores them in a JSON array.
    private static JSONObject loadFromFile(String fileName, int content_server_id) throws Exception{
        // Read from txt file
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);

        // Create WeatherEntry objects from file contents
        String line;
        int numberOfEntries = countNumberOfEntries(fileName);
        if (numberOfEntries == 0) {
            System.out.println("Error: Invalid file format. Missing id field.");
            return null;
        }
        WeatherEntry[] entries = new WeatherEntry[numberOfEntries];
        boolean isFirstLine = true;
        int index = 0;
        while ((line = br.readLine()) != null) {
            if (isFirstLine) {
                // Check if contains id. If yes, then create new WeatherEntry object and add data. Otherwise, return error.
                if (!line.contains("id")) {
                    System.out.println("Error: Invalid file format. Missing id field.");
                    return null;
                } else {
                    entries[index] = new WeatherEntry();
                    String[] splitLine = line.split(":");
                    String fieldData = splitLine[1];
                    entries[index].setId(fieldData);
                    // System.out.println("id: " + fieldData);
                }
                isFirstLine = false;
                continue;
            }

            // Add field data to weatherEntry object
            if (line.contains("name")) {
                String[] splitLine = line.split(":");
                String fieldData = splitLine[1];
                entries[index].setName(fieldData);
                // System.out.println("name: " + fieldData);
            } else if (line.contains("state")) {
                String[] splitLine = line.split(":");
                String fieldData = splitLine[1];
                entries[index].setState(fieldData);
                // System.out.println("state: " + fieldData);
            } else if (line.contains("time_zone")) {
                String[] splitLine = line.split(":");
                String fieldData = splitLine[1];
                entries[index].setTime_zone(fieldData);
                // System.out.println("time_zone: " + fieldData);
            } else if (line.contains("lat")) {
                String[] splitLine = line.split(":");
                Double fieldData = Double.parseDouble(splitLine[1]);
                entries[index].setLat(fieldData);
                // System.out.println("lat: " + fieldData);
            } else if (line.contains("lon")) {
                String[] splitLine = line.split(":");
                Double fieldData = Double.parseDouble(splitLine[1]);
                entries[index].setLon(fieldData);
                // System.out.println("lon: " + fieldData);
            } else if (line.contains("local_date_time")) {
                String[] splitLine = line.split(":");
                String fieldData = splitLine[1];
                entries[index].setLocal_date_time(fieldData);
                // System.out.println("local_date_time: " + fieldData);
            } else if (line.contains("local_date_time_full")) {
                String[] splitLine = line.split(":");
                String fieldData = splitLine[1];
                entries[index].setLocal_date_time_full(fieldData);
                // System.out.println("local_date_time_full: " + fieldData);
            } else if (line.contains("air_temp")) {
                String[] splitLine = line.split(":");
                Double fieldData = Double.parseDouble(splitLine[1]);
                entries[index].setAir_temp(fieldData);
                // System.out.println("air_temp: " + fieldData);
            } else if (line.contains("apparent_t")) {
                String[] splitLine = line.split(":");
                Double fieldData = Double.parseDouble(splitLine[1]);
                entries[index].setApparent_t(fieldData);
                // System.out.println("apparent_t: " + fieldData);
            } else if (line.contains("cloud")) {
                String[] splitLine = line.split(":");
                String fieldData = splitLine[1];
                entries[index].setCloud(fieldData);
                // System.out.println("cloud: " + fieldData);
            } else if (line.contains("dewpt")) {
                String[] splitLine = line.split(":");
                Double fieldData = Double.parseDouble(splitLine[1]);
                entries[index].setDewpt(fieldData);
                // System.out.println("dewpt: " + fieldData);
            } else if (line.contains("press")) {
                String[] splitLine = line.split(":");
                Double fieldData = Double.parseDouble(splitLine[1]);
                entries[index].setPress(fieldData);
                // System.out.println("press: " + fieldData);
            } else if (line.contains("rel_hum")) {
                String[] splitLine = line.split(":");
                Long fieldData = Long.parseLong(splitLine[1]);
                entries[index].setRel_hum(fieldData);
                // System.out.println("rel_hum: " + fieldData);
            } else if (line.contains("wind_dir")) {
                String[] splitLine = line.split(":");
                String fieldData = splitLine[1];
                entries[index].setWind_dir(fieldData);
                // System.out.println("wind_dir: " + fieldData);
            } else if (line.contains("wind_spd_kmh")) {
                String[] splitLine = line.split(":");
                Long fieldData = Long.parseLong(splitLine[1]);
                entries[index].setWind_spd_kmh(fieldData);
                // System.out.println("wind_spd_kmh: " + fieldData);
            } else if (line.contains("wind_spd_kt")) {
                String[] splitLine = line.split(":");
                Long fieldData = Long.parseLong(splitLine[1]);
                entries[index].setWind_spd_kt(fieldData);
                // System.out.println("wind_spd_kt: " + fieldData);

                // Increment counter and update isFirstLine for next entry
                index++;
                isFirstLine = true;
            } else {
                // Invalid field
                System.out.println("Error: Invalid file format. Invalid field name.");
                return null;
            }
        }
        br.close();

        // Create JSON Array from WeatherEntry objects
        JSONArray jsonObjects = new JSONArray();
        for (WeatherEntry entry : entries) {
            if (entry == null) {
                break;
            }

            String id = entry.getId();
            String name = entry.getName();
            String state = entry.getState();
            String time_zone = entry.getTime_zone();
            double lat = entry.getLat();
            double lon = entry.getLon();
            String local_date_time = entry.getLocal_date_time();
            String local_date_time_full = entry.getLocal_date_time_full();
            double air_temp = entry.getAir_temp();
            double apparent_t = entry.getApparent_t();
            String cloud = entry.getCloud();
            double dewpt = entry.getDewpt();
            double press = entry.getPress();
            long rel_hum = entry.getRel_hum();
            String wind_dir = entry.getWind_dir();
            long wind_spd_kmh = entry.getWind_spd_kmh();
            long wind_spd_kt = entry.getWind_spd_kt();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("state", state);
            jsonObject.put("time_zone", time_zone);
            jsonObject.put("lat", lat);
            jsonObject.put("lon", lon);
            jsonObject.put("local_date_time", local_date_time);
            jsonObject.put("local_date_time_full", local_date_time_full);
            jsonObject.put("air_temp", air_temp);
            jsonObject.put("apparent_t", apparent_t);
            jsonObject.put("cloud", cloud);
            jsonObject.put("dewpt", dewpt);
            jsonObject.put("press", press);
            jsonObject.put("rel_hum", rel_hum);
            jsonObject.put("wind_dir", wind_dir);
            jsonObject.put("wind_spd_kmh", wind_spd_kmh);
            jsonObject.put("wind_spd_kt", wind_spd_kt);
            jsonObjects.add(jsonObject);
        }

        JSONObject contentServerData = new JSONObject();
        contentServerData.put("content_server_id", content_server_id);
        contentServerData.put("number_of_entries", numberOfEntries);
        contentServerData.put("weather_data", jsonObjects);
        
        return contentServerData;

        // // Create a parser
        // JSONParser parser = new JSONParser();
        // JSONArray jsonObjects = null;

        // try {
            
        //     // File file = new File(fileName);
        //     // InputStream is = new FileInputStream(file);
        //     // JSONTokener tokener = new JSONTokener(is);
        //     // jsonObjects = new JSONArray(tokener);
        //     // Parse the file
        //     jsonObjects = (JSONArray) parser.parse(new FileReader(fileName));
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        // return jsonObjects;
    }

    private void sendPutRequest(String serverName, int portNumber, JSONObject data) {
        try (
            // Open a socket
            Socket socket = new Socket(serverName, portNumber);

            // Open an input stream and output stream to the socket
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            JSONArray weatherData = (JSONArray) data.get("weather_data");
            // PUT request header
            String request = "PUT /weather.json HTTP/1.1\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: " + weatherData.size() + "\r\n\r\n";

            // Add the Content Server Data to the request
            request += data.toJSONString();
            
            // Send PUT request to Aggregation server
            out.println(request);
            out.flush();

            // Check server response
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println("Server: " + serverResponse);
                // if (serverResponse.equals("201 - HTTP_CREATED.") && !this.connectedToAggregationServer) {
                //     System.out.println("PUT request successful. First weather data received and storage file created.");
                //     this.connectedToAggregationServer = true;
                // } else if (serverResponse.equals("201 - HTTP_CREATED.") && this.connectedToAggregationServer) {
                //     System.out.println("Server returned wrong response. Expected 200 - HTTP_OK but received 201 - HTTP_CREATED.");
                // } else if (serverResponse.equals("200 - HTTP_OK.") && this.connectedToAggregationServer) {
                //     System.out.println("PUT request successful. Updated weather data.");
                // } else if (serverResponse.equals("200 - HTTP_OK.") && !this.connectedToAggregationServer) {
                //     System.out.println("Server returned wrong response. Expected 201 - HTTP_CREATED but received 200 - HTTP_OK.");
                // } else {
                //     System.out.println("PUT request failed.");
                // }
            }

            // Close buffer and writer
            in.close();
            out.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public static void main(String args[]) throws Exception{
        // Create content server
        ContentServer contentServer = new ContentServer();
        
        // Get server name and port number from "http://servername.domain.domain:portnumber", and station ID.
        URI uri;
        String serverName = "localhost";
        int portNumber = 4567;
        try {
            uri = new URI(args[0]);
            // URL url = uri.toURL();
            serverName = uri.getHost();
            System.out.println("Server name: " + uri.getHost() + " Port number: " + uri.getPort() + " Station ID: " + args[1] + " File name: " + args[2]);
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
        
        // String serverName = args[0];
        // int portNumber = Integer.parseInt(args[1]);

        // Assign unique ID to content server
        int stationID = Integer.parseInt(args[1]);
        contentServer.setContentServerID(stationID);
        // System.out.println("Connecting to " + serverName + " on port " + portNumber + "...");
        // System.out.println("File name: " + args[2]);

        // Parse file and store in JSON array
        JSONObject data = loadFromFile(args[2], stationID);
        // ContentServerData data = loadFromFile(args[2], contentServer.id);

        // Connect to aggregation server and send PUT request to aggregation server
        contentServer.sendPutRequest(serverName, portNumber, data);
    }
}