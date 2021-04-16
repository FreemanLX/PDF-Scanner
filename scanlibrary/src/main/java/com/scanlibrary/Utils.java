package com.scanlibrary;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Utils {
    private Utils() { }
    public static Uri getUri(Context context, Bitmap bitmap){
        return getUri(context, bitmap, 100);
    }

    @NonNull
    public static Uri getUri(Context context, Bitmap bitmap, int quality) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null));
    }

    public static Bitmap getBitmap(Context context, Uri uri) throws IOException {
        return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
    }
}