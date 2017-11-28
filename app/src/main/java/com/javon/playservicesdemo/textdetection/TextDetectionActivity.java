package com.javon.playservicesdemo.textdetection;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.javon.playservicesdemo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TextDetectionActivity extends AppCompatActivity {

    private static String LOG_TAG = "TextDetectionActivity";

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_PERMISSIONS = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri mImageUri;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_detection);

        imageView = (ImageView) findViewById(R.id.imageView);

    }

    public void takePicture(View view) {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (cameraPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED) {
            logAndShowSnackbar("Permissions granted");
            dispatchTakePictureIntent();
        } else {
            requestPermissions();
        }
    }

    private void requestPermissions() {
        logAndShowSnackbar("Camera and Write permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ActivityCompat.requestPermissions(this, permissions,
                RC_HANDLE_PERMISSIONS);
    }

    private void logAndShowSnackbar(final String text) {
        Log.w(LOG_TAG, text);
        View container = findViewById(R.id.activity_text_detection);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created

            if (photoFile != null) {
                mImageUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, mImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_PERMISSIONS) {
            Log.d(LOG_TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            logAndShowSnackbar("Permissions granted, fire off the intent");
            dispatchTakePictureIntent();
            return;
        }

        logAndShowSnackbar("Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
    }

    public Bitmap readBitmap(Uri selectedImage) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        AssetFileDescriptor fileDescriptor =null;
        try {
            fileDescriptor = this.getContentResolver().openAssetFileDescriptor(selectedImage,"r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally{
            try {
                bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            String result = "Error retrieving image";

            this.getContentResolver().notifyChange(mImageUri, null);
            ContentResolver cr = this.getContentResolver();
            Bitmap imageBitmap;
            try
            {
                imageBitmap = readBitmap(mImageUri);

                if(imageBitmap != null) {
                    StringBuilder detectedText = new StringBuilder();

                    TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();

                    if(!textRecognizer.isOperational()) {
                        // Note: The first time that an app using a Vision API is installed on a
                        // device, GMS will download a native libraries to the device in order to do detection.
                        // Usually this completes before the app is run for the first time.  But if that
                        // download has not yet completed, then the above call will not detect any text,
                        // barcodes, or faces.
                        //
                        // isOperational() can be used to check if the required native libraries are currently
                        // available.  The detectors will automatically become operational once the library
                        // downloads complete on device.
                        Log.w(LOG_TAG, "Detector dependencies are not yet available.");

                        // Check for low storage.  If there is low storage, the native library will not be
                        // downloaded, so detection will not become operational.
                        IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                        boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                        if (hasLowStorage) {
                            Toast.makeText(this,"Low Storage", Toast.LENGTH_LONG).show();
                            Log.w(LOG_TAG, "Low Storage");
                        }
                    }

                    Bitmap rotatedBitmap = rotateImage(imageBitmap, 90);
                    imageView.setImageBitmap(rotatedBitmap);

                    Frame imageFrame = new Frame.Builder()
                            .setBitmap(rotatedBitmap)
                            .build();

                    SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

                    for (int i = 0; i < textBlocks.size(); i++) {
                        TextBlock textBlock = textBlocks.get(i);

                        detectedText.append(textBlock.getValue());
                        detectedText.append("\n");
                    }

                    result = detectedText.toString();

                    if (result.isEmpty()) {
                        result = "Detected no text!";
                    }

                }
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Failed to load", e);
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Text Detection sample")
                    .setMessage(result)
                    .setPositiveButton("OK", null)
                    .show();

        }
    }
}
