package com.example.pdfscanner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import com.example.pdfscanner.ui.reload_variables;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;
import java.io.IOException;
import java.util.ArrayList;

public class Multiple_scan extends AppCompatActivity {

    private static ArrayList<Bitmap> arr_bitmap;

    public class image_document_scanned {
        Bitmap bitmap;
        image_document_scanned(Bitmap bitmap){
            this.bitmap = bitmap;
        }
        public Bitmap getBitmap() {
            return bitmap;
        }
    }

    ArrayList<image_document_scanned> arrayList = new ArrayList<>();
    class image_adapter extends ArrayAdapter<image_document_scanned> {
        private Context context;
        private int resource;
        public image_adapter(@NonNull Context context, int resource, @NonNull ArrayList<image_document_scanned> objects) {
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
            ImageView img = convertView.findViewById(R.id.imglist);
            img.setImageBitmap(getItem(position).getBitmap());
            return convertView;
        }
    }

    Button cameraButton, mediaButton, savedButton;
    GridView gridView;
    image_adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_scan);
        init();
        if(savedInstanceState != null){
            arrayList = new ArrayList<>();
            arrayList = reload_variables.Getter();
            adapter = new image_adapter(this, R.layout.grid_view_element, arrayList);
        }
        gridView.setOnItemClickListener((parent, view, position, id) -> {
             arrayList.remove(position);
             adapter = new image_adapter(this, R.layout.grid_view_element, arrayList);
             gridView.setAdapter(adapter);
        }
        );
        if(adapter != null) {
            gridView.setAdapter(adapter);
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        if(width > height) {
            ViewGroup.LayoutParams params = gridView.getLayoutParams();
            params.width = width/2;
            gridView.setLayoutParams(params);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        reload_variables x = new reload_variables(arrayList);
    }


    private class ScanButtonClickListener implements View.OnClickListener {
        private final int preference;
        public ScanButtonClickListener(int preference) {
            this.preference = preference;
        }
        @Override
        public void onClick(View v) {
            startScan(preference);
        }
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, 99);
    }

    protected void init(){
        cameraButton = (Button) findViewById(R.id.button4);
        cameraButton.setOnClickListener(new ScanButtonClickListener((ScanConstants.OPEN_CAMERA)));
        mediaButton = (Button) findViewById(R.id.button2);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        savedButton = (Button) findViewById(R.id.button3);
        savedButton.setOnClickListener(v -> save_document());
        gridView = (GridView) findViewById(R.id.gridview);
        arr_bitmap = new ArrayList<>();
    }

    private void save_document(){
        for(image_document_scanned img : arrayList){
              arr_bitmap.add(img.getBitmap());
        }

        if(arr_bitmap.size() > 0)
              setResult(RESULT_OK);
          else
              setResult(RESULT_CANCELED);
          finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    arrayList.add(new image_document_scanned(bitmap));
                    adapter = new image_adapter(this, R.layout.grid_view_element, arrayList);
                    gridView.setAdapter(adapter);
                    getContentResolver().delete(uri, null, null);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ArrayList<Bitmap> getter(){
           return arr_bitmap;
    }

}