package com.scanlibrary;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import java.io.IOException;

public class PickImageFragment extends Fragment {

    private View view;
    private ImageButton cameraButton;
    private IScanner scanner;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }
        this.scanner = (IScanner) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.pick_image_fragment, null);
        init();
        return view;
    }

    private void init() {
        cameraButton = (ImageButton) view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new CameraButtonClickListener());
        if (isIntentPreferenceSet()) {
            handleIntentPreference();
        } else {
            getActivity().finish();
        }
    }



    private void handleIntentPreference() {
        int preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        if (preference == ScanConstants.OPEN_CAMERA) {
            openCamera();
        } else if (preference == ScanConstants.OPEN_MEDIA) {
            openMediaContent();
        }
    }

    private boolean isIntentPreferenceSet() {
        return getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0) != 0;
    }

    private class CameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            openCamera();
        }
    }


    public void openCamera() {
         startActivityForResult( new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), ScanConstants.OPEN_CAMERA);
    }

    public void openMediaContent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        if (resultCode == Activity.RESULT_OK) {
            try {
                switch (requestCode) {
                    case ScanConstants.OPEN_CAMERA:
                        bitmap = (Bitmap) data.getExtras().get("data");
                        break;

                    case ScanConstants.PICKFILE_REQUEST_CODE:
                        bitmap = getBitmap(data.getData());
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().finish();
            }
        } else {
            getActivity().finish();
        }
        if (bitmap != null) {
            postImagePick(bitmap);
        }
    }

    protected void postImagePick(Bitmap bitmap) {

        Uri uri = Utils.getUri(getActivity(), bitmap);
        bitmap.recycle();
        scanner.onBitmapSelect(uri);
    }

    private Bitmap getBitmap(Uri selectedimg) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        AssetFileDescriptor fileDescriptor = getActivity().getContentResolver().openAssetFileDescriptor(selectedimg, "r");
        return BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
    }
}