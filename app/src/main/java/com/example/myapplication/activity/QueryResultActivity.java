package com.example.myapplication.activity;

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

import com.example.myapplication.ka.KaResponse;
import com.example.myapplication.ka.KaService;
import com.example.myapplication.util.OnCompleteCallable;
import com.example.myapplication.R;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.appcompat.app.AppCompatActivity;

/**
 * this activity will send query request with singer, song and languageMode in received bundle .
 * then render the results on the list view, the user can click the list item to lunch and play the customized YoutubePlayerActivity
 */
public class QueryResultActivity extends AppCompatActivity {
    //ui
    private Button goBackButton;
    private ProgressDialog loadingProgressDialog;
    //flag and data
    private String singer="";
    private String song="";
    private ListView listView;
    KaResponse kaResponse;
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

    /**
     * get setting in SharedPreferences
     */
    private void getSetting(){
        setting = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
    }

    /**
     * receive query params(singer, song and languageMode) from bundle
     */
    private void getBundle(){
        Bundle bundle=this.getIntent().getExtras();
        singer=bundle.getString("singer");
        song=bundle.getString("song");
        languageMode = bundle.getInt("languageMode");
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
        //start YoutubePlayerActivity when click the list item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ytblink=kaResponse.getSongs().getSongLinks().get(position);
                Intent intent=new Intent();
                intent.setClass(QueryResultActivity.this, YoutubePlayerActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("link",ytblink);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        //finish this activity when click the goBackButton
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QueryResultActivity.this.finish();
            }
        });
    }

    /**
     * send the query request via KaService, and render results in the list view when request completed
     */
    private void sendQueryRequest(){
        loadingProgressDialog =new ProgressDialog(this);
        loadingProgressDialog.setMessage("loading...");
        loadingProgressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                    //check correct server
                    prepareQueryServer();
                    //execute KaService
                    KaService kaService=new KaService(QUERY_SERVER, singer, song, languageMode, new OnCompleteCallable(){
                        @Override
                        public void doOnComplete(String msg, boolean success) {
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

    /**
     * use the server address of last successful request, or try to get new server address
     */
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

    /**
     * get new server address from readme file of github repo
     * @return server address
     * @throws IOException
     */
    private String getNewQueryServerFromRepo() throws IOException {
        URL url = new URL(DEFAULT_REPO);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String str = InputStreamToString(con.getInputStream());

        int s = str.indexOf("query host:");
        str = str.substring(s);
        return str.substring(str.indexOf("\">") + 2, str.indexOf("</a>"));
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
     * write server address into setting, this should be called after a successful request
     */
    private void updateValidQueryServerSetting(){
        setting = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean("need_update", false);
        editor.putString("host", QUERY_SERVER);
        editor.commit();
    }

    /**
     * get KaResponse from KaService result string
     * @param result json string of KaService result
     */
    private void handleQueryResult(String result){
        try {
            kaResponse=new KaResponse(result);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * render the songs in list view
     */
    private void showQueryResult(){
        runOnUiThread(new Runnable() {
            public void run() {
                ArrayAdapter adapter = new ArrayAdapter<String>(QueryResultActivity.this, R.layout.listitem, kaResponse.getSongs().getValidSongs());
                listView.setAdapter(adapter);
            }
        });
    }

    /**
     * write failure server address into setting, this should be called after a unsuccessful request
     */
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
