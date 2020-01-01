package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ToggleButton lang;
    EditText singer;
    EditText song;
    Button singer_record;
    Button song_record;
    Button to_send;
    CheckBox taiwanese;
    CheckBox chinese;
    ProgressDialog progressDialog;
    ImageView mic1;
    ImageView mic2;
    private final int REQ_CODE_SPEECH_INPUT=100;
    private  boolean busy;
    private File recordFile;
    private MediaRecorder mediaRecorder=new MediaRecorder();
    int nowlang=0;
    //0->台語 1->中文

    int singer_or_song=0;
    //0->singer 1->song

    String singer_to_send="";
    String song_to_send="";

    private void initUI()
    {
        //lang=findViewById(R.id.language);
        taiwanese=findViewById(R.id.checkBox0);
        chinese=findViewById(R.id.checkBox1);
        taiwanese.setChecked(true);
        chinese.setChecked(false);
        singer=findViewById(R.id.singer);
        song=findViewById(R.id.song);
        singer_record=findViewById(R.id.singerbutton);
        song_record=findViewById(R.id.songbutton);
        to_send=findViewById(R.id.check);
        mic1=findViewById(R.id.imageView);
        mic2=findViewById(R.id.imageView2);
        mic1.bringToFront();
        mic2.bringToFront();

    }

    private void permissiononCheck(){
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 =ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        if(permission1!= PackageManager.PERMISSION_GRANTED||permission2!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},1);
        }
    }

    private boolean checkNetWork(){
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
    private void chineseSpeechInput(){
        if(checkNetWork()){
            Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.TAIWAN.toString());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"say something");
            try {
                startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
            }
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(getApplicationContext(),"doesnt support",Toast.LENGTH_SHORT).show();
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
                    if(singer_or_song==0){
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
        if(checkNetWork()){
            try {
                recordFile=File.createTempFile("record_temp",".m4a",getCacheDir());
                mediaRecorder.setOutputFile(recordFile.getAbsolutePath());
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setAudioEncodingBitRate(326000);
                mediaRecorder.setAudioSamplingRate(44100);
                mediaRecorder.setAudioChannels(1);
                mediaRecorder.prepare();
                mediaRecorder.start();
            }
            catch (IOException e)
            {
                pushResult(e.getMessage(),false);
                e.printStackTrace();
            }

            final Dialog dialog =new Dialog(this);
            dialog.setContentView(R.layout.dialog_recording);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            dialog.setOnCancelListener(new Dialog.OnCancelListener(){
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    endTaiwaneseRecognition();
                }
            });

            ImageButton btnComplete=dialog.findViewById(R.id.btn_robot);
            btnComplete.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View view){
                    dialog.cancel();
                }
            });

            TextView textView=dialog.findViewById(R.id.text_dialogHint);
            textView.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View view){
                    dialog.cancel();
                }
            });

            dialog.show();
        }
    }


    private void endTaiwaneseRecognition()
    {
        mediaRecorder.stop();
        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("loading...");
        progressDialog.show();
        new TaiwaneseSender().execute(recordFile.getAbsolutePath(),"main");
    }

    private void pushResult(String msg,boolean success){
        progressDialog.dismiss();
        if(success) {
            if(singer_or_song==0){
                singer.setText(msg.split("2.")[0].replace("1.",""));
            }
            else {
                song.setText(msg.split("2.")[0].replace("1.",""));
            }
        }else {
            Toast.makeText(getApplicationContext(),"辨識失敗",Toast.LENGTH_SHORT).show();
        }
        busy=false;
    }

    void check(){
        if(taiwanese.isChecked()){
            chinese.setChecked(false);
            nowlang=0;
        }
        else {
            chinese.setChecked(true);
            nowlang=1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        permissiononCheck();
        singer_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singer_or_song=0;
                if(nowlang==1){
                    chineseSpeechInput();
                }
                else{
                    startTaiwaneseRecognition();
                }
            }
        });



        taiwanese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });

        chinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chinese.isChecked()){
                    taiwanese.setChecked(false);
                    nowlang=1;
                }
                else {
                    taiwanese.setChecked(true);
                    nowlang=0;
                }
            }
        });

        song_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singer_or_song=1;
                if(nowlang==1){
                    chineseSpeechInput();
                }
                else{
                    startTaiwaneseRecognition();
                }
            }
        });

        to_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkNetWork()){
                    song_to_send=song.getText().toString();
                    singer_to_send=singer.getText().toString();
                    check();
                    if(song_to_send.equals("")!=true||singer_to_send.equals("")!=true){
                        Intent intent=new Intent();
                        intent.setClass(MainActivity.this,Result.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("singer",singer_to_send);
                        bundle.putString("song",song_to_send);
                        bundle.putInt("nowlang",nowlang);
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


    // 台語語音辨識
    @SuppressLint("StaticFieldLeak")
    public class TaiwaneseSender extends AsyncTask<String, Void, Boolean> {
        /*
         * param[0]:  path of sound file
         * param[1]: the target model
         * */
        // 伺服器核發之安全性token
        private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJpZCI6NzgsInVzZXJfaWQiOiIwIiwic2VydmljZV9pZCI6IjMiLCJzY29wZXMiOiI5OTk5OTk5OTkiLCJzdWIiOiIiLCJpYXQiOjE1NDEwNjUwNzEsIm5iZiI6MTU0MTA2NTA3MSwiZXhwIjoxNjk4NzQ1MDcxLCJpc3MiOiJKV1QiLCJhdWQiOiJ3bW1rcy5jc2llLmVkdS50dyIsInZlciI6MC4xfQ.K4bNyZ0vlT8lpU4Vm9YhvDbjrfu_xuPx8ygoKsmovRxCCUbj4OBX4PzYLZxeyVF-Bvdi2-wphGVEjz8PsU6YGRSh5SDUoHjjukFesUr8itMmGfZr4BsmEf9bheDm65zzbmbk7EBA9pn1TRimRmNG3XsfuDZvceg6_k6vMWfhQBA";

        // 伺服器資訊
        private static final String host = "140.116.245.149";
        private static final int port = 2802;
        private static final String TAG = "TaiwaneseSender";

        // result message
        private String message;

        @Override
        protected Boolean doInBackground(String... param) {


            String model = param[1];
            String padding = new String(new char[8 - model.length()])
                    .replace("\0", "\u0000");
            String label = "A";
            String header = token + "@@@" + model + padding + label;

            try {
                byte[] b_header = header.getBytes();
                byte[] b_sample = readAsByteArray(param[0]);

                int len = b_header.length + b_sample.length;
                byte[] b_len = new byte[4];
                b_len[0] = (byte) ((len & 0xff000000) >>> 24);
                b_len[1] = (byte) ((len & 0x00ff0000) >>> 16);
                b_len[2] = (byte) ((len & 0x0000ff00) >>> 8);
                b_len[3] = (byte) ((len & 0x000000ff));

                ByteArrayOutputStream arrayOutput = new ByteArrayOutputStream();
                arrayOutput.write(b_len);
                arrayOutput.write(b_header);
                arrayOutput.write(b_sample);

                Socket socket = new Socket();
                InetSocketAddress socketAddress = new InetSocketAddress(host, port);
                socket.connect(socketAddress, 10000);

                // 將訊息傳至server
                BufferedOutputStream sout = new BufferedOutputStream(socket.getOutputStream());
                sout.write(arrayOutput.toByteArray());
                sout.flush();

                // 從server接收訊息
                arrayOutput = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                BufferedInputStream sin = new BufferedInputStream(socket.getInputStream());
                int n;
                while (true) {
                    n = sin.read(buf);
                    if (n < 0) break;
                    arrayOutput.write(buf, 0, n);
                }

                sout.close();
                sin.close();
                socket.close();

                message = new String(arrayOutput.toByteArray(), Charset.forName("UTF-8"));

                return true;
            } catch (Exception e) {
                message = e.getMessage();
                Log.e(TAG, "doInBackground: ", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            Log.i(TAG, "onPostExecute: message: " + message);

            if (success) {
                Matcher match = Pattern.compile("ori:(.*)result:(.*)").matcher(message);
                if (match.find()) {
                    if (match.group(2).contains("same with ori")) {
                        // `result` same as `ori`
                        pushResult(match.group(1)
                                        .replace(" ", "")
                                        .replace("\n", "")
                                        .replace("�", "")
                                , true);
                    } else {
                        pushResult(match.group(2)
                                        .replace(" ", "")
                                        .replace("\n", "")
                                        .replace("�", "")
                                , true);
                    }
                } else {
                    // match failed
                    pushResult("辨識失敗", false);
                }
            } else {
                // print error message send by server
                pushResult(message, false);
            }
        }

        private byte[] readAsByteArray(String path) throws IOException {
            FileInputStream fis = new FileInputStream(path);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];

            for (int readNum; (readNum = fis.read(b)) != -1; ) {
                bos.write(b, 0, readNum);
            }

            return bos.toByteArray();
        }
    }
}
