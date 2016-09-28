package com.cms.demo.app;

/**
 * Created by NoName on 5/14/2016.
 */

public class StoreTime {
    private String time,location_name,message;
    int i=0;

    public StoreTime(String time, String location_name,String message) {

        this.time =time;
        this.location_name=location_name;
        this.message=message;
    }

    public String getTitle() {

        return time;

    }

    public String getMessage(){
        return message;
    }

    public String getLocationName(){
        return location_name;
    }

    public void setLocation(String location_name){
        this.location_name=location_name;

    }
}
