package com.weatherapp;
// Class that contains the content server id, the intermediate file name, and the list of connected content servers to be returned to the AggregationServer.

import java.util.List;

public class ContentServerAndWeatherData {
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