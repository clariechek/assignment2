package com.weatherapp;

public class RequestInformation {
    private int contentServerId;
    private int lamportClockTime;
    private int process_id;

    public RequestInformation(int contentServerId, int lamportClockTime, int process_id) {
        this.contentServerId = contentServerId;
        this.lamportClockTime = lamportClockTime;
        this.process_id = process_id;
    }

    public int getContentServerId() {
        return contentServerId;
    }

    public int getLamportClockTime() {
        return lamportClockTime;
    }

    public int getProcess_id() {
        return process_id;
    }
}