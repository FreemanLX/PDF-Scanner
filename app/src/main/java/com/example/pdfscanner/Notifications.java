package com.example.pdfscanner;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.jetbrains.annotations.Contract;

public class Notifications {
    String text;
    int priority;
    boolean auto_cancel;
    Context context;
    String title;

    Notifications(Context context, String text){
        this.title = "PDFScanner";
        this.text = text;
        this.priority = NotificationCompat.PRIORITY_DEFAULT;
        this.auto_cancel = true;
        this.context = context;
    }


     Notifications(Context context, String text, String title, int priority){
         this.title = title;
         this.text = text;
         this.priority = priority;
         this.auto_cancel = true;
         this.context = context;
     }


     Notifications(Context context, String text, String title, int priority, boolean auto_cancel){
              this.text = text;
              this.title = title;
              this.priority = priority;
              this.auto_cancel = auto_cancel;
              this.context = context;
     }

     public void show(){
         NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "PDFScanner notification")
                 .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                 .setContentTitle(title)
                 .setContentText(text)
                 .setPriority(priority)
                 .setAutoCancel(auto_cancel);
         NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
         managerCompat.notify(1, builder.build());
         if (Build.VERSION.SDK_INT >= 26) {
             NotificationChannel channel = new NotificationChannel("PDFScanner notification", "PDFScanner", NotificationManager.IMPORTANCE_HIGH);
             NotificationManager manager = context.getSystemService(NotificationManager.class);
             manager.createNotificationChannel(channel);
         }

     }


}
