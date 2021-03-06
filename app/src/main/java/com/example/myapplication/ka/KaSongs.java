package com.example.myapplication.ka;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * manage and store songs data from query result of KaService
 */
public class KaSongs {
    private JSONArray allSongs;
    private ArrayList<String> validSongs;
    private ArrayList<String> songLinks;
    //flag for the song data array of query result in allSongs JSONArray
    private static final int SINGER_FIELD=0;
    private static final int SONG_FIELD=1;
    private static final int LINK_FIELD=2;
    private static final int NO_USE_FIELD=3;

    /**
     * @param allSongs 'content' field from KaService result
     */
    public KaSongs(JSONArray allSongs){
        this.allSongs=allSongs;
        filterWithValidSongs();
    }

    /**
     * filter allSongs with valid format(containing non NULL link) and add into validSongs and songLinks
     */
    private void filterWithValidSongs(){
        validSongs=new ArrayList<>();
        songLinks=new ArrayList<>();
        for (int i = 0 ; i < allSongs.length(); i++) {
            try {
                JSONArray jsonObject = allSongs.getJSONArray(i);
                if (!jsonObject.getString(LINK_FIELD).equals("NULL")) {
                    validSongs.add(jsonObject.getString(SINGER_FIELD) + " " + jsonObject.getString(SONG_FIELD));
                    songLinks.add(jsonObject.getString(LINK_FIELD));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getValidSongs() {
        return validSongs;
    }

    public ArrayList<String> getSongLinks() {
        return songLinks;
    }
}
