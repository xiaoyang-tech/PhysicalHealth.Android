package com.example.xyphysicalhealth;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.luoye.bzmedia.BZMedia;
import com.luoye.bzmedia.BuildConfig;
import com.tencent.mmkv.MMKV;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import java.io.File;
import java.util.ArrayList;
import cn.xymind.happycat.util.CameraCheckUtil;
import cn.xymind.happycat.util.Constant;
import cn.xymind.happycat.util.FileUtil;
import cn.xymind.happycat.util.PermissionUtil;

public class MainActivity extends AppCompatActivity {
    public static String studyPath;
    public static String STUDY_PATH = "mobile20210122.dat";

    private String TAG ="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        init();
        checkSupport();
    }

    /**
     * 检查设备是否支持
     */
    private void checkSupport(){
        int cameraCheckResult = CameraCheckUtil.isSupportSDK(this);
        switch (cameraCheckResult){
            case Constant.CHECK_CAMERA.GOOD:
                Log.e(TAG,"设备支持，可以测量");
                break;
            case Constant.CHECK_CAMERA.NOT_SUPPORT_CAMERA2:
                Log.e(TAG,"设备不支持，VERSION.SDK_INT < 21，不支持Camera2");
                break;
            case Constant.CHECK_CAMERA.MAX_PIXEL_LESS:
                Log.e(TAG,"设备不支持，相机最大像素小于200万");
                break;
            case Constant.CHECK_CAMERA.ISO_ADJUSTABLE_RANGE_UNAVAILABLE:
                Log.e(TAG,"设备不支持，相机不能调整ISO");
                break;
            case Constant.CHECK_CAMERA.HARDWARE_LEVEL_LESS:
                Log.e(TAG,"设备不支持，相机级别不够");
                break;
            case Constant.CHECK_CAMERA.NOT_ENOUGH_FPS:
                Log.e(TAG,"设备不支持，相机不满足最低30FPS");
                break;
        }
    }
    private void init(){
        MMKV.initialize(this);
        BZMedia.init(this, BuildConfig.DEBUG);
        if (FileUtil.copyFileOrDir(STUDY_PATH, getApplication())) {
            studyPath = this.getFilesDir().getAbsolutePath() + File.separator +  STUDY_PATH;
            Log.e(TAG, studyPath);
        }
    }

    public void Measure(View view) {
        startActivity(new Intent(MainActivity.this, HealthMeasureActivity.class));
    }

    private boolean requestPermission() {
        ArrayList<String> permissionList = new ArrayList<>();
        if (!PermissionUtil.isPermissionGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!PermissionUtil.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!PermissionUtil.isPermissionGranted(this, Manifest.permission.CAMERA)) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (!PermissionUtil.isPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }

        String[] permissionStrings = new String[permissionList.size()];
        permissionList.toArray(permissionStrings);

        if (permissionList.size() > 0) {
            PermissionUtil.requestPermission(this, permissionStrings, PermissionUtil.CODE_REQ_PERMISSION);
            return false;
        } else {
            Log.d(TAG,"Have all permissions");
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG,"Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG,"OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    //openCV4Android 需要加载用到
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

}