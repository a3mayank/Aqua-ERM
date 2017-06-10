package com.mayankattri.aqua;

import java.io.Serializable;

/**
 * Created by mayank on 29/5/17.
 */

public class Trip implements Serializable{

    private String id;
    private String name;
    private String vehicle;
    private String date = null;
    private String time = null;

    public Trip(String id, String name, String vehicle) {
        this.id = id;
        this.name = name;
        this.vehicle = vehicle;
    }

    public Trip(String id, String name, String vehicle, String time) {
        this.id = id;
        this.name = name;
        this.vehicle = vehicle;
        this.time = time;
    }

    public Trip(String id, String name, String vehicle, String date, String time) {
        this.id = id;
        this.name = name;
        this.vehicle = vehicle;
        this.date = date;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String Date) {
        this.date = date;
    }
}
