package com.example.tkkil.phuot.Folder;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GetNearbyPlaces extends AsyncTask<Object, String, String> {
    String place;
    private String mGooglePlacesData;
    private GoogleMap mMap;
    private String mUrl;
    private ArrayList<Marker> mMarkers;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        mUrl = (String) objects[1];
        mMarkers = (ArrayList<Marker>) objects[2];
        place = (String) objects[3];

        DownloadUrl downloadUrl = new DownloadUrl();

        try {
            mGooglePlacesData = downloadUrl.readUrl(mUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mGooglePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String, String>> nearbyPlaceList = null;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
        for (int i = 0; i < nearbyPlaceList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            String placeName = googlePlace.get("place_name");
            String vincinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));

            LatLng latLng = new LatLng(lat, lng);

            if (place.equalsIgnoreCase("restaurant")) {
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vincinity);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            }
            if (place.equalsIgnoreCase("gas_station")) {
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vincinity);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

            }
            if (place.equalsIgnoreCase("hotel")) {
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vincinity);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            }

            mMarkers.add(mMap.addMarker(markerOptions));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }
}
