package com.example.xyphysicalhealth;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.luoye.bzcamera.BZCameraView;
import com.luoye.bzcamera.listener.OnCameraStateListener;

import ai.nuralogix.dfx.ConstraintResult;
import cn.xymind.happycat.callback.CloudAnalyzerResultListener;
import cn.xymind.happycat.callback.CollectorListener;
import cn.xymind.happycat.callback.MNNFaceDetectListener;
import cn.xymind.happycat.callback.MeasureFaceListener;
import cn.xymind.happycat.enums.WebError;
import cn.xymind.happycat.helper.Camera1Helper;

import static com.example.xyphysicalhealth.MainActivity.studyPath;


public class HealthMeasureActivity extends AppCompatActivity implements OnCameraStateListener,
        MeasureFaceListener, CollectorListener, MNNFaceDetectListener, CloudAnalyzerResultListener {

    private String TAG= "HealthMeasureActivity";
    private Camera1Helper camera1Helper;
    private BZCameraView bzCameraView;
    private int previewFormat = ImageFormat.NV21;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_measure);
        initView();
    }

    private void initView(){
        bzCameraView = findViewById(R.id.bz_camera_view);
        bzCameraView.setPreviewTargetSize(480, 640);
        bzCameraView.setPreviewFormat(previewFormat);
        bzCameraView.setOnCameraStateListener(this);
        initCamera();
    }

    private void initCamera(){
        camera1Helper = new Camera1Helper(studyPath,
                this,this,this,this);
    }

    public void startMeasurement(View view) {
        if (camera1Helper != null) {
            camera1Helper.startMeasurement();
        }
    }

    public void stopMeasurement(View view) {
        if (camera1Helper != null) {
            camera1Helper.stopMeasurement("");
        }
    }

    ////OnCameraStateListener/////////////////////////
    @Override
    public void onPreviewSuccess(Camera camera, int width, int height) {

        if (camera1Helper != null) {
            camera1Helper.onPreviewSuccess();
        }
    }

    @Override
    public void onPreviewDataUpdate(byte[] data, int width, int height, int displayOrientation, int cameraId) {
        if (camera1Helper != null) {
            camera1Helper.onPreviewDataUpdate(data,width,height,displayOrientation,cameraId);
        }
    }

    @Override
    public void onPreviewFail(String message) { }

    @Override
    public void onCameraClose() { }

/////CloudAnalyzerResultListener////////////////////

    @Override
    public void onResult(String s) {
        Log.e(TAG,s);
    }

    @Override
    public void onError(WebError webError) {

    }

    ////DfxCollectorListener///////////////////////////////
    @Override
    public void onConstraintReceived(ConstraintResult constraintStatus) {
        if(constraintStatus.status !=ConstraintResult.ConstraintStatus.Good){
            Log.e(TAG,constraintStatus.toString());
        }
    }
    ////MNNFaceDetectListener/////////////
    @Override
    public void onNoFaceDetected() {
        Log.e(TAG,"onNoFaceDetected");
    }
    ////MeasureFaceListener/////////////
    @Override
    public void measureStop(String message){
        Log.e(TAG,"measureStop"+message);
    }

    @Override
    public void measureStart() {
        Log.e(TAG,"measureStart");
    }
    @Override
    protected void onPause() {
        super.onPause();
        bzCameraView.onPause();
        stopMeasurement(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bzCameraView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (camera1Helper != null) {
            camera1Helper.onDestroy();
            camera1Helper = null;
        }
        super.onDestroy();
    }

}