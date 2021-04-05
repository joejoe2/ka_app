package com.example.myapplication.ka;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  manage and store all data from query result of KaService
 */
public class KaResponse {
    private String rawResponse;
    private String status;
    private KaSongs songs;

    /**
     * @param rawResponse the json string from KaService result
     * @throws JSONException
     */
    public KaResponse(String rawResponse) throws JSONException {
        this.rawResponse = rawResponse;

        JSONObject json = new JSONObject(rawResponse);
        status = json.getString("status");
        songs=new KaSongs(json.getJSONArray("content"));
    }

    public String getStatus() {
        return status;
    }

    public KaSongs getSongs() {
        return songs;
    }
}
