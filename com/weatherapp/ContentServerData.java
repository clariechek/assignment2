package com.weatherapp;
import org.json.simple.JSONArray;

public class ContentServerData {
    private int content_server_id;
    private int number_of_entries;
    private JSONArray weather_data;

    public ContentServerData(int content_server_id, int number_of_entries, JSONArray weather_data) {
        this.content_server_id = content_server_id;
        this.number_of_entries = number_of_entries;
        this.weather_data = weather_data;
    }

    public int getContentServerId() {
        return content_server_id;
    }

    public int getNumberOfEntries() {
        return number_of_entries;
    }

    public JSONArray getWeatherData() {
        return weather_data;
    }
}