package com.example.tkkil.phuot.Models;

/**
 * Created by tkkil on 31-10-2017.
 */

public class Group {
    private String host;
    private String name;
    private String pass;
    private String time;

    public Group() {
    }

    public Group(String host, String name, String pass, String time) {
        this.host = host;
        this.name = name;
        this.pass = pass;
        this.time = time;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
