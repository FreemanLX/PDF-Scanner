package com.example.pdfscanner;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class Exists_File{
    String loc;
    String UUID;
    boolean result;
    Exists_File(String loc, String UUID) {
        this.loc = loc;
        this.UUID = UUID;
    }

    synchronized public void verify(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        Uri file = Uri.fromFile(new File(loc));
        StorageReference verify = storage.getReference().child("data/" + UUID + "/" + file.getLastPathSegment());
        try {
            verify.getDownloadUrl().addOnFailureListener(e -> {
                result = false;
            });
        }
        catch(Exception e) {
            e.printStackTrace();
            result = false;
        }
        result = true;
    }

    public boolean getResult(){
        return result;
    }
}
