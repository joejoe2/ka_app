package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 台語語音辨識
@SuppressLint("StaticFieldLeak")
public class TaiwaneseRecognitionService extends AsyncTask<String, Void, Boolean> {
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

    private onCompleteCallable onCompleteCallable;

    public TaiwaneseRecognitionService(onCompleteCallable callable) {
        this.onCompleteCallable = callable;
    }

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
                    onCompleteCallable.call(match.group(1)
                                    .replace(" ", "")
                                    .replace("\n", "")
                                    .replace("�", "")
                            , true);
                } else {
                    onCompleteCallable.call(match.group(2)
                                    .replace(" ", "")
                                    .replace("\n", "")
                                    .replace("�", "")
                            , true);
                }
            } else {
                // match failed
                onCompleteCallable.call("辨識失敗", false);
            }
        } else {
            // print error message send by server
            onCompleteCallable.call(message, false);
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
