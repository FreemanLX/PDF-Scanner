package com.example.pdfscanner;

public class Sync_Exists_File  extends Thread{
     FileVerifier obj;

     Sync_Exists_File(FileVerifier obj){
          this.obj = obj;
     }

     @Override
     public void run(){
         obj.verify();
     }

}
