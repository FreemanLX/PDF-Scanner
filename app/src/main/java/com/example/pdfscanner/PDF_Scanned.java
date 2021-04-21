package com.example.pdfscanner;

public class PDF_Scanned {
    String name, size;
    long size_int;
    PDF_Scanned(String name, String size) {
        this.name = name;
        this.size = size;
    }
    PDF_Scanned(String name, String size, long size_int){
        this.name = name;
        this.size = size;
        this.size_int = size_int;
    }
    public String getName() {
        return "File name: " + name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSize() {
        return "File size: " + size;
    }
    public long getSizeInt(){
        return size_int;
    }
}