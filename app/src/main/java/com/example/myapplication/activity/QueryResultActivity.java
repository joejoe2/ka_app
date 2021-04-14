package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.myapplication.ka.KaResponse;
import com.example.myapplication.ka.KaService;
import com.example.myapplication.util.OnCompleteCallable;
import com.example.myapplication.R;
import com.example.myapplication.util.ToastLogger;

import org.json.JSONException;

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
    private String singer = "";
    private String song = "";
    private ListView listView;
    KaResponse kaResponse;
    private int languageMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBundle();
        initUI();
        setListener();
        sendQueryRequest();
    }

    /**
     * receive query params(singer, song and languageMode) from bundle
     */
    private void getBundle() {
        Bundle bundle = this.getIntent().getExtras();
        singer = bundle.getString("singer");
        song = bundle.getString("song");
        languageMode = bundle.getInt("languageMode");
    }

    private void initUI() {
        setContentView(R.layout.activity_result);
        ImageView man = findViewById(R.id.imageView3);
        ImageView woman = findViewById(R.id.imageView4);
        man.bringToFront();
        woman.bringToFront();
        goBackButton = findViewById(R.id.back);
        listView = findViewById(R.id.songlistview);
    }

    private void setListener() {
        //start YoutubePlayerActivity when click the list item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ytblink = kaResponse.getSongs().getSongLinks().get(position);
                Intent intent = new Intent();
                intent.setClass(QueryResultActivity.this, YoutubePlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("link", ytblink);
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
    private void sendQueryRequest() {
        loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setMessage("loading...");
        loadingProgressDialog.show();
        //execute KaService
        KaService kaService = new KaService(singer, song, languageMode, new OnCompleteCallable() {
            @Override
            public void doOnComplete(String msg, boolean success) {
                if (success) {
                    processResult(msg);
                    showResult();
                } else {
                    ToastLogger.logOnActivity(QueryResultActivity.this, "網路錯誤");
                }
                loadingProgressDialog.dismiss();
            }
        });
        kaService.execute();
    }

    /**
     * get KaResponse from KaService result string
     *
     * @param result json string of KaService result
     */
    private void processResult(String result) {
        try {
            kaResponse = new KaResponse(result);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * render the songs in list view
     */
    private void showResult() {
        runOnUiThread(new Runnable() {
            public void run() {
                ArrayAdapter adapter = new ArrayAdapter<String>(QueryResultActivity.this, R.layout.listitem, kaResponse.getSongs().getValidSongs());
                listView.setAdapter(adapter);
            }
        });
    }

}
