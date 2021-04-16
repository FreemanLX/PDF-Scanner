package com.example.pdfscanner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.widget.Button;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.scanlibrary.Utils;
import android.app.Fragment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import static android.content.pm.PackageManager.*;

public class Scanner extends AppCompatActivity {
    private Button scanButton;
    private ListView scannedImageView;
    PDF_Adapter adapter;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;
    ArrayList<Bitmap> data_photo;
    ArrayList<String> pdf_locations;

    public class PDF_Scanned {
        String name, size, date;

        PDF_Scanned(String name, String size, String date) {
            this.name = name;
            this.size = size;
            this.date = date;
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

        public String getDate() {
            return "Date scanned: " + date;
        }
    }

    ArrayList<PDF_Scanned> arrayList = new ArrayList<>();

    public class PDF_Adapter extends ArrayAdapter<PDF_Scanned> {
        private Context context;
        private int resource;

        public PDF_Adapter(@NonNull Context context, int resource, @NonNull ArrayList<PDF_Scanned> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
        }



        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(resource, parent, false);
            TextView name = convertView.findViewById(R.id.textlist);
            TextView size = convertView.findViewById(R.id.textlist_size);
            TextView date = convertView.findViewById(R.id.textlist_date);
            name.setText(getItem(position).getName());
            size.setText(getItem(position).getSize());
            date.setText(getItem(position).getDate());
            return convertView;
        }
    }

    protected void set_text(String s, int id) {
        TextView update = (TextView) findViewById(id);
        update.setText(s);
    }

    private void init() {
        scannedImageView = (ListView) findViewById(R.id.listview);
        scanButton = (Button) findViewById(R.id.mediaButton);
        scanButton.setOnClickListener(v -> {
            Intent move = new Intent(Scanner.this, multiple_document_scanned_mode.class);
            startActivityForResult(move, 1);
        });
        data_photo = new ArrayList<>();
        pdf_locations = new ArrayList<>();
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
        welcome_text = "Welcome, " + welcome_text;
        set_text(welcome_text, R.id.textView2);
        set_text(email, R.id.textView4);
        ImageView img = this.findViewById(R.id.imageView);
        img.setImageBitmap(btm);
        init();
        registerForContextMenu(scannedImageView);
        if(isStoragePermissionGranted() == false) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.scanner_main, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterContextMenuInfo info  = (AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.delete_id:
                delete(info.position);
                return true;

            case R.id.share_id:
                share(info.position);
                return true;

            case R.id.save_id:
                save(info.position);
                return true;

            default:
                return super.onContextItemSelected(item);
        }

    }

    protected void delete(int position) {
        String loc = pdf_locations.get(position);
        File outputFile = new File(loc);
        if(outputFile.exists()) {
            outputFile.delete();
        }
        arrayList.remove(position);
        adapter = new PDF_Adapter(this, R.layout.list_row, arrayList);
        scannedImageView.setAdapter(adapter);
        pdf_locations.remove(position);
    }

    protected void save(int position) {

    }

    protected void share(int position){
        String loc = pdf_locations.get(position);
        File outputFile = new File(loc);
        if(outputFile.exists()) {
            Uri pdfUri = Uri.fromFile(outputFile);
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, pdfUri);
            startActivity(Intent.createChooser(share, "Share the PDF"));
        }
        else{
            return;
        }
    }


    protected Document CreatePDF(Document standard) throws IOException, DocumentException {
        standard.open();
        for (int i = 0; i < data_photo.size(); i++) {
            Bitmap bitmap = data_photo.get(i);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            Image img = Image.getInstance(bytes.toByteArray());
            img.setAlignment(Image.MIDDLE);
            standard.add(img);
        }
        standard.close();
        return standard;
    }

   boolean storage_perm = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                data_photo = data.getParcelableArrayListExtra("output");
                String file_size = null;
                Document document = new Document();
                try {
                    document = CreatePDF(document);
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
                calendar = Calendar.getInstance();
                dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                SimpleDateFormat datenameFormat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
                String loc = writeFileOnInternalStorage("PDF_" + datenameFormat.format(calendar.getTime()), document);
                if(storage_perm) {
                    pdf_locations.add(loc);
                    File file = new File(loc);
                    file_size = String.valueOf(file.length());
                    date = dateFormat.format(calendar.getTime());
                    arrayList.add(new PDF_Scanned("PDF_" + datenameFormat.format(calendar.getTime()), file_size + " B", date));
                    adapter = new PDF_Adapter(this, R.layout.list_row, arrayList);
                    scannedImageView.setAdapter(adapter);
                }
            }
        }
    }

    public final int cks(String self) {
              return checkCallingPermission(self);
    }

    public  boolean isStoragePermissionGranted() {
        return cks(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED
                && cks(Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
                && cks(Manifest.permission.CAMERA) == PERMISSION_GRANTED;
    }


    public String writeFileOnInternalStorage(String sFileName, Document document) {
        if(storage_perm) {
            String savedocument;
            try {
                String savefile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/Data/PDFScanner";
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/Data", "/PDFScanner");
                if (!dir.exists()) {
                    boolean mkdir = dir.mkdirs();
                    if(!mkdir)
                        throw new Exception();
                }
                savedocument = savefile + "/" + sFileName + ".pdf";
                PdfWriter.getInstance(document, new FileOutputStream(savedocument));
                document.open();
                document.addAuthor(author_name);
                document.close();
                File t = new File(savedocument);
                if (!t.exists()) {
                        throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
                storage_perm = false;
                return null;
            }
            return savedocument;
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean ok = true;
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    ok = false;
                    break;
                }
            }
            storage_perm = ok;
        }

        }

}