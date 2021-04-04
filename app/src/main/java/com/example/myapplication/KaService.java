package com.example.myapplication;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class KaService extends AsyncTask <Void, Void, Boolean>{
    private String server;
    private String singer;
    private String song;
    private int mode;
    private String result;
    private OnCompleteCallable onCompleteCallable;

    public KaService(String server, String singer, String song, int mode, OnCompleteCallable onCompleteCallable) {
        this.server = server==null?"":server;
        this.singer = singer==null?"":singer.trim();
        this.song = song==null?"":song.trim();
        this.mode = mode;
        this.onCompleteCallable = onCompleteCallable;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            URL url = new URL(getRequestUrl());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            result = InputStreamToString(con.getInputStream());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String InputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        is.close();
        return sb.toString();
    }

    private String getRequestUrl(){
        if(singer.equals("")){
            return server+"/search_song?song="+song+"&mode="+mode;
        }else if(song.equals("")){
            return server+"/search_singer?singer="+singer+"&mode="+mode;
        }
        else {
            return server+"/search_singer_and_song?singer="+singer+"&song="+song+"&mode="+mode;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        onCompleteCallable.call(result, success);
    }
}
