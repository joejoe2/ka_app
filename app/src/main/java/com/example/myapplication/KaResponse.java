package com.example.myapplication;

import org.json.JSONException;
import org.json.JSONObject;

public class KaResponse {
    private String rawResponse;
    private String status;
    private KaSongs songs;

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
