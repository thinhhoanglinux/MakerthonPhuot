package com.example.tkkil.phuot.Folder;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
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

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        mLatLng = (LatLng) objects[2];

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
        HashMap<String,String> directionList = null;
        DataParser parser = new DataParser();
        directionList = parser.parseDirections(s);
        duration = directionList.get("duration");
        distance = directionList.get("distance");

        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title("duration: " + duration);
        markerOptions.snippet("distance: " + distance);
        mMap.addMarker(markerOptions);
    }
}
