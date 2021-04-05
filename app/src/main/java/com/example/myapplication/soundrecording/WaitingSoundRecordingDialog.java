package com.example.myapplication.soundrecording;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.R;

import androidx.annotation.NonNull;

/**
 * specific dialog for waiting sound recording finish
 */
public class WaitingSoundRecordingDialog extends Dialog {
    public WaitingSoundRecordingDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_recording);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageButton btnComplete=findViewById(R.id.btn_robot);
        btnComplete.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                cancel();
            }
        });
        TextView textView=findViewById(R.id.text_dialogHint);
        textView.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                cancel();
            }
        });
    }
}
