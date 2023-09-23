package com.weatherapp;

public class LamportClock {
    private int time;
    private int process_id;

    public LamportClock(int process_id) {
        this.process_id = process_id;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public void tick() {
        time++;
    }

    public void getMessage(int sent_time) {
        time = Math.max(time, sent_time) + 1;
    }

    public int getProcessId() {
        return process_id;
    }
}