package io.gksoftwaresolutions.medicationreminder.model;

import androidx.annotation.NonNull;

public class Pill {
    private String name;
    private String date;
    private String time;
    private String state;

    public Pill(String name, String date, String time, String state) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.state = state;
    }

    public Pill() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @NonNull
    @Override
    public String toString() {
        return getName() + "," + getDate() + "," + getTime();
    }
}
