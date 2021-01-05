package com.example.finalproject;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionTracker,GoogleMap.OnMapClickListener{

    private final static int MY_PERMISSION_FINE_LOCATION = 101;
    private ZoomControls zoom;
    private GoogleMap mMap;
    private EditText fromlocation;
    private List<Polyline> polyline = new ArrayList<>();
    private EditText tolocation;
    private Button getdirection;
    private List<Marker> firstmarker = new ArrayList<>();
    private List<Marker> secondmarker = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment directionfragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        directionfragment.getMapAsync(this);
        getdirection = (Button) findViewById(R.id.getdirection);
        fromlocation = findViewById(R.id.fromlocation);
        tolocation = findViewById(R.id.tolocation);
        getdirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        zoom = (ZoomControls) findViewById(R.id.zoominzoomout);
        zoom.setOnZoomOutClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mMap.animateCamera(CameraUpdateFactory.zoomOut());

            }
        });
        zoom.setOnZoomInClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

    }
    private void sendRequest() {
        String fromAddress = fromlocation.getText().toString();
        String toAddress = tolocation.getText().toString();
        if (fromAddress.isEmpty()) {
            Toast.makeText(this, "PLEASE ENTER A FROM ADDRESS", Toast.LENGTH_SHORT).show();
            return;
        }
        if (toAddress.isEmpty()) {
            Toast.makeText(this, "PLEASE ENTER A TO ADDRESS", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new GetDirection(this, fromAddress, toAddress).execute();
        } catch (UnsupportedEncodingException error) {
            error.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        LatLng syracuseU = new LatLng(43.038424, -76.133399);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(syracuseU, 11));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSION_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Permission Required", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        polyline = new ArrayList<>();
        firstmarker = new ArrayList<>();
        secondmarker = new ArrayList<>();
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.start_location, 8));((TextView) findViewById(R.id.totaltime)).setText(route.duration.text);((TextView) findViewById(R.id.totaldistance)).setText(route.distance.text);
            firstmarker.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.start)).title(route.start_address).position(route.start_location)));
            secondmarker.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.end)).title(route.end_address).position(route.end_location)));
            PolylineOptions polylines = new PolylineOptions().geodesic(true).color(Color.BLUE).width(10);
            for (int i = 0; i < route.polyline.size(); i++)
                polylines.add(route.polyline.get(i));

            polyline.add(mMap.addPolyline(polylines));
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(MapsActivity.this,"Your Location: " + latLng.latitude + " and " + latLng.longitude,Toast.LENGTH_LONG).show();
    }
}
