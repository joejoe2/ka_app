package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //ui
    private EditText singer;
    private EditText song;
    private Button singerRecordButton;
    private Button songRecordButton;
    private Button queryButton;
    private CheckBox taiwaneseOption;
    private CheckBox chineseOption;
    private ProgressDialog loadingProgressDialog;
    private ImageView microphoneImage1;
    private ImageView microphoneImage2;
    //tool
    private File recordFile;
    private MediaRecorder soundRecorder;
    //flag and data
    private final int REQ_CODE_SPEECH_INPUT=100;
    private Language languageMode=Language.Chinese;
    private boolean isInputForSinger;
    private String singerToSend="";
    private String songToSend="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        checkPermission();
        setListener();
    }

    private void initUI() {
        setContentView(R.layout.activity_main);
        taiwaneseOption =findViewById(R.id.checkBox0);
        chineseOption =findViewById(R.id.checkBox1);
        taiwaneseOption.setChecked(true);
        chineseOption.setChecked(false);
        singer=findViewById(R.id.singer);
        song=findViewById(R.id.song);
        singerRecordButton =findViewById(R.id.singerbutton);
        songRecordButton =findViewById(R.id.songbutton);
        queryButton =findViewById(R.id.check);
        microphoneImage1 =findViewById(R.id.imageView);
        microphoneImage2 =findViewById(R.id.imageView2);
        microphoneImage1.bringToFront();
        microphoneImage2.bringToFront();
    }

    private void checkPermission(){
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 =ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if(permission1!= PackageManager.PERMISSION_GRANTED||permission2!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},1);
        }
    }

    private void setListener(){
        taiwaneseOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constrainOptions(Language.Taiwanese);
            }
        });

        chineseOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constrainOptions(Language.Chinese);
            }
        });

        singerRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInputForSinger=true;
                if(languageMode.equals(Language.Chinese)){
                    startChineseRecognition();
                }
                else{
                    startTaiwaneseRecognition();
                }
            }
        });

        songRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInputForSinger=false;
                if(languageMode.equals(Language.Chinese)){
                    startChineseRecognition();
                }
                else{
                    startTaiwaneseRecognition();
                }
            }
        });

        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasNetWork()){
                    String songToSend =song.getText().toString();
                    String  singerToSend =singer.getText().toString();

                    if(!songToSend.equals("") || !singerToSend.equals("")){
                        Intent intent=new Intent();
                        intent.setClass(MainActivity.this, QueryResultActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("singer", singerToSend);
                        bundle.putString("song", songToSend);
                        bundle.putInt("nowlang", languageMode.ordinal());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"輸入歌手或歌名",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void constrainOptions(Language languageOption){
        if(languageOption.equals(Language.Taiwanese)){
            taiwaneseOption.setChecked(true);
            chineseOption.setChecked(false);
            languageMode=Language.Taiwanese;
        }
        else if(languageOption.equals(Language.Chinese)){
            chineseOption.setChecked(true);
            taiwaneseOption.setChecked(false);
            languageMode=Language.Chinese;
        }
    }

    private void startChineseRecognition(){
        if(hasNetWork()){
            Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.TAIWAN.toString());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"say something");
            try {
                startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
            }
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(getApplicationContext(),"does not support !",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case REQ_CODE_SPEECH_INPUT:
            {
                if(resultCode==RESULT_OK&&null!=data){
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(isInputForSinger){
                        singer.setText(result.get(0));
                    }
                    else{
                        song.setText(result.get(0));
                    }
                }
                break;
            }
        }
    }


    private void startTaiwaneseRecognition(){
        if(hasNetWork()){
            try {
                recordFile=File.createTempFile("record_temp",".m4a", getCacheDir());
                soundRecorder = SoundRecorderFactory.generate(recordFile);
                soundRecorder.prepare();
                soundRecorder.start();
            }
            catch (IOException e)
            {
                pushResultToEditText(e.getMessage(),false);
                e.printStackTrace();
            }

            final Dialog waitingTaiwaneseRecognitionDialog =new WaitingTaiwaneseRecognitionDialog(this);
            waitingTaiwaneseRecognitionDialog.setOnCancelListener(new Dialog.OnCancelListener(){
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    endTaiwaneseRecognition();
                }
            });
            waitingTaiwaneseRecognitionDialog.show();
        }
    }

    private void endTaiwaneseRecognition()
    {
        soundRecorder.stop();
        loadingProgressDialog =new ProgressDialog(MainActivity.this);
        loadingProgressDialog.setMessage("loading...");
        loadingProgressDialog.show();
        new TaiwaneseRecognitionService(new onCompleteCallable() {
            @Override
            public void call(String msg, boolean success) {
                pushResultToEditText(msg, success);
            }
        }).execute(recordFile.getAbsolutePath(),"main");
    }

    private void pushResultToEditText(String msg, boolean success){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingProgressDialog.dismiss();
                if (success) {
                    if(isInputForSinger){
                        singer.setText(msg.split("2.")[0].replace("1.",""));
                    }
                    else {
                        song.setText(msg.split("2.")[0].replace("1.",""));
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"辨識失敗",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean hasNetWork(){
        ConnectivityManager mConnectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert mConnectivityManager!=null;
        NetworkInfo mNetWorkInfo =mConnectivityManager.getActiveNetworkInfo();
        if(mNetWorkInfo!=null&&mNetWorkInfo.isConnected())return true;
        else
        {
            Toast.makeText(this,"無網路連線",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
