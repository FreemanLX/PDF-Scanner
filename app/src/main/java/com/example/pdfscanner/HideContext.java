package com.example.pdfscanner;
import android.app.Activity;
import android.view.View;

class HideContext<T extends View>  {
     Activity context;
     int id;
     public HideContext(Activity activity, int id){
                this.context = activity;
                this.id = id;
     }
     public void set(){
         T type = (T) context.findViewById(id);
         type.setVisibility(View.GONE);
     }

}
