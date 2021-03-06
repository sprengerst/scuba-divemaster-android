package inc.together.scuba;

import android.Manifest;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
    private ImageView mPreviewImage;

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

        immersiveFullScreenMode();

        setContentView(R.layout.activity_main);


        TedPermission.with(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {}

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        MainActivity.this.finish();
                    }
                })
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();


        mCameraView = findViewById(R.id.camera);
        mShutterTimer = findViewById(R.id.countdown_textview);
        mShutterTimer.setVisibility(View.GONE);

        mPreviewImage = findViewById(R.id.preview_image);
        mPreviewImage.setVisibility(View.GONE);

        mCameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                // Create a bitmap or a file...
                CameraUtils.decodeBitmap(picture, new CameraUtils.BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
                        ImageSaveUtils.saveImage(MainActivity.this, bitmap);
                        Log.d("MainActivity", "Image taking finished!");
                        mPreviewImage.setImageBitmap(bitmap);
                        mPreviewImage.setVisibility(View.VISIBLE);
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

    // This work only for android 4.4+
    private void immersiveFullScreenMode() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {

            final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
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

            //if (Math.abs(last_z - z) > 2.2F) => Tilt
            //else if (Math.abs(last_y-y) > 1.3F) => horizontal shake


            float value1 = Math.abs(last_y-y);
            float value2 = Math.abs(last_z-z);



            if ( value1 > 3.2F && value1 > value2){
                Log.d("MainActivity", "Shake orientation: horizontal");

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
            }else if (value2 > 3.2F && value2 > value1)  {
                Log.d("MainActivity", "Shake orientation: vertical");

                if (mCameraView.getFacing() == Facing.BACK) {
                    mCameraView.setFacing(Facing.FRONT);
                } else {
                    mCameraView.setFacing(Facing.BACK);
                }

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
