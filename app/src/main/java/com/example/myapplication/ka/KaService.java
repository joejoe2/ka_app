package com.example.myapplication.ka;

import android.os.AsyncTask;

import com.example.myapplication.util.OnCompleteCallable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Async send query request to KaService server and invoke the OnCompleteCallable when task done
 */
public class KaService extends AsyncTask <Void, Void, Boolean>{
    private String singer;
    private String song;
    private int mode;
    private String result;
    private OnCompleteCallable onCompleteCallable;
    private static final String KA_SERVER ="https://ka-service.herokuapp.com";

    public KaService(String singer, String song, int mode, OnCompleteCallable onCompleteCallable) {
        this.singer = singer==null?"":singer.trim();
        this.song = song==null?"":song.trim();
        this.mode = mode;
        this.onCompleteCallable = onCompleteCallable;
    }

    /**
     * send the request in background thread
     * @param voids
     * @return only when connection error will return false
     */
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

    /**
     * read all lines of inputStream and convert into string
     * @param inputStream
     * @return
     * @throws IOException
     */
    private String InputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        inputStream.close();
        return sb.toString();
    }

    /**
     * build the request url by singer, song, mode and server
     * @return the request url which can be sent directly to server
     */
    private String getRequestUrl(){
        if(singer.equals("")){
            return KA_SERVER+"/search_song?song="+song+"&mode="+mode;
        }else if(song.equals("")){
            return KA_SERVER+"/search_singer?singer="+singer+"&mode="+mode;
        }
        else {
            return KA_SERVER+"/search_singer_and_song?singer="+singer+"&song="+song+"&mode="+mode;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        onCompleteCallable.doOnComplete(result, success);
    }
}
