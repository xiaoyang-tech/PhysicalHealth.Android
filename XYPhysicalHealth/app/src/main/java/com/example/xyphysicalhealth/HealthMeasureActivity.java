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
import cn.xymind.happycat.enums.MeasureState;
import cn.xymind.happycat.enums.WebError;
import cn.xymind.happycat.helper.PhysicalHealth;

public class HealthMeasureActivity extends AppCompatActivity implements OnCameraStateListener,
        MeasureProcessListener, CollectorListener, MNNFaceDetectListener, CloudAnalyzerResultListener, RecordVideoListener, HttpResultListener {

    private String TAG= "HealthMeasureActivity";
    private PhysicalHealth camera1Helper;
    private BZCameraView bzCameraView;
    private int previewFormat = ImageFormat.NV21;
    private String userName ="your userName";
    private String password ="your password";
    private String license="your license";
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
        camera1Helper =  new PhysicalHealth.Builder()
                .username(userName)
                .password(password)
                .license(license)
                .studyPath(MainActivity.studyPath)
                .recordVideo(true)//录制视频true
                .CloudAnalyzerResultListener(this)
                .CollectorListener(this)
                .MNNFaceDetectListener(this)
                .HttpResultListener(this)
                .RecordVideoListener(this)
                .MeasureProcessListener(this)
                .build();
    }

    public void startMeasurement(View view) {
        if (camera1Helper != null) {
            camera1Helper.startMeasurement();
        }
    }

    public void stopMeasurement(View view) {
        if (camera1Helper != null) {
            camera1Helper.interruptMeasurement();
        }
    }

    //OnCameraStateListener
    @Override
    public void onPreviewSuccess(Camera camera, int width, int height) {

        if (camera1Helper != null) {
            camera1Helper.onPreviewSuccess();
        }
    }

    @Override
    public void onPreviewDataUpdate(byte[] data, int width, int height, int displayOrientation, int cameraId) {
        if (camera1Helper != null) {
            camera1Helper.onPreviewDataUpdate(data,width,height,displayOrientation,cameraId,previewFormat);
        }
    }

    @Override
    public void onPreviewFail(String message) { }

    @Override
    public void onCameraClose() { }

    //CloudAnalyzerResultListener

    @Override
    public void onCloudAnalyzerResult(String s) {
        Log.e(TAG,s);
    }

    @Override
    public void onCloudAnalyzerError(WebError webError) {
        Log.e(TAG,"错误码: "+webError.getErrorCode());
    }

    //CollectorListener
    @Override
    public void onConstraintReceived(ConstraintResult constraintStatus) {
        if(constraintStatus.status !=ConstraintResult.ConstraintStatus.Good){
            Log.e(TAG,constraintStatus.toString());
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
    public void onMeasureUpdate(String m){

        Log.e(TAG,"onMeasureUpdate "+m);
    }
    @Override
    public void onMeasureStop(MeasureState measureState) {
        Log.e(TAG,"measureStop "+measureState);
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
        if (camera1Helper != null) {
            camera1Helper.onDestroy();
            camera1Helper = null;
        }
        super.onDestroy();
    }

    @Override
    public void onHttpSuccess(int requestCode) {

    }

    @Override
    public void onHttpError(int requestCode, int responseCode, String body) {

    }
}