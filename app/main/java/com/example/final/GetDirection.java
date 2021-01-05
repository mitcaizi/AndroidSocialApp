package com.example.finalproject;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GetDirection {
    private static final String json = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String apikey = "AIzaSyCyZUYU_bq5gOfdlb5KL7373AC6y_3sV6s";
    private DirectionTracker tracker;
    private String start_location;
    private String end_location;

    public GetDirection(DirectionTracker listener, String startlocation, String endlocation) {
        this.tracker = listener;
        this.start_location = startlocation;
        this.end_location = endlocation;
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(start_location, "utf-8");
        String urlDestination = URLEncoder.encode(end_location, "utf-8");

        return json + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + apikey;
    }

    public void execute() throws UnsupportedEncodingException {
        new DownloadRawData().execute(createUrl());
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream inputStream = url.openConnection().getInputStream();
                StringBuffer stringbuffer = new StringBuffer();
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = bufferedreader.readLine()) != null) {
                    stringbuffer.append(line + "\n");
                }

                return stringbuffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                parsejson(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parsejson(String data) throws JSONException {
        if (data == null)
            return;

        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");
            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.end_address = jsonLeg.getString("end_address");
            route.start_address = jsonLeg.getString("start_address");
            route.start_location = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.end_location = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.polyline = decodePolyLine(overview_polylineJson.getString("points"));

            routes.add(route);
        }

        tracker.onDirectionFinderSuccess(routes);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int length = poly.length();
        int index = 0;
        List<LatLng> polyline = new ArrayList<LatLng>();
        int latitude = 0;
        int longitude = 0;

        while (index < length) {
            int a;
            int shift = 0;
            int res = 0;
            do {
                a = poly.charAt(index++) - 63;
                res |= (a & 0x1f) << shift;
                shift += 5;
            } while (a >= 0x20);
            int dlat = ((res & 1) != 0 ? ~(res >> 1) : (res >> 1));
            latitude += dlat;

            shift = 0;
            res = 0;
            do {
                a = poly.charAt(index++) - 63;
                res |= (a & 0x1f) << shift;
                shift += 5;
            } while (a >= 0x20);
            int position = ((res & 1) != 0 ? ~(res >> 1) : (res >> 1));
            longitude += position;

            polyline.add(new LatLng(
                    latitude / 100000d, longitude / 100000d
            ));
        }

        return polyline;
    }


}
