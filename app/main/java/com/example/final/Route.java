package com.example.finalproject;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class Route {
    public Distance distance;
    public Duration duration;
    public String end_address;
    public LatLng end_location;
    public String start_address;
    public LatLng start_location;
    public List<LatLng> polyline;
}