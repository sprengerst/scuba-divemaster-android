package inc.together.scuba;

import android.Manifest;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private CameraView mCameraView;
    private SensorManager mSensorMgr;
    private Sensor mDefaultAccMeter;

    private long mShakeTimestamp;
    private int mShakeCount;
    float x, y, z;
    float last_x, last_y, last_z;
    private TextView mShutterTimer;

    private final int COUNTDOWN_IMAGE_CAPTURE = 3;
    private static final float SHAKE_THRESHOLD_GRAVITY = 1.4F;
    private static final int SHAKE_SLOP_TIME_MS = 500;

    private boolean mShakeEnabled = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        TedPermission.with(this)
//                .setPermissionListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted() {}
//
//                    @Override
//                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                        MainActivity.this.finish();
//                    }
//                })
//                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
//                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
//                .check();


        mCameraView = findViewById(R.id.camera);
        mShutterTimer = findViewById(R.id.countdown_textview);
        mShutterTimer.setVisibility(View.GONE);

        mCameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                // Create a bitmap or a file...
                CameraUtils.decodeBitmap(picture, new CameraUtils.BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
                        ImageSaveUtils.saveImage(bitmap);
                        Log.d("MainActivity", "Image taking finished!");
                    }
                });

            }
        });


        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorMgr != null) {
            mDefaultAccMeter = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorMgr.registerListener(this, mDefaultAccMeter, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();

        if (mSensorMgr != null) {
            mSensorMgr.registerListener(this, mDefaultAccMeter, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.stop();

        if (mSensorMgr != null) {
            mSensorMgr.unregisterListener(this, mDefaultAccMeter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.destroy();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        last_x = x;
        last_y = y;
        last_z = z;

        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement.
        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

//        Log.d("MainActivity", "Gforce Value: " + gForce);
        if (mShakeEnabled && gForce > SHAKE_THRESHOLD_GRAVITY) {
            final long now = System.currentTimeMillis();

            // ignore shake events too close to each other (500ms)
            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return;
            }

            mShakeTimestamp = now;
            mShakeCount++;

            Log.d("MainActivity", "Shake detected: " + mShakeCount);
            Log.d("MainActivity", "X/Y/Z: " + x + "/" + y + "/" + z);
            Log.d("MainActivity", "LAST X/Y/Z: " + last_x + "/" + last_y + "/" + last_z);

            if (Math.abs(last_z - z) <= 1.8F) {
                Log.d("MainActivity", "Shake orientation: horizontal");

                if (mCameraView.getFacing() == Facing.BACK) {
                    mCameraView.setFacing(Facing.FRONT);
                } else {
                    mCameraView.setFacing(Facing.BACK);
                }

            } else {
                Log.d("MainActivity", "Shake orientation: vertical");

                mShakeEnabled = false;
                mShutterTimer.setVisibility(View.VISIBLE);

                CountDownAnimation countDownAnimation = new CountDownAnimation(mShutterTimer, COUNTDOWN_IMAGE_CAPTURE);
                countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
                    @Override
                    public void onCountDownEnd(CountDownAnimation animation) {
                        mCameraView.capturePicture();
                        mShakeEnabled = true;
                        mShutterTimer.setVisibility(View.GONE);
                    }
                });

                countDownAnimation.start();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
