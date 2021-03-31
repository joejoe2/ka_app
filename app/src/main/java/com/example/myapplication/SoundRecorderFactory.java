package com.example.myapplication;

import android.media.MediaRecorder;

import java.io.File;

public class SoundRecorderFactory {
    public static MediaRecorder generate(File outputFile){
        MediaRecorder mediaRecorder=new MediaRecorder();
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(326000);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioChannels(1);
        return mediaRecorder;
    }
}
