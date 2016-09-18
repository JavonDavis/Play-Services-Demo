package com.javon.playservicesdemo.facedetection;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.javon.playservicesdemo.R;

import java.io.File;

public class FaceDetectionActivity extends AppCompatActivity {

    private static String LOG_TAG = "FaceDetection";

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private RelativeLayout container;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        container = (RelativeLayout) findViewById(R.id.activity_face_detection);

    }

    public void takePicture(View view) {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        Log.w(LOG_TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(container, "Need camera.",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", listener)
                .show();
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    private void dispatchTakePictureIntent() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo;
        try
        {
            // place where to store camera taken picture
            photo = createTemporaryFile("picture", ".jpg");
            photo.delete();
        }
        catch(Exception e)
        {
            Log.v(LOG_TAG, "Can't create file to take picture!");
            Toast.makeText(this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
            return ;
        }
        mImageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(LOG_TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Camera permission granted, fire off the intent");
            dispatchTakePictureIntent();
            return;
        }

        Log.e(LOG_TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Detection sample")
                .setMessage("No camera")
                .setPositiveButton("OK", listener)
                .show();
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
                imageBitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);

                if(imageBitmap != null) {
                    StringBuilder faceInfo = new StringBuilder();

                    FaceDetector faceDetector = new FaceDetector.Builder(this)
                            .setProminentFaceOnly(true)
                            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                            .build();

                    if(!faceDetector.isOperational()) {
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


                    Frame imageFrame = new Frame.Builder()
                            .setBitmap(imageBitmap)
                            .build();

                    SparseArray<Face> faces = faceDetector.detect(imageFrame);

                    for (int i = 0; i < faces.size(); i++) {
                        Face face = faces.get(faces.keyAt(i));

                        String message = "Face was detected... ";
                        if(face.getIsSmilingProbability() == Face.UNCOMPUTED_PROBABILITY) {
                            message += "But we could not tell if it was happy or not :/";
                        }
                        else if(face.getIsSmilingProbability() < 0.5) {
                            message += "and it wasn't so happy";
                        }
                        else {
                            message += "and it was really happy!";
                        }
                        faceInfo.append(message);
                        faceInfo.append("\n\n");
                    }

                    result = faceInfo.toString();

                    if (result.isEmpty()) {
                        result = "Detected no Face!";
                    }

                }
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Failed to load", e);
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Face Detection sample")
                    .setMessage(result)
                    .setPositiveButton("OK", null)
                    .show();

        }
    }
}
