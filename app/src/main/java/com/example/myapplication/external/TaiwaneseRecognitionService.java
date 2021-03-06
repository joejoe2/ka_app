package com.example.myapplication.external;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.example.myapplication.util.OnCompleteCallable;

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

/**
 * the original code is from the optional class in 2019 and the author is unknown
 */
// 台語語音辨識
@SuppressLint("StaticFieldLeak")
public class TaiwaneseRecognitionService extends AsyncTask<String, Void, Boolean> {
    /*
     * param[0]:  path of sound file
     * param[1]: the target model
     * */
    // 伺服器核發之安全性token
    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJ1c2VyX2lkIjoiOTYiLCJuYmYiOjE2MjM3NDU2NTYsInNjb3BlcyI6IjAiLCJhdWQiOiJ3bW1rcy5jc2llLmVkdS50dyIsImlzcyI6IkpXVCIsInZlciI6MC4xLCJpYXQiOjE2MjM3NDU2NTYsInN1YiI6IiIsImV4cCI6MTYzOTI5NzY1NiwiaWQiOjM3MSwic2VydmljZV9pZCI6IjMifQ.rA7jKWhy_ZPNirgxkDH5kIXlBupHdhMhw2QteNc7j-qkEbP06dgwCBtS4REjuBu1D_FrwSA_UPkOm8jxEgbn7ZbQZKeFtd3eUC8bIEBEduw5XnOFYLjKRGkk9WwFyRyGCsuQJnTTL8ZLtUVasyo6utiOJ4F3eAosKh3Fgmu5r3E";

    // 伺服器資訊
    private static final String host = "140.116.245.149";
    private static final int port = 2802;
    private static final String TAG = "TaiwaneseSender";

    // result message
    private String message;

    private OnCompleteCallable onCompleteCallable;

    public TaiwaneseRecognitionService(OnCompleteCallable callable) {
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

        System.out.println("onPostExecute: message: " + message);

        if (success) {
            Matcher match = Pattern.compile("ori:(.*)result:(.*)").matcher(message);
            if (match.find()) {
                String matchResult;
                if (match.group(2).contains("same with ori")) {
                    // `result` same as `ori`
                    matchResult=match.group(1);
                } else {
                    matchResult=match.group(2);
                }
                onCompleteCallable.doOnComplete(matchResult
                                .replace(" ", "")
                                .replace("\n", "")
                                .replace("�", "")
                                .replace(";", "")
                                .replace("*", "")
                                .replaceAll("[a-zA-z*]", "")
                        , true);
            } else {
                // match failed
                onCompleteCallable.doOnComplete("辨識失敗", false);
            }
        } else {
            // print error message send by server
            onCompleteCallable.doOnComplete(message, false);
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
