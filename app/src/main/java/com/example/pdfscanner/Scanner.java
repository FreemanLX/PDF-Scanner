package com.example.pdfscanner;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import static android.os.Environment.getExternalStorageDirectory;


public class Scanner extends AppCompatActivity {
    private boolean guest = false;
    PDF_Adapter adapter;
    ArrayList<Bitmap> data_photo;
    AtomicInteger current_position = new AtomicInteger();
    ArrayList<String> pdf_locations;
    TextView empty_message;
    FirebaseStorage storage;
    private String UUID;
    ArrayList<PDF_Scanned> arrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    Scanner scanner;

    private void setAdapter(){
         adapter = new PDF_Adapter(arrayList, this);
         recyclerView = findViewById(R.id.listview);
         RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
         recyclerView.setLayoutManager(layoutManager);
         recyclerView.setItemAnimator(new DefaultItemAnimator());
         recyclerView.addItemDecoration(new DividerLineRecyclerView(this));
         recyclerView.setAdapter(adapter);

    }
    protected void after_sort(){
        if(arrayList.size() > 0) {
            QuickSort<PDF_Scanned> quickSort = new QuickSort<>(arrayList, new PDF_Scanned_operator());
            try {
                quickSort.sort();
                arrayList = quickSort.getArrayList();
            }
            catch (Exception e){ e.printStackTrace(); }
        }
    }


    protected void set_lists(){
        if(arrayList.size() > 0) {
            adapter = new PDF_Adapter(arrayList, this);
            recyclerView.setAdapter(adapter);
            empty_message.setVisibility(View.GONE);
        }
    }


    protected void set_text(String s, int id) {
        TextView update = (TextView) findViewById(id);
        update.setText(s);
    }


    protected boolean verify_text(String text){
          return text.length() > 0 && text.length() < 20;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init() {
        guest = getIntent().getBooleanExtra("Guest", false);
            if (!guest) {
                ImageView img = this.findViewById(R.id.imageView);
                String welcome_text = getIntent().getStringExtra("Welcome");
                String email = getIntent().getStringExtra("Email");
                Bitmap btm = getIntent().getParcelableExtra("profile_photo");
                img.setImageBitmap(btm);
                set_text("Welcome, " + welcome_text, R.id.textView2);
                set_text(email, R.id.textView4);
            } else {
                set_text("Welcome, Guest", R.id.textView2);
                set_text("No mail address", R.id.textView4);
            }
        setAdapter();
        data_photo = new ArrayList<>();
        pdf_locations = new ArrayList<>();
        empty_message = (TextView) findViewById(R.id.empty_mes);
        if(!guest) {
            try {
                FirebaseApp.initializeApp(this.getApplicationContext());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            storage = FirebaseStorage.getInstance();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Reset_all extends AsyncTask<String, Integer, Boolean> {
        CustomLoadingDialog general;
        Reset_all(CustomLoadingDialog obj){ general = obj; general.LoadingDialog(); }
        protected Boolean doInBackground(String... strings) {
            int i = 0;
            while(i < pdf_locations.size()){
                if(!guest){
                    FileVerifier exists_file = new FileVerifier(pdf_locations.get(i), UUID);
                    Sync_Exists_File obj = new Sync_Exists_File(exists_file);
                    obj.start();
                    try {
                        obj.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(exists_file.getResult()) {
                        Sync_DeleteDB sync_deleteDB = new Sync_DeleteDB(new DeleteDB(pdf_locations.get(i), UUID));
                        sync_deleteDB.start();
                    }
                }
                delete(i, false);
                i++;
            }
            clearAppData();
            return true;
        }
        protected void onProgressUpdate(Integer... progress) { this.general.setProgressBar(progress[0]); }
        protected void onPostExecute(Boolean result) { }
    }


        @SuppressLint("NonConstantResourceId")
        public boolean onContextItemSelected(MenuItem item) {
            int position = current_position.get();
            switch (item.getItemId()) {
                case R.id.delete_id:
                    AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
                    builder.setCancelable(true);
                    builder.setMessage("Do you really want to delete the selected item?");
                    builder.setNegativeButton("No", (dialog, id) -> {
                        dialog.cancel();
                    });
                    builder.setPositiveButton("Yes", (dialog, id) -> {
                        CustomLoadingDialog delete_msg = new CustomLoadingDialog(scanner, "The selected data will be deleted, please wait.");
                        new Delete(delete_msg, position).execute();
                        dialog.cancel();
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    break;

                case R.id.share_id:
                    share(position);
                    break;

                case R.id.open_id:
                    open(position);
                    break;
            }
            return true;

        }


    @SuppressLint("StaticFieldLeak")
    private class Sync_File extends AsyncTask<String, Integer, Boolean> {
        CustomLoadingDialog general;
        Sync_File(CustomLoadingDialog obj){
                 general = obj;
                 general.LoadingDialog();
        }
        protected Boolean doInBackground(String... strings) {
            if(!guest) {
                Sync_Thread obj = new Sync_Thread(new Sync_data(UUID));
                obj.start();
                while(obj.isAlive()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            Read_files read_files = new Read_files(pdf_locations, arrayList);
            pdf_locations = read_files.getLocations();
            arrayList =  read_files.getPdf_scanned();
            Sync_Read_Files sync_read_files = new Sync_Read_Files(read_files);
            sync_read_files.start();
            try {
                sync_read_files.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            after_sort();
            return true;
        }
        protected void onProgressUpdate(Integer... progress) { this.general.setProgressBar(progress[0]); }
        protected void onPostExecute(Boolean result) { this.general.stop(); set_lists(); }
    }

    @SuppressLint("StaticFieldLeak")
    public class Delete extends AsyncTask<String, Integer, Boolean> {
        CustomLoadingDialog general;
        int position;
        Delete(CustomLoadingDialog obj, int position){
            general = obj;
            this.position = position;
            general.LoadingDialog();
        }
        protected Boolean doInBackground(String... strings) {
            if(!guest){
                FileVerifier exists_file = new FileVerifier(pdf_locations.get(position), UUID);
                Sync_Exists_File obj = new Sync_Exists_File(exists_file);
                obj.start();
                if(exists_file.getResult()) {
                    Sync_DeleteDB sync_deleteDB = new Sync_DeleteDB(new DeleteDB(pdf_locations.get(position), UUID));
                    sync_deleteDB.start();
                    try {
                        sync_deleteDB.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            delete(position, true);
            return true;
        }
        protected void onProgressUpdate(Integer... progress) { this.general.setProgressBar(progress[0]); }
        protected void onPostExecute(Boolean result) { this.general.stop(); refresh();}
    }


    @SuppressLint("StaticFieldLeak")
    private class Sign_Out extends AsyncTask<String, Integer, Boolean> {
        CustomLoadingDialog general;
        Sign_Out(CustomLoadingDialog obj){
            general = obj;
            general.LoadingDialog();
        }
        protected Boolean doInBackground(String... strings) {
            int i = 0;
            while(i < pdf_locations.size()){ delete(i, false); }
            clearAppData();
            return true;
        }
        protected void onProgressUpdate(Integer... progress) { this.general.setProgressBar(progress[0]); }
        protected void onPostExecute(Boolean result) { }
    }


    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        BottomNavigationView mainBottomnav = findViewById(R.id.nav_view);
        init();
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mainBottomnav.setOnNavigationItemSelectedListener(
                    item -> {
                        switch (item.getItemId()) {
                            case R.id.homeButton:
                                 break;
                            case R.id.mediaButton:
                                startActivityForResult(new Intent(Scanner.this, Multiple_scan.class), 1);
                                break;
                            case R.id.settingsButton:
                                break;
                        }
                        return false;
                    });
        }
        else{
            Button scanButton = (Button) findViewById(R.id.mediaButton);
            scanButton.setOnClickListener(v -> startActivityForResult(new Intent(Scanner.this, Multiple_scan.class), 1));
        }
        UUID = getIntent().getStringExtra("UUID");
        CustomLoadingDialog sync = new CustomLoadingDialog(this);
        new Sync_File(sync).execute();
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener((RecyclerView recyclerView, int position, View v) -> {
            open(position);
        });

        ItemClickSupport.addTo(recyclerView).setOnItemLongClickListener((RecyclerView recyclerView, int position, View v) -> {
            current_position.set(position);
            return false;
        });
        registerForContextMenu(recyclerView);
        scanner = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
       // MenuItem Open = contextMenu.add(Menu.NONE, 1, 1, "Open");
       // MenuItem Delete = contextMenu.add(Menu.NONE, 2, 2, "Delete");
       // MenuItem Share = contextMenu.add(Menu.NONE, 3, 3, "Share");
       // Open.setOnMenuItemClickListener(onEditMenu);
       // Delete.setOnMenuItemClickListener(onEditMenu);
       // Share.setOnMenuItemClickListener(onEditMenu);
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scanner_main, contextMenu);
    }


    private void upload_pdf(String location) throws Exception{
        Uri file = Uri.fromFile(new File(location));
        StorageReference storageRef = storage.getReference();
        UploadTask uploadTask = storageRef.child("data/" + UUID + "/" + file.getLastPathSegment()).putFile(file);
        uploadTask.addOnProgressListener(taskSnapshot -> { }).addOnPausedListener(taskSnapshot -> Log.d("", "Upload is paused")).addOnFailureListener(exception -> { }).addOnSuccessListener(taskSnapshot -> { });
    }

    private void clearAppData() {
        try {
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager)getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    void refresh(){
        finish();
        startActivity(getIntent());
    }


    @SuppressLint("NonConstantResourceId")
    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    if (guest) {
                        Alert_Generator ag = new Alert_Generator(this, "You are logged as guest", Alert_Generator.OK);
                        ag.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setCancelable(true);
                        builder.setMessage("Do you really want to sign out?");
                        builder.setNegativeButton("No", (dialog, id) -> {
                            dialog.cancel();
                        });
                        builder.setPositiveButton("Yes", (dialog, id) -> {
                            CustomLoadingDialog delete_msg = new CustomLoadingDialog(this, "Signing out, please wait.");
                            new Sign_Out(delete_msg).execute();
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                    return true;
                case R.id.refresh:
                    refresh();
                    return true;
                case R.id.reset:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(true);
                    builder.setMessage("Do you really want to reset the selected item?");
                    builder.setNegativeButton("No", (dialog, id) -> {
                        dialog.cancel();
                    });
                    builder.setPositiveButton("Yes", (dialog, id) -> {
                        CustomLoadingDialog delete_msg = new CustomLoadingDialog(this, "The all data will be deleted, please wait.");
                        new Reset_all(delete_msg).execute();
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }




    @SuppressLint("IntentReset")
    protected void open(int position){
        String loc = pdf_locations.get(position);
        File outputFile = new File(loc);
        if(outputFile.exists()) {
            Uri uri = FileProvider.getUriForFile(this, this.getPackageName() + ".provider", outputFile);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setType("application/pdf");
            try {
                startActivity(Intent.createChooser(intent, null));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No Application available to view pdf", Toast.LENGTH_LONG).show();
            }
        }
    }


    protected void delete(int position, boolean notification) {
        String loc = pdf_locations.get(position);
        File outputFile = new File(loc);
        try {
            if (outputFile.exists() && !outputFile.delete()) throw new Exception();
            arrayList.remove(position);
            pdf_locations.remove(position);
        }
        catch (Exception e){
             e.getMessage();
             return;
        }
       if(notification) {
           Notifications ng = new Notifications(this, "The file has been successfully deleted");
           ng.show();
       }
    }


    @SuppressLint("IntentReset")
    protected void share(int position){
        String loc = pdf_locations.get(position);
        File outputFile = new File(loc);
        if(outputFile.exists()) {
            Uri uri = FileProvider.getUriForFile(this, this.getPackageName() + ".provider", outputFile);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setData(uri);
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.setType("application/pdf");
            startActivity(Intent.createChooser(sendIntent, null));
        }
    }


    protected String CreatePDFandSave(String filename, Document standard) {
            String Save_document;
            try {
                File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data", "/PDFScanner");
                if (!dir.exists() && !dir.mkdirs()) throw new Exception();
                Save_document = getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner" + "/" + filename + ".pdf";
                PdfWriter.getInstance(standard, new FileOutputStream(Save_document));
                standard.open();
                for (int i = 0; i < data_photo.size(); i++) {
                    Bitmap bitmap = data_photo.get(i);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    Image img = Image.getInstance(bytes.toByteArray());
                    img.setAlignment(Image.BOX);
                    standard.add(img);
                    standard.newPage();
                }
                standard.close();
                File t = new File(Save_document);
                if (!t.exists()) throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return Save_document;
    }

   @SuppressLint("DefaultLocale")
   protected String size_conversion(int size){
        if(size >= 1024 && size < 1024 * 1024) return String.format("%.2f", (float) size / (1024)) + " KB";
        if(size >= 1024 * 1024) return String.format("%.2f", (float) size / (1024 * 1024)) + " MB";
        return size + " B";
   }



    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                data_photo = Multiple_scan.getter();
                Document document = new Document();
                String loc = CreatePDFandSave("PDF_" + datetimeFormat.format(calendar.getTime()), document);
                if(!guest) {
                    try {
                        upload_pdf(loc);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                pdf_locations.add(loc);
                long len = new File(loc).length();
                if(len != 0){
                    arrayList.add(new PDF_Scanned("PDF_" + datetimeFormat.format(calendar.getTime()) + ".pdf", size_conversion((int) len), len));
                    if (arrayList.size() != 0) {
                        empty_message.setVisibility(View.GONE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            arrayList.sort(new PDF_Scanned_operator());
                        }
                    }
                    adapter = new PDF_Adapter(arrayList, this);
                    recyclerView.setAdapter(adapter);
                }
            }
        }
    }
}
