package com.example.pdfscanner;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "";
    protected  GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        configureNextButton();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
    protected void onMoving(GoogleSignInAccount account) throws IOException {

        Intent scanner = new Intent(this, Scanner.class);
        scanner.putExtra("Welcome", account.getGivenName());
        scanner.putExtra("Email", account.getEmail());
        Uri photo = account.getPhotoUrl();
        ExecutorService threadpool = Executors.newCachedThreadPool();
        Future<Bitmap> futureTask = threadpool.submit(() -> getBitmapFromURL(photo.toString()));
        while (!futureTask.isDone()) {
            System.out.println("FutureTask is not finished yet...");
        }
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
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            try {
                onMoving(account);
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
            handleSignInResult(task);
            GoogleSignInAccount data_google = task.getResult();
            try {
                onMoving(data_google);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    protected void configureNextButton() {
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(v -> signIn());
    }
}