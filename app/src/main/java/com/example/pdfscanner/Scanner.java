package com.example.pdfscanner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import static android.os.Environment.getExternalStorageDirectory;


public class Scanner extends AppCompatActivity {
    private Button scanButton;
    private ListView scannedImageView;
    PDF_Adapter adapter;
    private Calendar calendar;
    ArrayList<Bitmap> data_photo;
    ArrayList<String> pdf_locations;
    TextView empty_message;
    FirebaseStorage storage;
    private String UUID;

    public static class PDF_Scanned {
        String name, size;
        PDF_Scanned(String name, String size) {
            this.name = name;
            this.size = size;
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

    private void clearAppData() {
        try {
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager)getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear "+packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void sync_files_from_database(){
        try {
            File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data", "/PDFScanner");
            if (!dir.exists() && !dir.mkdirs()) throw new Exception();
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
        StorageReference listRef = storage.getReference();
        Task<ListResult> sfr = listRef.child("data/" + UUID ).listAll().addOnSuccessListener(listResult -> {
            for (StorageReference prefix : listResult.getItems()) {
                    String filename = prefix.getName();
                    File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner/" + filename);
                    if(!dir.exists()) {
                        try {
                            dir.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                prefix.getFile(dir).addOnSuccessListener(taskSnapshot -> {
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });

            }})
                .addOnFailureListener(e -> {
                    // Uh-oh, an error occurred!
                });

        readfiles(false);
    }

    protected void readfiles(boolean upload_to_database){
        String savefile = getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner";
        File dir = new File(savefile);
        File[] listFile = dir.listFiles();
            if (listFile != null) {
                for (File file : listFile) {
                    pdf_locations.add(file.getAbsolutePath());
                    String path = file.getAbsolutePath();
                    Log.d("", path);
                    if(upload_to_database) {
                        Thread x = new Thread(() -> {
                            upload_pdf(path);
                        });
                        x.start();
                        while (x.isAlive()) {

                        }
                    }
                    String filename = path.substring(path.lastIndexOf("/")+1);
                    arrayList.add(new PDF_Scanned(filename, size_conversion((int) file.length())));
                    if (arrayList.size() != 0) empty_message.setVisibility(View.GONE);
                    adapter = new PDF_Adapter(this, R.layout.list_row, arrayList);
                    scannedImageView.setAdapter(adapter);
                }
            }

    }


    protected void set_text(String s, int id) {
        TextView update = (TextView) findViewById(id);
        update.setText(s);
    }

    public void includesForDeleteFiles(String loc) {
            StorageReference storageRef = storage.getReference();
            Uri file = Uri.fromFile(new File(loc));
            StorageReference verify = storage.getReference().child("data/" + UUID + "/" + file.getLastPathSegment());
            verify.delete().addOnSuccessListener(aVoid -> {
            }).addOnFailureListener(Throwable::printStackTrace);
    }


    private boolean Exists_File(String loc){
        storage = FirebaseStorage.getInstance();
        Uri file = Uri.fromFile(new File(loc));
        StorageReference verify = storage.getReference().child("data/" + UUID + "/" + file.getLastPathSegment());
        try {
            verify.getDownloadUrl();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    private void upload_pdf(String location){
               Uri file = Uri.fromFile(new File(location));
               StorageReference storageRef = storage.getReference();
                UploadTask uploadTask = storageRef.child("data/" + UUID + "/" + file.getLastPathSegment()).putFile(file);
                uploadTask.addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                }).addOnPausedListener(taskSnapshot -> Log.d("", "Upload is paused")).addOnFailureListener(exception -> { }).addOnSuccessListener(taskSnapshot -> { });

    }

    private void init() {
        scannedImageView = (ListView) findViewById(R.id.listview);
        scanButton = (Button) findViewById(R.id.mediaButton);
        scanButton.setOnClickListener(v -> startActivityForResult(new Intent(Scanner.this, multiple_document_scanned_mode.class), 1));
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
   String author_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        String welcome_text = getIntent().getStringExtra("Welcome");
        author_name = welcome_text;
        String email = getIntent().getStringExtra("Email");
        Bitmap btm = getIntent().getParcelableExtra("profile_photo");
        if(btm != null){
            ImageView img = this.findViewById(R.id.imageView);
            img.setImageBitmap(btm);
        }
        if(welcome_text != null) {
            welcome_text = "Welcome, " + welcome_text;
            set_text(welcome_text, R.id.textView2);
        }
        else {
            TextView tw = (TextView) findViewById(R.id.textView2);
            tw.setVisibility(View.GONE);
        }
        if(email != null) set_text(email, R.id.textView4);
        else{
            TextView tw = (TextView) findViewById(R.id.textView4);
            tw.setVisibility(View.GONE);
        }
        init();
        registerForContextMenu(scannedImageView);
        scannedImageView.setOnItemClickListener((parent, view, position, id) -> {
                      open(position);
        });
        UUID = getIntent().getStringExtra("UUID");
        sync_files_from_database();
        storage_perm = getIntent().getBooleanExtra("permission", true);
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

    void refresh(){
        finish();
        startActivity(getIntent());
    }

    void log_out(){
        Thread delete_files_from_storage = new Thread( () -> {
            for (int i = 0; i < pdf_locations.size(); i++) {
                delete(i, false, false);
            }
            clearAppData();
        });
        delete_files_from_storage.start();
        while(delete_files_from_storage.isAlive()){ }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                log_out();
                return true;
            case R.id.refresh:
                refresh();
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
                delete(info.position, true, true);
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
        if(!db) {
            if (Exists_File(loc)) {
                Thread delete_from_database = new Thread(() -> {
                    includesForDeleteFiles(loc);
                });
                delete_from_database.start();
            }
        }
        File outputFile = new File(loc);
        try {
            if (outputFile.exists()) if (!outputFile.delete()) throw new Exception();
            arrayList.remove(position);
            adapter = new PDF_Adapter(this, R.layout.list_row, arrayList);
            scannedImageView.setAdapter(adapter);
            pdf_locations.remove(position);
            if (arrayList.size() == 0) empty_message.setVisibility(View.VISIBLE);
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
        if(storage_perm) {
            String savedocument;
            try {
                File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/Android/Data", "/PDFScanner");
                if (!dir.exists() && !dir.mkdirs()) throw new Exception();
                savedocument = getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner" + "/" + filename + ".pdf";
                PdfWriter.getInstance(standard, new FileOutputStream(savedocument));
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
                File t = new File(savedocument);
                if (!t.exists()) throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
                storage_perm = false;
                return null;
            }
            return savedocument;
        }
        return null;
    }

   @SuppressLint("DefaultLocale")
   protected String size_conversion(int size){
        if(size >= 1024 && size < 1024 * 1024) return String.format("%.2f", (float) size / (1024)) + " KB";
        if(size >= 1024 * 1024) return String.format("%.2f", (float) size / (1024 * 1024)) + " MB";
        return size + " B";
   }

   boolean storage_perm = false;
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        calendar = Calendar.getInstance();
        SimpleDateFormat datenameFormat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                storage_perm = true;
                Thread get_array_parceable = new Thread( () -> {
                    data_photo = multiple_document_scanned_mode.getter();
                });
                get_array_parceable.start();
                while(get_array_parceable.isAlive()) { }
                Document document = new Document();
                String loc = null;
                loc = CreatePDFandSave("PDF_" + datenameFormat.format(calendar.getTime()), document);
                if(storage_perm) {
                    upload_pdf(loc);
                    pdf_locations.add(loc);
                    File file = new File(loc);
                    arrayList.add(new PDF_Scanned("\nPDF_" + datenameFormat.format(calendar.getTime()) + ".pdf", size_conversion((int) file.length())));
                    if(arrayList.size() != 0) empty_message.setVisibility(View.GONE);
                    adapter = new PDF_Adapter(this, R.layout.list_row, arrayList);
                    scannedImageView.setAdapter(adapter);
                }
            }
        }
    }
}