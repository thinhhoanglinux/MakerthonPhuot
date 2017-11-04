package com.example.tkkil.phuot.Models;

import java.io.Serializable;

public class User implements Serializable {
    private String fullname;
    private String email;
    private String uid;
    private String avatar;
    private String phone;
    private String address;
    private String birthday;
    private Boolean gender;

    public User() {
    }

    public User(String fullname, String email, String uid, String avatar, String phone, String address, String birthday, Boolean gender) {
        this.fullname = fullname;
        this.email = email;
        this.uid = uid;
        this.avatar = avatar;
        this.phone = phone;
        this.address = address;
        this.birthday = birthday;
        this.gender = gender;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }
}
