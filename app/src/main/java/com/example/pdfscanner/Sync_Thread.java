package com.example.pdfscanner;

public class Sync_Thread extends Thread {
    Sync_data obj;
    Sync_Thread(Sync_data obj){
        this.obj = obj;
    }
    public void run(){
        obj.sync();
    }

}
