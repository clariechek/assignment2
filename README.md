# Distributed Weather Client/Server System
A client/server weather system that aggregates and distributes weather data in JSON format using a RESTful API, and JUnit Jupiter framework for automated testing.

Note: Currently, the following has not been done yet, but will be completed:
1. Aggregation server tracks duration content server has connected and removes corresponding data after 30 seconds.
2. Synchronise aggregation server and content server using lamport clocks.
3. Testing
4. HTTP - currently not working when I type "http://localhost:4567" in my browser after starting the aggregation server.
5. Makefile


The components of the system are:
1. **Aggregation Server** - The aggregation server does the following:
- The aggregation server reads the port number the server runs on. If this is not given, it will run on port 4567 by default.
- Upon initialisation, it checks if the pid allocator file ("pid.txt") exists. If it doesn't exist, then it creates the file and inputs the value 1 to it.
- Then it creates a LamportClock and allocates the process id and time of the LamportClock to 0. It also creates a LC_AS.txt file that stores the time value of its lamport clock in case it crashes prematurely.
- Finally, it starts up, and listens for connection.
- The Aggregation Server contains a queue that will consists of the list of PUT requests from the content servers. The queue will always be of size 20, hence when a new entry is added, the oldest entry is removed.
- To remove an "expired" entry, the Aggregation Server looks up the content server id, lamport time, and process id, of the corresponding entry in its database and removes it.
- When it receives a PUT request, it stores the content server id, lamport time, process id, and weather data into an intermediate .json file. Then, the information in the file is parse into a JSON object and stored in its WeatherData variable, and weather.json file.
- Upon receiving the PUT request, it uses the getMessage(int sent_time) method to update its LamportClock. sent_time is the lamport time the PUT request was sent by the content server.
- Upon termination, the aggregation server deletes the pid allocator file ("pid.txt).

2. **Content Server**
- The content server reads the URL containing the server name and port number, the content server's id, and the file name of the local content server data.
- Upon initialisation, the content server will initialise the lamport clock. It checks if the LC_CS{content_server_id}.txt file exists to obtain its lamport clock time.
- The content server reads from its .txt file and parses the data into JSON format. Then, it makes a PUT request to the aggregation server and sends the content server id, lamport clock time, process id and JSON data.
- Upon completion, it terminates.

3. **GET Client**
- The get client reads the URL containing the server name and port number, and the id of the content server it wants data from.
- It sends a GET request to the aggregation server and receives JSON data which it displays as normal text.

4. **LamportClock**
- The LamportClock class contains the lamport clock time, and process id. The aggregation server, and each instance of the content server has a LamportClock.
- A unique process id is allocated to each content server. The process id for the aggregation server is always 0.

5. **WeatherEntry**
- The WeatherEntry class contains the fields of each data entry such as ID, name, state, time_zone, lat, lon, etc.


## Compilation
1. In the project folder, navigate to the `weatherapp` directory using the following command:
```
cd com/weatherapp/
```

2. Open terminal and run the following command to compile:
```
javac -cp json-simple-1.1.jar WeatherEntry.java LamportClock.java AggregationServer.java ContentServer.java GETClient.java
```

*Note: I will enable the compilation in the makefile later.

## Run the Application
1. In the `weatherapp` directory, run the instruction below to start the Aggregation Server on port 4567
```
java -cp .:com/weatherapp/json-simple-1.1.jar com.weatherapp.AggregationServer 4567
```
The default port the Aggregation Server runs on is 4567.

2. Next, start the Content Server using the following command. To run multiple content servers parallelly, run the command below in each terminal for each content server. 
```
java -p .:com/weatherapp/json-simple-1.1.jar com.weatherapp.ContentServer http://localhost:4567 0 cs1_1.txt
```
In the above command, 0 is the content server id, and cs1_1.txt is the corresponding local content server file.

3. Finally, run the GET Client using the command below. To run multiple clients parallelly, run the command below in each terminal for each client. 
```
java -p .:com/weatherapp/json-simple-1.1.jar com.weatherapp.GETClient http://localhost:4567 0
```
In the above command, 0 is the content server id you wish to get data from.

## Testing
I will be writing automated unit tests for functionality, regressive testing, and testing harness for multiple distributed entities, edge cases and synchronization.
