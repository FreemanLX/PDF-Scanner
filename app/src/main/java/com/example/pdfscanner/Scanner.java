package com.example.pdfscanner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import static android.os.Environment.getExternalStorageDirectory;


public class Scanner extends AppCompatActivity {
    private ListView scannedImageView;
    PDF_Adapter adapter;
    ArrayList<Bitmap> data_photo;
    ArrayList<String> pdf_locations;
    TextView empty_message;
    FirebaseStorage storage;
    private String UUID;

    public static class PDF_Scanned {
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

    ArrayList<PDF_Scanned> arrayList = new ArrayList<>();

    public static class PDF_Adapter extends ArrayAdapter<PDF_Scanned> {
        private final Context context;
        private final int resource;
        public PDF_Adapter(@NonNull Context context, int resource, @NonNull ArrayList<PDF_Scanned> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
        }

        @SuppressLint("ViewHolder")
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(resource, parent, false);
            TextView name = convertView.findViewById(R.id.textlist);
            TextView size = convertView.findViewById(R.id.textlist_size);
            name.setText(getItem(position).getName());
            size.setText(getItem(position).getSize());
            return convertView;
        }
    }


    class PDF_Scanned_operator implements Comparator<PDF_Scanned> {
        public int compare(Scanner.PDF_Scanned obj, Scanner.PDF_Scanned obj1) {
            long obj_size = obj.getSizeInt();
            long obj1_size = obj1.getSizeInt();
            return Long.compare(obj_size, obj1_size);
        }
    }

    protected void sync_files_from_database() {
        Thread thread = new Thread( () -> {
        try {
            File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data", "/PDFScanner");
            if (!dir.exists() && !dir.mkdirs()) throw new Exception();
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
        StorageReference listRef = storage.getReference();
        listRef.child("data/" + UUID ).listAll().addOnSuccessListener(listResult -> {
            for (StorageReference prefix : listResult.getItems()) {
                    String filename = prefix.getName();
                    File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner/" + filename);
                    if(!dir.exists()) {
                        try {
                            if(!dir.createNewFile()) throw new Exception();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    prefix.getFile(dir).addOnSuccessListener(taskSnapshot -> { });
            }});});
        thread.start();
        while(thread.isAlive()){
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e){
                 e.printStackTrace();
            }
        }
    }

    protected void Read_files(){
        Thread thread_for_read = new Thread ( ()-> {
            File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner");
            File[] listFile = dir.listFiles();
            if (listFile != null) {
                for (File file : listFile) {
                    pdf_locations.add(file.getAbsolutePath());
                    long len = file.length();
                    arrayList.add(new PDF_Scanned(file.getName(), size_conversion((int) len), len));
                }
            }
        });
        thread_for_read.start();
        while(thread_for_read.isAlive()){
             try{
                 Thread.sleep(1000);
             }
             catch (Exception e){
                 e.printStackTrace();
             }

        }
    }

    protected void after_sort(){
        Thread preparing_for_sort = new Thread( ()->{
        if(arrayList.size() > 0) {
            try {
                qsort(arrayList, new PDF_Scanned_operator());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        } });
        preparing_for_sort.start();
        while (preparing_for_sort.isAlive()){
            try {
                Thread.sleep(1000);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    protected void set_lists(){
        if(arrayList.size() > 0) {
            adapter = new PDF_Adapter(this, R.layout.list_row, arrayList);
            scannedImageView.setAdapter(adapter);
            empty_message.setVisibility(View.GONE);
        }
    }


    protected void set_text(String s, int id) {
        TextView update = (TextView) findViewById(id);
        update.setText(s);
    }

    protected void hide_text(int id){
        TextView tw = (TextView) findViewById(id);
        tw.setVisibility(View.GONE);
    }

    protected boolean verify_text(String text){
          return text.length() > 0 && text.length() < 20;
    }

    private void init() {
        String welcome_text = getIntent().getStringExtra("Welcome");
        String email = getIntent().getStringExtra("Email");
        Bitmap btm = getIntent().getParcelableExtra("profile_photo");
        ImageView img = this.findViewById(R.id.imageView);
        img.setImageBitmap(btm);
        if(verify_text(welcome_text) && verify_text(email)){
            set_text("Welcome, " + welcome_text, R.id.textView2);
            set_text(email, R.id.textView4);
        }
        if(verify_text(welcome_text) && !verify_text(email)){
              set_text("Welcome, " + welcome_text, R.id.textView4);
              hide_text(R.id.textView2);
        }

        scannedImageView = (ListView) findViewById(R.id.listview);
        Button scanButton = (Button) findViewById(R.id.mediaButton);
        scanButton.setOnClickListener(v -> startActivityForResult(new Intent(Scanner.this, Multiple_scan.class), 1));
        data_photo = new ArrayList<>();
        pdf_locations = new ArrayList<>();
        empty_message = (TextView) findViewById(R.id.empty_mes);
        try {
            FirebaseApp.initializeApp(this.getApplicationContext());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        storage = FirebaseStorage.getInstance();
    }

    @SuppressLint("StaticFieldLeak")
    private class Sync_File extends AsyncTask<String, Integer, Boolean> {
        Custom_loading_dialog general;
        Sync_File(Custom_loading_dialog obj){
                 general = obj;
                 general.LoadingDialog();
        }
        protected Boolean doInBackground(String... strings) { sync_files_from_database(); Read_files(); after_sort(); return true; }
        protected void onProgressUpdate(Integer... progress) { this.general.setProgressBar(progress[0]); }
        protected void onPostExecute(Boolean result) { this.general.stop(); set_lists(); }
    }

    @SuppressLint("StaticFieldLeak")
    private class Delete extends AsyncTask<String, Integer, Boolean> {
        Custom_loading_dialog general;
        int position;
        Delete(Custom_loading_dialog obj, int position){
            general = obj;
            this.position = position;
            general.LoadingDialog();
        }
        protected Boolean doInBackground(String... strings) { delete(position, true, true); return true; }
        protected void onProgressUpdate(Integer... progress) { this.general.setProgressBar(progress[0]); }
        protected void onPostExecute(Boolean result) { this.general.stop(); refresh();}
    }


    @SuppressLint("StaticFieldLeak")
    private class Reset_all extends AsyncTask<String, Integer, Boolean> {
        Custom_loading_dialog general;
        Reset_all(Custom_loading_dialog obj){ general = obj; general.LoadingDialog(); }
        protected Boolean doInBackground(String... strings) {
            int i = 0;
            while(i < pdf_locations.size()){ delete(i, true, false); }
            clearAppData();
            return true;
        }
        protected void onProgressUpdate(Integer... progress) { this.general.setProgressBar(progress[0]); }
        protected void onPostExecute(Boolean result) { }
    }

    @SuppressLint("StaticFieldLeak")
    private class Sign_Out extends AsyncTask<String, Integer, Boolean> {
        Custom_loading_dialog general;
        Sign_Out(Custom_loading_dialog obj){
            general = obj;
            general.LoadingDialog();
        }
        protected Boolean doInBackground(String... strings) {
            int i = 0;
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            while(i < pdf_locations.size()){ delete(i, false, false); }
            clearAppData();
            return true;
        }
        protected void onProgressUpdate(Integer... progress) { this.general.setProgressBar(progress[0]); }
        protected void onPostExecute(Boolean result) { }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        init();
        registerForContextMenu(scannedImageView);
        scannedImageView.setOnItemClickListener((parent, view, position, id) -> open(position));
        UUID = getIntent().getStringExtra("UUID");
        Custom_loading_dialog sync = new Custom_loading_dialog(this);
        new Sync_File(sync).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.scanner_main, menu);
    }


    public void includesForDeleteFiles(String loc) {
        Uri file = Uri.fromFile(new File(loc));
        StorageReference verify = storage.getReference().child("data/" + UUID + "/" + file.getLastPathSegment());
        verify.delete().addOnSuccessListener(aVoid -> {
        }).addOnFailureListener(Throwable::printStackTrace);
    }


    public class Exists_File{
        String loc;
        Exists_File(String loc) { this.loc = loc; }

        public boolean verify(){
            storage = FirebaseStorage.getInstance();
            Uri file = Uri.fromFile(new File(loc));
            StorageReference verify = storage.getReference().child("data/" + UUID + "/" + file.getLastPathSegment());
            try { verify.getDownloadUrl(); } catch(Exception e) { e.printStackTrace(); return false; }
            return true;
        }
    }

    private void upload_pdf(String location){
        Uri file;
        try {
            file = Uri.fromFile(new File(location));
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setNegativeButton(
                "No",
                (dialog, id) -> dialog.cancel());
        switch (item.getItemId()) {
            case R.id.action_settings:
                builder.setMessage("Do you really want to sign out?");
                builder.setPositiveButton(
                        "Yes",
                        (dialog, id) -> {
                            Custom_loading_dialog signout = new Custom_loading_dialog(this, "You'll be sign out in the next moment");
                            new Sign_Out(signout).execute();
                        });
                AlertDialog sgout = builder.create();
                sgout.show();
                return true;
            case R.id.refresh:
                refresh();
                return true;
            case R.id.reset:
                builder.setMessage("Do you really want to reset the data?");
                builder.setPositiveButton(
                        "Yes",
                        (dialog, id) -> {
                            Custom_loading_dialog reset_t = new Custom_loading_dialog(this, "The data will be deleted, please wait");
                            new Reset_all(reset_t).execute();
                        });
                AlertDialog resetout = builder.create();
                resetout.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterContextMenuInfo info  = (AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.delete_id:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Do you really want to delete?");
                builder.setCancelable(true);
                builder.setPositiveButton(
                        "Yes",
                        (dialog, id) -> {
                              Custom_loading_dialog delete_msg = new Custom_loading_dialog(this, "The selected data will be deleted, please wait.");
                              new Delete(delete_msg, info.position).execute();
                        });

                builder.setNegativeButton(
                        "No",
                        (dialog, id) -> dialog.cancel());

                AlertDialog alert11 = builder.create();
                alert11.show();
                return true;

            case R.id.share_id:
                share(info.position);
                return true;

            case R.id.open_id:
                open(info.position);
                return true;

            default:
                return super.onContextItemSelected(item);
        }

    }

    @SuppressLint("IntentReset")
    protected void open(int position){
        String loc = pdf_locations.get(position);
        File outputFile = new File(loc);
        if(outputFile.exists()) {
            Uri uri = FileProvider.getUriForFile(this, this.getPackageName() + ".provider", outputFile);
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setType("application/pdf");
            try {
                startActivity(Intent.createChooser(intent, null));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No Application available to view pdf", Toast.LENGTH_LONG).show();
            }
        }
    }


    protected void delete(int position, boolean db, boolean notification) {
        String loc = pdf_locations.get(position);
        if(db) {
            Thread thread = new Thread( () -> {
                Exists_File object = new Exists_File(loc);
                ExecutorService threadpool = Executors.newCachedThreadPool();
                Future<Boolean> futureTask = threadpool.submit(object::verify);
                boolean retrieve = false;
                try {
                    retrieve = futureTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                threadpool.shutdown();
                if (retrieve)
                    includesForDeleteFiles(loc);
            });
            thread.start();
            while (thread.isAlive()) {
                try {
                   Thread.sleep(2000);
                }
                catch (Exception e){
                   e.printStackTrace();
               }
            }
        }
        File outputFile = new File(loc);
        try {
            if (outputFile.exists()) if (!outputFile.delete()) throw new Exception();
            arrayList.remove(position);
            adapter = new PDF_Adapter(this, R.layout.list_row, arrayList);
            pdf_locations.remove(position);
        }
        catch (Exception e){
             e.getMessage();
             return;
        }
       if(notification) {
           NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "PDFScanner notification")
                   .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                   .setContentTitle("PDFScanner")
                   .setContentText("File " + loc + " had been successfully deleted from our database!")
                   .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                   .setAutoCancel(true);
           NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
           managerCompat.notify(1, builder.build());
           if (Build.VERSION.SDK_INT >= 26) {
               NotificationChannel channel = new NotificationChannel("PDFScanner notification", "PDFScanner", NotificationManager.IMPORTANCE_HIGH);
               NotificationManager manager = getSystemService(NotificationManager.class);
               manager.createNotificationChannel(channel);
           }
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


   protected <T> void swap_array_list(ArrayList<T> array, int pos1, int pos2){
          T temp = array.get(pos1);
          array.set(pos1, array.get(pos2));
          array.set(pos2, temp);
   }

   protected <T> int partition(ArrayList<T> array, int low, int high,  java.util.Comparator<? super T> c){
        T pivot = array.get(high);
        int index = low - 1;
        for (int jndex = low; jndex < high; jndex++){
            try {
                if (c.compare(array.get(jndex), pivot) <= 0) {
                    index++;
                    swap_array_list(array, index, jndex);
                }
            }
            catch (NullPointerException | ClassCastException e){
                    e.printStackTrace();
            }
        }
        swap_array_list(array, index + 1, high);
        return index + 1;
   }

   protected <T> void qsort(ArrayList<T> array, java.util.Comparator<? super T> c) {
            int high = array.size();
            qsort(array, 0, high - 1, c);
   }

   protected <T> void qsort(ArrayList<T> array, int low, int high, java.util.Comparator<? super T> c){
         if(low < high){
             int p = partition(array, low, high, c);
             qsort(array, low, p - 1 , c);
             qsort(array, p + 1, high, c);
         }
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
                upload_pdf(loc);
                pdf_locations.add(loc);
                arrayList.add(new PDF_Scanned("PDF_" + datetimeFormat.format(calendar.getTime()) + ".pdf", size_conversion((int) new File(loc).length()), new File(loc).length()));
                if(arrayList.size() != 0) {
                    empty_message.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        arrayList.sort(new PDF_Scanned_operator());
                    }
                    else{
                        qsort(arrayList, new PDF_Scanned_operator());
                    }
                }
                adapter = new PDF_Adapter(this, R.layout.list_row, arrayList);
                scannedImageView.setAdapter(adapter);
            }
        }
    }
}
