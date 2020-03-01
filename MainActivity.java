package com.example.facerecognitionemojikeyboard;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.aflak.ezcam.EZCam;
import me.aflak.ezcam.EZCamCallback;

public class MainActivity extends Activity implements EZCamCallback, View.OnLongClickListener {
    private TextureView textureView;

    private EZCam cam;
    private SimpleDateFormat dateFormat;
    private String mFilename;
    private final String TAG = "CAM";
    private Parcelable mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView) findViewById(R.id.textureView);
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());

        cam = new EZCam(this);
        cam.setCameraCallback(this);

        String id = cam.getCamerasList().get(CameraCharacteristics.LENS_FACING_FRONT);
        cam.selectCamera(id);

        Dexter.withActivity(MainActivity.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                cam.open(CameraDevice.TEMPLATE_PREVIEW, textureView);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Log.e(TAG, "permission denied");
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    public boolean onLongClick(View v) {
        cam.takePicture();
        return false;
    }

    @Override
    public void onCameraReady() {
        cam.setCaptureSetting(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);
        cam.startPreview();

        textureView.setOnLongClickListener(this);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cam.takePicture();
    }

    @Override
    public void onPicture(Image image) {
        cam.stopPreview();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

        finish();
    }

    @Override
    public void onCameraDisconnected() {
        Log.e(TAG, "Camera disconnected");
    }

    @Override
    public void onError(String message) {
        Log.e(TAG, message);
    }

    @Override
    protected void onDestroy() {
        cam.close();
        super.onDestroy();
    }

    @Override
    public void finish() {
        // Unpack the receiver.
        ResultReceiver receiver =
                getIntent().getParcelableExtra(SimpleIME.KEY_RECEIVER);

        Bundle resultData = new Bundle();

//        resultData.putString(SimpleIME.KEY_MESSAGE, mFilename);
        resultData.putParcelable(SimpleIME.KEY_MESSAGE, mBitmap);

        receiver.send(SimpleIME.RESULT_OK, resultData);

        super.finish();
    }

}
