package com.example.pdfscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class Alert_Generator {
    String text;
    int dialog_buttons;
    boolean auto_cancel;
    Context context;
    boolean Output;

    public static final int OK = 1;
    public static final int OKCancel = 2;
    public static final int YesNo = 3;

    Alert_Generator(Context context, String text){
        this.text = text;
        this.dialog_buttons = OK;
        this.auto_cancel = true;
        this.context = context;
    }

    Alert_Generator(Context context, String text, int dialog_buttons){
        this.text = text;
        this.dialog_buttons = dialog_buttons;
        this.auto_cancel = true;
        this.context = context;
    }


    Alert_Generator(Context context, String text, int dialog_buttons, boolean auto_cancel){
        this.text = text;
        this.dialog_buttons = dialog_buttons;
        this.context = context;
        this.auto_cancel = auto_cancel;
    }

    void setOutput(boolean output){
        Output = output;
    }

    boolean getOutput(){
        return Output;
    }

    void show(){
        Handler mHandler = new Handler(Looper.getMainLooper());
        Runnable show_async = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setMessage(text);
                if (dialog_buttons == OK) {
                    builder.setPositiveButton("OK", (dialog, id) -> {
                        setOutput(false);
                        dialog.cancel();
                    });
                    setOutput(true);
                }
                if (dialog_buttons == OKCancel) {
                    builder.setNegativeButton("Cancel", (dialog, id) -> {
                        setOutput(false);
                        dialog.cancel();
                    });
                    builder.setPositiveButton("OK", (dialog, id) -> {
                        setOutput(true);
                        dialog.cancel();
                    });

                }
                if (dialog_buttons == YesNo) {
                    builder.setNegativeButton("No", (dialog, id) -> {
                        setOutput(false);
                        dialog.cancel();
                    });
                    builder.setPositiveButton("Yes", (dialog, id) -> {
                        setOutput(true);
                        dialog.cancel();
                    });

                }
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        };
        mHandler.post(show_async);
        SynchronousHandler obj = new SynchronousHandler();
        obj.postAndWait(mHandler, show_async);

    }

}
