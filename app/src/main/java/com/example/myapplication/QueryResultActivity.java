package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.appcompat.app.AppCompatActivity;

public class QueryResultActivity extends AppCompatActivity {
    //ui
    private Button goBackButton;
    private ProgressDialog loadingProgressDialog;
    //flag and data
    private String singer="";
    private String song="";
    private ListView listView;
    private KaSongs songs;
    private int languageMode;
    private static String QUERY_SERVER ="https://ka-service.herokuapp.com";
    private static final String DEFAULT_REPO="https://github.com/joejoe2/ka_app/blob/master/README.MD";
    private static final String PREFS_NAME="ka";
    SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSetting();
        getBundle();
        initUI();
        setListener();
        sendQueryRequest();
    }

    private void getSetting(){
        setting = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
    }

    private void getBundle(){
        Bundle bundle=this.getIntent().getExtras();
        singer=bundle.getString("singer");
        song=bundle.getString("song");
        languageMode =bundle.getInt("nowlang");
    }

    private void initUI() {
        setContentView(R.layout.activity_result);
        ImageView man=findViewById(R.id.imageView3);
        ImageView woman=findViewById(R.id.imageView4);
        man.bringToFront();
        woman.bringToFront();
        goBackButton =findViewById(R.id.back);
        listView =findViewById(R.id.songlistview);
    }

    private void setListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ytblink=songs.getSongLinks().get(position);
                Intent intent=new Intent();
                intent.setClass(QueryResultActivity.this, YoutubePlayerActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("link",ytblink);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QueryResultActivity.this.finish();
            }
        });
    }

    private void sendQueryRequest(){
        loadingProgressDialog =new ProgressDialog(this);
        loadingProgressDialog.setMessage("loading...");
        loadingProgressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                    prepareQueryServer();
                    KaService kaService=new KaService(QUERY_SERVER, singer, song, languageMode, new OnCompleteCallable(){
                        @Override
                        public void call(String msg, boolean success) {
                            if (success){
                                handleQueryResult(msg);
                                showQueryResult();
                                updateValidQueryServerSetting();
                            }else {
                                handleQueryConnectionError();
                            }
                            loadingProgressDialog.dismiss();
                        }
                    });
                    kaService.execute();
            }
        }).start();
    }

    private void prepareQueryServer() {
        try {
            if (setting.getBoolean("need_update", true)) {
                QUERY_SERVER = getNewQueryServerFromRepo();
                SharedPreferences.Editor editor = setting.edit();
                editor.putBoolean("need_update", false);
                editor.putString("host", QUERY_SERVER);
                editor.commit();
            } else {
                QUERY_SERVER = setting.getString("host", QUERY_SERVER);
            }
        } catch (IOException ex) {
            SharedPreferences.Editor editor = setting.edit();
            editor.putBoolean("need_update", true);
            editor.commit();
            loadingProgressDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "網路錯誤", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getNewQueryServerFromRepo() throws IOException {
        URL url = new URL(DEFAULT_REPO);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String str = InputStreamToString(con.getInputStream());

        int s = str.indexOf("query host:");
        str = str.substring(s);
        return str.substring(str.indexOf("\">") + 2, str.indexOf("</a>"));
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

    private void updateValidQueryServerSetting(){
        setting = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean("need_update", false);
        editor.putString("host", QUERY_SERVER);
        editor.commit();
    }

    private void handleQueryResult(String result){
        try {
            KaResponse kaResponse=new KaResponse(result);
            songs=kaResponse.getSongs();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void showQueryResult(){
        runOnUiThread(new Runnable() {
            public void run() {
                ArrayAdapter adapter = new ArrayAdapter<String>(QueryResultActivity.this, R.layout.listitem, songs.getValidSongs());
                listView.setAdapter(adapter);
            }
        });
    }

    private void handleQueryConnectionError(){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "網路錯誤", Toast.LENGTH_SHORT).show();
            }
        });
        setting = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean("need_update", true);
        editor.commit();
    }
}
