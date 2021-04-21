package com.example.pdfscanner;

import android.annotation.SuppressLint;

import java.io.File;
import java.util.ArrayList;

import static android.os.Environment.getExternalStorageDirectory;


public class Read_files {
    ArrayList<String> locations;
    ArrayList<PDF_Scanned> pdf_scanned;

    @SuppressLint("DefaultLocale")
    protected String size_conversion(int size){
        if(size >= 1024 && size < 1024 * 1024) return String.format("%.2f", (float) size / (1024)) + " KB";
        if(size >= 1024 * 1024) return String.format("%.2f", (float) size / (1024 * 1024)) + " MB";
        return size + " B";
    }


    Read_files(ArrayList<String> locations, ArrayList<PDF_Scanned> pdf_scanned){
            this.locations = locations;
            this.pdf_scanned = pdf_scanned;
    }

    synchronized void set(){
        File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner");
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (File file : listFile) {
                locations.add(file.getAbsolutePath());
                long len = file.length();
                pdf_scanned.add(new PDF_Scanned(file.getName(), size_conversion((int) len), len));
            }
        }
    }

    ArrayList<PDF_Scanned> getPdf_scanned(){
          return pdf_scanned;
    }
    ArrayList<String> getLocations(){
          return locations;
    }

}
