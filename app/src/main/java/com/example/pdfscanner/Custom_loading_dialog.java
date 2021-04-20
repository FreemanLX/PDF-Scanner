package com.example.pdfscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public class Custom_loading_dialog {
   Activity activity;
   AlertDialog dialog;
   String message;
   TextView textView;
   View view;
   ProgressBar progressBar;

   void init(){
       textView = (TextView) view.findViewById(R.id.textView6);
       progressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
       progressBar.setMax(100);
       progressBar.setProgress(0);
   }

   Custom_loading_dialog(@NotNull Custom_loading_dialog obj){
        this.activity = obj.activity;
        this.view = obj.view;
        init();
   }

   Custom_loading_dialog(Activity activity){
       view = View.inflate(activity, R.layout.custom_loading_dialog, null);
       init();
       this.activity = activity;
   }

   Custom_loading_dialog(Activity activity, String message){
       view = View.inflate(activity, R.layout.custom_loading_dialog, null);
       init();
       this.activity = activity;
       textView.setText(message);
   }

   void setProgressBar(int progress){
        progressBar.setProgress(progress);
        if(progress == 100) stop();
   }

   void LoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        builder.setCancelable(false);
        builder.setView(view);
        this.dialog = builder.create();
        this.dialog.show();
   }

   void stop(){
       this.dialog.dismiss();
   }

}
