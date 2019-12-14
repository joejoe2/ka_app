package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    //int id;
    ArrayList toshow = new ArrayList<String>();
    ArrayList songlink=new ArrayList<String>();
    int mode;
    ProgressDialog progressDialog;

    String QUERY_SERVER="http://showdata.nctu.me:8080";

    void init_query_server(){

            URL url = null;
            try {
                url = new URL("https://github.com/joejoe2/ka_app/blob/master/README.MD");
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                String str = InputStreamToString(con.getInputStream());

                int s=str.indexOf("query host:");
                str=str.substring(s);
                QUERY_SERVER=str.substring(str.indexOf("\">")+2,str.indexOf("</a>"));
                System.out.println(QUERY_SERVER);
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    void Start_to_send(){
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("loading...");
        progressDialog.show();


        new Thread(new Runnable() {
            @Override
            public void run() {

                init_query_server();
                send_to_url=QUERY_SERVER;

                song=song.trim();
                singer=singer.trim();

                try {
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
                        JSONArray songlist=json.getJSONArray("content");
                        for(int i=0;i<songlist.length();i++){
                            JSONArray jsonObject = songlist.getJSONArray(i);
                            if(!jsonObject.getString(2).equals("NULL")){
                                toshow.add(jsonObject.getString(0)+" "+jsonObject.getString(1));
                                songlink.add(jsonObject.getString(2));
                            }
                        }
                    }
                    else {
                        if(mode==0){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "status="+status, Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                    if(mode==1){
                        send_to_url=QUERY_SERVER;
                        if(singer.equals("")){
                            send_to_url=send_to_url+"/search_song?song="+song+"&mode="+2;
                        }
                        else if(song.equals("")){
                            send_to_url=send_to_url+"/search_singer?singer="+singer+"&mode="+2;
                        }
                        else {
                            send_to_url=send_to_url+"/search_singer_and_song?singer="+singer+"&song="+song+"&mode="+2;
                        }
                         url = new URL(send_to_url);
                         con = (HttpURLConnection)url.openConnection();
                         str = InputStreamToString(con.getInputStream());
                         json = new JSONObject(str);
                         status = json.getString("status");
                        if(status.equals("success")){
                            JSONArray songlist=json.getJSONArray("content");
                            for(int i=0;i<songlist.length();i++){
                                JSONArray jsonObject = songlist.getJSONArray(i);
                                if(!jsonObject.getString(2).equals("NULL")){
                                    toshow.add(jsonObject.getString(0)+" "+jsonObject.getString(1));
                                    songlink.add(jsonObject.getString(2));
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

                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            ArrayAdapter adapter = new ArrayAdapter<String>(Result.this, android.R.layout.simple_list_item_1, toshow);
                            list.setAdapter(adapter);
                        }
                    });
                    progressDialog.dismiss();
                } catch(Exception ex) {
                    System.out.println(ex);
                    progressDialog.dismiss();
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
