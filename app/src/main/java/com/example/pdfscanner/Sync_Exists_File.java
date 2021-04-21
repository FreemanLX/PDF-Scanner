package com.example.pdfscanner;

public class Sync_Exists_File  extends Thread{
     Exists_File obj;

     Sync_Exists_File(Exists_File obj){
          this.obj = obj;
     }

     @Override
     public void run(){
         obj.verify();
     }

}
