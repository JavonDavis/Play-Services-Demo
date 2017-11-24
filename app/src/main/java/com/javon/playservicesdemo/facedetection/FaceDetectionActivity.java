package com.javon.playservicesdemo.facedetection;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.javon.playservicesdemo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FaceDetectionActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int RC_HANDLE_WRITE_EXTERNAL_STORAGE = 3;
    private static String LOG_TAG = "FaceDetection";

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private RelativeLayout container;
    private ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        container = (RelativeLayout) findViewById(R.id.activity_face_detection);
        imageView = (ImageView) findViewById(R.id.imageView);
//        requestCameraPermission();
//        requestExternalStoragePermission();



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

    private void requestExternalStoragePermission() {
        Log.w(LOG_TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_WRITE_EXTERNAL_STORAGE);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_WRITE_EXTERNAL_STORAGE);
            }
        };

        Snackbar.make(container, "Need camera.",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", listener)
                .show();
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

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private Bitmap convert(Bitmap bitmap, Bitmap.Config config) {
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
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


            try {
                imageBitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
//                BitmapFactory.Options bitmapFatoryOptions=new BitmapFactory.Options();
//                bitmapFatoryOptions.inPreferredConfig=Bitmap.Config.RGB_565;
//                imageBitmap= BitmapFactory.decodeFile(mCurrentPhotoPath, bitmapFatoryOptions);


                if (imageBitmap != null) {
                    ExifInterface ei = new ExifInterface(mImageUri.getPath());
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap rotatedBitmap = null;
                    switch(orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(imageBitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(imageBitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(imageBitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotatedBitmap = rotateImage(imageBitmap, 270);
                    }
                    StringBuilder faceInfo = new StringBuilder();

                    FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                            .setTrackingEnabled(false)
                            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                            .build();

                    // This is a temporary workaround for a bug in the face detector with respect to operating
                    // on very small images.  This will be fixed in a future release.  But in the near term, use
                    // of the SafeFaceDetector class will patch the issue.
                    Detector<Face> faceDetector = new SafeFaceDetector(detector);

                    if (!faceDetector.isOperational()) {
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
                            Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show();
                            Log.w(LOG_TAG, "Low Storage");
                        }
                    }

                    rotatedBitmap = rotatedBitmap.copy(Bitmap.Config.RGB_565, false);
//                    InputStream stream = getResources().openRawResource(R.raw.face);
//                    rotatedBitmap = BitmapFactory.decodeStream(stream);
                    imageView.setImageBitmap(rotatedBitmap);
                    Frame imageFrame = new Frame.Builder()
                            .setBitmap(rotatedBitmap)
                            .build();

                    SparseArray<Face> faces = faceDetector.detect(imageFrame);

                    for (int i = 0; i < faces.size(); i++) {
                        Face face = faces.get(faces.keyAt(i));

                        String message = "Face was detected... ";
                        if (face.getIsSmilingProbability() == Face.UNCOMPUTED_PROBABILITY) {
                            message += "But we could not tell if it was happy or not :/";
                        } else if (face.getIsSmilingProbability() < 0.5) {
                            message += "and it wasn't so happy";
                        } else {
                            message += "and it was really happy!";
                        }
                        faceInfo.append(message);
                        faceInfo.append("\n\n");
                    }

                    result = faceInfo.toString();

                    if (result.isEmpty()) {
                        result = "Detected no Face!";
                    }

                    faceDetector.release();

                }
            } catch (Exception e) {
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
