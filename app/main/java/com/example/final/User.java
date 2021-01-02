package com.example.lab_7;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class User {

    public String displayname;
    public String email;
    public String phone;
    public String profilePicture;
    public Object timestamp;
    public Map<String, Boolean> follower=new HashMap<>();
    public User(String displayname, String email, String phone){
        this.displayname=displayname;
        this.email=email;
        this.phone=phone;
        this.timestamp= ServerValue.TIMESTAMP;
    }
    public Object getTimestamp(){
        return timestamp;

    }
    public User(){


    }
}