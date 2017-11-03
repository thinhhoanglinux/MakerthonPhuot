package com.example.tkkil.phuot.Folder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anhho on 31/10/2017.
 */

public class DataParser {

    private HashMap<String,String> getPlace(JSONObject googlePlaceJson){
        HashMap<String,String> googlePlacesMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String lat = "";
        String lng = "";
        String ref = "";

        try {
            if (!googlePlaceJson.isNull("name")){
                placeName = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("vicinity")){
                vicinity = googlePlaceJson.getString("vicinity");
            }
            lat = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            lng = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            ref = googlePlaceJson.getString("reference");

            googlePlacesMap.put("place_name", placeName);
            googlePlacesMap.put("vicinity", vicinity);
            googlePlacesMap.put("lat", lat);
            googlePlacesMap.put("lng", lng);
            googlePlacesMap.put("reference", ref);

        }catch (Exception exc){

        }

        return googlePlacesMap;

    }

    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray){
        int count = jsonArray.length();
        List<HashMap<String,String>> placesList = new ArrayList<>();
        HashMap<String,String> placeMap = null;

        for(int i =0; i<count; i++){
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    public List<HashMap<String,String>> parse(String jsonData){
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }

    public HashMap<String,String> parseDirections(String jsonData){
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray(uk"routes").getJSONObject(0).getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getDuration(jsonArray);
    }

    private HashMap<String,String> getDuration(JSONArray jsonArray) {
        HashMap<String,String> googleDirectionsMap = new HashMap<>();
        String duration = "";
        String distance = "";

        try {
            duration = jsonArray.getJSONObject(0).getJSONObject("duration").getString("text");
            distance = jsonArray.getJSONObject(0).getJSONObject("distance").getString("text");

            googleDirectionsMap.put("duration", duration);
            googleDirectionsMap.put("distance", distance);
        }catch (Exception exc){

        }
        return googleDirectionsMap;
    }
}
