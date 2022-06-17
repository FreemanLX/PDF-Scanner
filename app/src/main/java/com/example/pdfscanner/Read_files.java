package com.example.pdfscanner;

import android.annotation.SuppressLint;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static android.os.Environment.getExternalStorageDirectory;

import androidx.annotation.RequiresApi;


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

    @RequiresApi(api = Build.VERSION_CODES.O)
    synchronized void set(){
        File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner");
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (File file : listFile) {
                locations.add(file.getAbsolutePath());
                long len = file.length();
                try {
                    if(len > 0 && is_pdf(Files.readAllBytes(Paths.get(file.getAbsolutePath()))))
                    pdf_scanned.add(new PDF_Scanned(file.getName(), size_conversion((int) len), len));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean is_pdf(byte[] data) {
        if (data != null && data.length > 4 &&
                data[0] == 0x25 && // %
                data[1] == 0x50 && // P
                data[2] == 0x44 && // D
                data[3] == 0x46 && // F
                data[4] == 0x2D) { // -

            // version 1.3 file terminator
            if (data[5] == 0x31 && data[6] == 0x2E && data[7] == 0x33 &&
                    data[data.length - 7] == 0x25 && // %
                    data[data.length - 6] == 0x25 && // %
                    data[data.length - 5] == 0x45 && // E
                    data[data.length - 4] == 0x4F && // O
                    data[data.length - 3] == 0x46 && // F
                    data[data.length - 2] == 0x20 && // SPACE
                    data[data.length - 1] == 0x0A) { // EOL
                return true;
            }

            // version 1.3 file terminator
            // EOL
            return data[5] == 0x31 && data[6] == 0x2E && data[7] == 0x34 &&
                    data[data.length - 6] == 0x25 && // %
                    data[data.length - 5] == 0x25 && // %
                    data[data.length - 4] == 0x45 && // E
                    data[data.length - 3] == 0x4F && // O
                    data[data.length - 2] == 0x46 && // F
                    data[data.length - 1] == 0x0A;
        }
        return false;
    }

    ArrayList<PDF_Scanned> getPdf_scanned(){
          return pdf_scanned;
    }
    ArrayList<String> getLocations(){
          return locations;
    }

}
