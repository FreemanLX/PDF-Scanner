package com.example.pdfscanner;
import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity<mAuth> extends AppCompatActivity{
    private static final String TAG = "";
    protected  GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    protected boolean storage_perm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        FirebaseApp.initializeApp(this.getApplicationContext());
        try {
            mAuth = FirebaseAuth.getInstance();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestProfile()
                .requestEmail()
                .build();
        configureNextButton();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        if(!isStoragePermissionGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 1);
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            try {
                connection.connect();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

     Bitmap btm;
    protected void onMoving(FirebaseUser account) throws IOException {
        Intent scanner = new Intent(this, Scanner.class);
        String name = account.getDisplayName();
        scanner.putExtra("Welcome", name);
        String email = account.getEmail();
        scanner.putExtra("Email", email);
        String UUID = account.getUid();
        scanner.putExtra("UUID", UUID);
        Uri photo = account.getPhotoUrl();
        scanner.putExtra("permission", storage_perm);
        ExecutorService threadpool = Executors.newCachedThreadPool();
        Future<Bitmap> futureTask = threadpool.submit(() -> getBitmapFromURL(photo.toString()));
        try {
            btm = futureTask.get();
        }
        catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }
        threadpool.shutdown();
        scanner.putExtra("profile_photo", btm);
        startActivity(scanner);
    }



    @Override
    protected void onStart() {
        FirebaseUser currentUser = null;
        try {
            currentUser = mAuth.getCurrentUser();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if(currentUser != null){
            try {
                onMoving(currentUser);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        super.onStart();
    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        try {
                            assert user != null;
                            onMoving(user);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }


    protected void configureNextButton() {
        Button button = (Button) findViewById(R.id.button);
        Button guest = (Button) findViewById(R.id.button5);
        button.setOnClickListener(v -> signIn());
        guest.setOnClickListener(v -> guest());
    }

    public void guest(){
        Intent scanner = new Intent(this, Scanner.class);
        scanner.putExtra("Guest", true);
        startActivity(scanner);
    }

    public final int cks(String self) {
        return checkCallingPermission(self);
    }

    public boolean isStoragePermissionGranted() {
        return cks(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED
                && cks(Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
                && cks(Manifest.permission.CAMERA) == PERMISSION_GRANTED;
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