package com.weatherapp;

public class WeatherEntry {
    private String id;
    private String name;
    private String state;
    private String time_zone;
    private Double lat;
    private Double lon;
    private String local_date_time;
    private String local_date_time_full;
    private Double air_temp;
    private Double apparent_t;
    private String cloud;
    private Double dewpt;
    private Double press;
    private Long rel_hum;
    private String wind_dir;
    private Long wind_spd_kmh;
    private Long wind_spd_kt;

    public String setId(String id) {
        this.id = id;
        return id;
    }

    public String setName(String name) {
        this.name = name;
        return name;
    }

    public String setState(String state) {
        this.state = state;
        return state;
    }

    public String setTime_zone(String time_zone) {
        this.time_zone = time_zone;
        return time_zone;
    }

    public Double setLat(Double lat) {
        this.lat = lat;
        return lat;
    }

    public Double setLon(Double lon) {
        this.lon = lon;
        return lon;
    }

    public String setLocal_date_time(String local_date_time) {
        this.local_date_time = local_date_time;
        return local_date_time;
    }

    public String setLocal_date_time_full(String local_date_time_full) {
        this.local_date_time_full = local_date_time_full;
        return local_date_time_full;
    }

    public Double setAir_temp(Double air_temp) {
        this.air_temp = air_temp;
        return air_temp;
    }

    public Double setApparent_t(Double apparent_t) {
        this.apparent_t = apparent_t;
        return apparent_t;
    }

    public String setCloud(String cloud) {
        this.cloud = cloud;
        return cloud;
    }

    public Double setDewpt(Double dewpt) {
        this.dewpt = dewpt;
        return dewpt;
    }

    public Double setPress(Double press) {
        this.press = press;
        return press;
    }

    public Long setRel_hum(Long rel_hum) {
        this.rel_hum = rel_hum;
        return rel_hum;
    }

    public String setWind_dir(String wind_dir) {
        this.wind_dir = wind_dir;
        return wind_dir;
    }

    public Long setWind_spd_kmh(Long wind_spd_kmh) {
        this.wind_spd_kmh = wind_spd_kmh;
        return wind_spd_kmh;
    }

    public Long setWind_spd_kt(Long wind_spd_kt) {
        this.wind_spd_kt = wind_spd_kt;
        return wind_spd_kt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public String getTime_zone() {
        return time_zone;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getLocal_date_time() {
        return local_date_time;
    }

    public String getLocal_date_time_full() {
        return local_date_time_full;
    }

    public Double getAir_temp() {
        return air_temp;
    }

    public Double getApparent_t() {
        return apparent_t;
    }

    public String getCloud() {
        return cloud;
    }

    public Double getDewpt() {
        return dewpt;
    }

    public Double getPress() {
        return press;
    }

    public Long getRel_hum() {
        return rel_hum;
    }

    public String getWind_dir() {
        return wind_dir;
    }

    public Long getWind_spd_kmh() {
        return wind_spd_kmh;
    }

    public Long getWind_spd_kt() {
        return wind_spd_kt;
    }
}