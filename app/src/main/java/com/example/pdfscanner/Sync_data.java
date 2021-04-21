package com.example.pdfscanner;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.util.ArrayList;

import static android.os.Environment.getExternalStorageDirectory;

public class Sync_data {
    String UUID_data;
    ArrayList<String> pdf_locations;
    ArrayList<PDF_Scanned> arrayList;


    Sync_data(String UUID){
        UUID_data = UUID;
    }
    Sync_data(ArrayList<String> pdf_locations, ArrayList<PDF_Scanned> arrayList, String UUID_data){
           this.pdf_locations = pdf_locations;
           this.arrayList = arrayList;
           this.UUID_data = UUID_data;
    }

    synchronized void sync(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        File directory = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data", "/PDFScanner");
        if (!directory.exists() && !directory.mkdirs()) return;

        StorageReference listRef = storage.getReference();
        Task<ListResult> listResultTask =  listRef.child("data/" + UUID_data).listAll();
          listResultTask.addOnSuccessListener(listResult -> {
                for (StorageReference prefix : listResult.getItems()) {
                    String filename = prefix.getName();
                    File file = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner/" + filename);
                    try {
                        if(!file.exists()){
                            if(!file.createNewFile()){
                                 throw new Exception();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    prefix.getFile(file).addOnSuccessListener(taskSnapshot -> { });
                }

          });

    }

}
