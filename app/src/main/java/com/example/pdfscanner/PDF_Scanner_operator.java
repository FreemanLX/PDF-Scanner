package com.example.pdfscanner;

import java.util.Comparator;

class PDF_Scanned_operator implements Comparator<PDF_Scanned> {
    public int compare(PDF_Scanned obj, PDF_Scanned obj1) {
        long obj_size = obj.getSizeInt();
        long obj1_size = obj1.getSizeInt();
        return Long.compare(obj_size, obj1_size);
    }
}