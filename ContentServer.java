/*
 * Reads weather data from a local file, converts it to JSON format, and makes a HTTP PUT request to the 
 * aggregation server to upload new weather data to the aggregation server.
 */


// import org.json.JSONTokener;
// import org.json.JSONObject;
import java.io.*;
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
    // Method that parses each entry in fileName into a JSON object and stores them in a JSON array.
    private static JSONArray loadFromFile(String fileName) {
        // Create a parser
        JSONParser parser = new JSONParser();

        try {
            // Parse the file
            JSONArray jsonObjects = (JSONArray) parser.parse(new FileReader(fileName));

            for (Object o : jsonObjects) {
                JSONObject jsonObject = (JSONObject) o;
                String id = (String) jsonObject.get("id");
                System.out.println(id);

                String name = (String) jsonObject.get("name");
                System.out.println(name);

                double lat = (Double) jsonObject.get("lat");
                System.out.println(lat);

                long wind_spd_kt = (Long) jsonObject.get("wind_spd_kt");
                System.out.println(wind_spd_kt);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception{
        // Get server name and port number
        String serverName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // Parse file and store in JSON array
        JSONArray jsonObjects = loadFromFile(args[2]);

        // Connect to aggregation server

        // Send PUT request to aggregation server
    }
    

    // Connect to aggregation server

    // Send PUT request to aggregation server
    // public static void main(String[] args) {
    //     String hostName = args[0];
    //     int portNumber = Integer.parseInt(args[1]);

    //     try (
    //         // Open a socket
    //         Socket socket = new Socket(hostName, portNumber);
    //         System.out.println("Connecting...");

    //         // Open an input stream and output stream to the socket
    //         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    //         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    //         BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

    //     ) {
    //         // Read from and write to the stream according to the server's protocol
    //         String fromServer, fromUser;
    //         while ((fromServer = in.readLine()) != null) {
    //             System.out.println("Server: " + fromServer);
    //             if (fromServer.equals("Bye."))
    //                 break;
                
    //             fromUser = stdIn.readLine();
    //             if (fromUser != null) {
    //                 System.out.println("Client: " + fromUser);
    //                 out.println(fromUser);
    //             }
    //         }
    //         // Close the streams and the sockets
    //     } catch (Exception e) {
    //         System.out.println("Exception: " + e.getMessage());
    //     }
    // }
}
