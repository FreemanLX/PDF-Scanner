package com.example.pdfscanner;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;

public class DeleteDB {

     String loc;
     String UUID;
     DeleteDB(String loc, String UUID){
           this.loc = loc;
           this.UUID = UUID;
     }

     synchronized void set(){
         FirebaseStorage storage = FirebaseStorage.getInstance();
         Uri file = null;
         try {
              file = Uri.fromFile(new File(loc));
         }
         catch (Exception e){
             e.printStackTrace();
         }
         StorageReference verify = storage.getReference().child("data/" + UUID + "/" + file.getLastPathSegment());
         verify.delete().addOnFailureListener(Throwable::printStackTrace);
     }

}
