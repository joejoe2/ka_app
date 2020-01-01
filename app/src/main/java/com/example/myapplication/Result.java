package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Result extends AppCompatActivity {

    String singer="";
    String song="";
    String send_to_url="";
    ListView list;
    String status;
    Button go_back_to_search;
    ImageView man;
    ImageView woman;
    //int id;
    ArrayList toshow = new ArrayList<String>();
    ArrayList songlink=new ArrayList<String>();
    int mode;
    ProgressDialog progressDialog;

    String QUERY_SERVER="https://ka-service.herokuapp.com";
    String PREFS_NAME="ka";

    void init_query_server() throws Exception{
        // Get from the SharedPreferences
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        if(settings.getBoolean("need_update", true)) {
            URL url = new URL("https://github.com/joejoe2/ka_app/blob/master/README.MD");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String str = InputStreamToString(con.getInputStream());

            int s = str.indexOf("query host:");
            str = str.substring(s);
            QUERY_SERVER = str.substring(str.indexOf("\">") + 2, str.indexOf("</a>"));
            System.out.println(QUERY_SERVER);

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("need_update",false);
            editor.putString("host",QUERY_SERVER);
            // Apply the edits!

            editor.commit();
        }else {
            QUERY_SERVER=settings.getString("host",QUERY_SERVER);
        }

    }

    void Start_to_send(){
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("loading...");
        progressDialog.show();


        new Thread(new Runnable() {
            @Override
            public void run() {



                try {
                    init_query_server();
                    send_to_url=QUERY_SERVER;
                    song=song.trim();
                    singer=singer.trim();

                    if(singer.equals("")){
                        send_to_url=send_to_url+"/search_song?song="+song+"&mode="+mode;
                    }
                    else if(song.equals("")){
                        send_to_url=send_to_url+"/search_singer?singer="+singer+"&mode="+mode;
                    }
                    else {
                        send_to_url=send_to_url+"/search_singer_and_song?singer="+singer+"&song="+song+"&mode="+mode;
                    }
                    URL url = new URL(send_to_url);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    String str = InputStreamToString(con.getInputStream());

                    JSONObject json = new JSONObject(str);
                    System.out.println(str);
                    status = json.getString("status");
                    if(status.equals("success")){
                        int songlimit=0;
                        JSONArray songlist=json.getJSONArray("content");
                        for(int i=0;i<songlist.length();i++){
                            JSONArray jsonObject = songlist.getJSONArray(i);
                            if(!jsonObject.getString(2).equals("NULL")){
                                toshow.add(jsonObject.getString(0)+" "+jsonObject.getString(1));
                                songlink.add(jsonObject.getString(2));
                                songlimit++;
                            }
                            if(songlimit>5){
                                break;
                            }
                        }
                    }
                    else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "status="+status, Toast.LENGTH_SHORT).show();
                                }
                            });
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            ArrayAdapter adapter = new ArrayAdapter<String>(Result.this, R.layout.listitem, toshow);
                            list.setAdapter(adapter);
                        }
                    });
                    //
                    SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("need_update",false);
                    editor.putString("host",QUERY_SERVER);
                    // Apply the edits!

                    editor.commit();
                    //
                    progressDialog.dismiss();
                } catch(Exception ex) {
                    System.out.println(ex);
                    //
                    SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("need_update",true);
                    // Apply the edits!
                    
                    editor.commit();
                    //
                    progressDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),"網路錯誤",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    static String InputStreamToString(InputStream is) throws IOException {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Bundle bundle=this.getIntent().getExtras();
        singer=bundle.getString("singer");
        song=bundle.getString("song");
        mode=bundle.getInt("nowlang");

        man=findViewById(R.id.imageView3);
        woman=findViewById(R.id.imageView4);
        man.bringToFront();
        woman.bringToFront();
        go_back_to_search=findViewById(R.id.back);
        list=findViewById(R.id.songlistview);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //id=position;
                String ytblink=""+songlink.get(position);
                Intent intent=new Intent();
                intent.setClass(Result.this,YoutubePlayActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("link",ytblink);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        go_back_to_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Result.this.finish();
            }
        });

        Start_to_send();
    }
}
