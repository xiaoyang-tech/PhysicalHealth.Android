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
import cn.xymind.happycat.callback.CloudAnalyzerResultListener;
import cn.xymind.happycat.callback.CollectorListener;
import cn.xymind.happycat.callback.MNNFaceDetectListener;
import cn.xymind.happycat.callback.MeasureProcessListener;
import cn.xymind.happycat.callback.SaveVideoListener;
import cn.xymind.happycat.enums.MeasureState;
import cn.xymind.happycat.enums.WebError;
import cn.xymind.happycat.helper.Camera1Helper;

import static com.example.xyphysicalhealth.MainActivity.studyPath;


public class HealthMeasureActivity extends AppCompatActivity implements OnCameraStateListener,
        MeasureProcessListener, CollectorListener, MNNFaceDetectListener, CloudAnalyzerResultListener, SaveVideoListener {

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
                this, this, this, this, true,this);
    }

    public void startMeasurement(View view) {
        if (camera1Helper != null) {
            camera1Helper.startMeasurement();
        }
    }

    public void stopMeasurement(View view) {
        if (camera1Helper != null) {
            camera1Helper.stopMeasurement(null);
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
    public void onResult(String s) {
        Log.e(TAG,s);
    }

    @Override
    public void onError(WebError webError) {
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
    public void measureStart() {
        Log.e(TAG,"measureStart");
    }

    @Override
    public void measureStop(MeasureState measureState) {
        Log.e(TAG,"measureStop"+measureState);
    }

    //SaveVideoListener
    @Override
    public void onRecordComplete(String path) {
        Log.e(TAG,"录制成功"+path);
    }

    @Override
    public void onStartRecord(String path) {
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
}