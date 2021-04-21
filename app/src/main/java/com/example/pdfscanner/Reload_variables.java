package com.example.pdfscanner;
import com.example.pdfscanner.Multiple_scan;

import java.util.ArrayList;

public class Reload_variables {
    static ArrayList<Multiple_scan.image_document_scanned> data;

    public Reload_variables(ArrayList<Multiple_scan.image_document_scanned> init){
             data = new ArrayList<>();
             if(init == null){
                 return;
             }
             this.data.addAll(init);
    }

    public static ArrayList<Multiple_scan.image_document_scanned> Getter(){
         return data;
    }


}
