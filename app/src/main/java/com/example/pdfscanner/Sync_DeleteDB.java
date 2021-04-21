package com.example.pdfscanner;

public class Sync_DeleteDB extends Thread {
     DeleteDB obj;

     Sync_DeleteDB(DeleteDB obj){
         this.obj = obj;
     }

     public void run() {
        obj.set();
     }
}
