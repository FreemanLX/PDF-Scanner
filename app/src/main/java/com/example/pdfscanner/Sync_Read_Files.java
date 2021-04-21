package com.example.pdfscanner;

import androidx.annotation.RequiresPermission;

import java.util.ArrayList;

public class Sync_Read_Files extends Thread {
      Read_files obj;

      Sync_Read_Files(Read_files obj){
          this.obj  = obj;
      }

      public void run(){
          obj.set();
      }

}
