package com.example.xyphysicalhealth;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.luoye.bzcamera.BZCameraView;
import com.luoye.bzcamera.listener.OnCameraStateListener;
import ai.nuralogix.dfx.ConstraintResult;
import cn.xymind.happycat.bean.FaceDetect;
import cn.xymind.happycat.callback.CloudAnalyzerResultListener;
import cn.xymind.happycat.callback.CollectorListener;
import cn.xymind.happycat.callback.HttpResultListener;
import cn.xymind.happycat.callback.MNNFaceDetectListener;
import cn.xymind.happycat.callback.MeasureProcessListener;
import cn.xymind.happycat.callback.RecordVideoListener;

import cn.xymind.happycat.helper.PhysicalHealth;

public class HealthMeasureActivity extends AppCompatActivity implements OnCameraStateListener,
        MeasureProcessListener, CollectorListener, MNNFaceDetectListener, CloudAnalyzerResultListener, RecordVideoListener, HttpResultListener {

    private String TAG= "HealthMeasureActivity";
    private PhysicalHealth physicalHealth;
    private BZCameraView bzCameraView;
    private int previewFormat = ImageFormat.NV21;
    private String AppId ="your AppId";
    private String SdkKey ="your SdkKey";

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
        physicalHealth =  new PhysicalHealth.Builder()
                .activity(this)//必填
                .AppId(AppId)//必填
                .SdkKey(SdkKey)//必填
                .studyPath(MainActivity.studyPath)//必填
                .recordVideo(false)//录制视频true,默认为false
                .CloudAnalyzerResultListener(this)
                .CollectorListener(this)
                .MNNFaceDetectListener(this)
                .HttpResultListener(this)
                .RecordVideoListener(this)
                .MeasureProcessListener(this)
                .build();
    }

    public void startMeasurement(View view) {
        if (physicalHealth != null) {
            physicalHealth.startMeasurement();
        }
    }

    public void stopMeasurement(View view) {
        if (physicalHealth != null) {
            physicalHealth.interruptMeasurement();
        }
    }

    //OnCameraStateListener
    @Override
    public void onPreviewSuccess(Camera camera, int width, int height) {
        if (physicalHealth != null) {
            physicalHealth.onPreviewSuccess();
        }
    }

    @Override
    public void onPreviewDataUpdate(byte[] data, int width, int height, int displayOrientation, int cameraId) {
        if (physicalHealth != null) {
            physicalHealth.onPreviewDataUpdate(data,width,height,displayOrientation,cameraId,previewFormat);
        }
    }

    @Override
    public void onPreviewFail(String message) { }

    @Override
    public void onCameraClose() { }

    //CloudAnalyzerResultListener

    @Override
    public void onCloudAnalyzerResult(String result) {
        Log.e(TAG,"onCloudAnalyzerResult "+result);
    }


    //CollectorListener
    @Override
    public void onConstraintReceived(ConstraintResult constraintStatus) {
        if(constraintStatus.status !=ConstraintResult.ConstraintStatus.Good){
            Log.e(TAG,constraintStatus.toString());
            //对视频帧的检测,error要停止UI
        }
    }
    //MNNFaceDetectListener
    @Override
    public void onNoFaceDetected() {
        Log.e(TAG,"onNoFaceDetected");
    }

    //MeasureProcessListener
    @Override
    public FaceDetect onMeasureFaceDetect() {
        Log.e(TAG,"onMeasureFaceDetect");
        return null;
    }

    @Override
    public void onMeasureStop(String msg) {
        Log.e(TAG,"onMeasureStop "+msg);
    }

    @Override
    public void onMeasureStart() {
        Log.e(TAG,"onMeasureStart");
    }

    //RecordVideoListener
    @Override
    public void onRecordComplete(String path) {
        Log.e(TAG,"录制成功 "+path);
    }

    @Override
    public void onRecordStart(String path) {
        Log.e(TAG,"开始录制 "+path);
    }

    @Override
    public void onRecordCanceled(boolean b) {
        Log.e(TAG,"取消录制 是否已经删除"+b);
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
        if (physicalHealth != null) {
            physicalHealth.onDestroy();
            physicalHealth = null;
        }
        super.onDestroy();
    }

    @Override
    public void onHttpSuccess(int requestCode) {
        Log.e(TAG,"onHttpSuccess"+requestCode);
    }

    @Override
    public void onHttpError(int requestCode, int responseCode, String body) {
        Log.e(TAG,"onHttpError"+requestCode+" "+responseCode+" "+body);
    }
}