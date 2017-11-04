package com.example.tkkil.phuot.Folder;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by anhho on 31/10/2017.
 */

public class DirectionData extends AsyncTask<Object, String, String> {

    private GoogleMap mMap;
    private String url;
    private String googleDirectionData;
    private String duration, distance;
    private LatLng mLatLng;
    private  ArrayList<Marker> mMarker;
    private ArrayList<Polyline> mPolylines;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        mLatLng = (LatLng) objects[2];
        mPolylines = (ArrayList<Polyline>) objects[3];
        mMarker = (ArrayList<Marker>) objects[4];

        DownloadUrl downloadUrl = new DownloadUrl();

        try {
            googleDirectionData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDirectionData;
    }

    @Override
    protected void onPostExecute(String s) {
        String[] directionList;
        HashMap<String,String> durationList = null;

        DataParser parser = new DataParser();
        DistanceData distanceData = new DistanceData();

        directionList = parser.parseDirections(s);

        durationList = distanceData.parseDirections(s);
        displayDirection(directionList);

        duration = durationList.get("duration");
        distance = durationList.get("distance");

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title(distance);
        markerOptions.snippet(duration);
        mMarker.add(mMap.addMarker(markerOptions));
        mMarker.get(0).showInfoWindow();
    }

    public void displayDirection(String[] directionList){
        int count = directionList.length;
        for(int i =0; i<count; i++){
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            polylineOptions.width(10);
            polylineOptions.addAll(PolyUtil.decode(directionList[i]));
            mPolylines.add(mMap.addPolyline(polylineOptions));
        }
    }
}
