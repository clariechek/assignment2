package com.weatherapp;

/*
 * Protocol that the GETclient and Aggregation server use to communicate.
 * This class keeps tracks of where the client and server are in their conversation and serves up the server's response to the client's statements.
 */

public class WeatherProtocol {
    // private static final int WAITING = 0;
    // private static final int CS_CONNECTED = 1;
    // private static final int RECEIVED_REQUEST = 2;
    // private static final int CS_UPDATE_SUCCESS = 3;
    // private static final int INVALID_REQUEST = 4;
    // private static final int CS_NO_CONTENT = 5;
    // // private static final int CS_INVALID_CONTENT = 6;

    // private int state = WAITING;

    public String processInput(String theInput) {
        String theOutput = null;

        System.out.println("Processing input: " + theInput);
        if (theInput.equals("PUT /weather.json HTTP/1.1")) {
            theOutput = "HTTP/1.1 200 OK\r\n" + theInput;
        } else {
            theOutput = theInput;
        }
        // } else if (theInput.equals("User-Agent: ATOMClient/1/0") || theInput.equals("Content-Type: application/json") || theInput.contains("Content-Length:")) {
        //     theOutput = theInput;
        // } else {
        //     theOutput = "Bye.";
        // }

        
        // if (state == WAITING) {
        //     theOutput = "201 - HTTP_CREATED";
        //     state = CS_CONNECTED;
        // } else if (state == CS_CONNECTED || state == CS_UPDATE_SUCCESS) {
        //     if (theInput.equals("GET")) {
        //         theOutput = "200 - HTTP_OK";
        //         state = RECEIVED_REQUEST;
        //     } else if (theInput.equals("PUT")) {
        //         theOutput = "200 - HTTP_OK";
        //         state = RECEIVED_REQUEST;
        //     } else {
        //         theOutput = "400 - HTTP_BAD_REQUEST";
        //         state = INVALID_REQUEST;
        //     }
        // } else if (state == RECEIVED_REQUEST) {
        //     if (theInput == null || theInput.isEmpty()) {
        //         theOutput = "204 - HTTP_NO_CONTENT";
        //         state = CS_NO_CONTENT;
        //     } else {
        //         theOutput = "200 - HTTP_OK";
        //         state = CS_UPDATE_SUCCESS;
        //     }
        //     // else if (checkJSONInput(theInput) == false) {
        //     //     theOutput = "500 - HTTP_INTERNAL_SERVER_ERROR";
        //     //     state = CS_INVALID_CONTENT;
        //     // } 
        // }
        
        return theOutput;
    }
}
 