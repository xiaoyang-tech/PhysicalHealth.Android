# 小阳健康测量SDK Android示例

## 1. 简介
### 1.1 产品概述
开发者使用PhysicalHealth SDK for Android来创建应用程序，使用传统的数码摄像头采集的（或预先录制好的）视频流来提取被测者的面部血流数据，然后将这些数据打包成我们的测量数据（二进制数据集）格式，发送到小阳健康云引擎进行处理。小阳云引擎是一个强大的基于云端的情感人工智能平台，它利用创新的面部血流成像技术分析人的生理和心理状态。

### 1.2 授权方式

授权项|内容|备注
:-|:-|:-
AppId||
SdkKey||

*以上内容需要联系管理员获取。*

### 1.3 环境要求
#### 1.3.1 软件系统
Android minSdkVersion 21
#### 1.3.2 硬件指标
摄像头要求如下：
* Camera 帧率 ≥ 30FPS
* Camera 像素 ≥ 200万


```java
CameraCheckResult cameraCheckResult = CameraCheckUtil.isSupportSDK(this);
  switch (cameraCheckResult){
    case GOOD:
      Log.e(TAG,"设备支持，可以测量");
      break;
    case NOT_SUPPORT_CAMERA2:
      Log.e(TAG,"设备不支持，VERSION.SDK_INT < 21，不支持Camera2");
      break;
    case MAX_PIXEL_LESS:
      Log.e(TAG,"设备不支持，相机最大像素小于200万");
      break;
    case ISO_ADJUSTABLE_RANGE_UNAVAILABLE:
      Log.e(TAG,"设备不支持，相机不能调整ISO");
      break;
    case HARDWARE_LEVEL_LESS:
      Log.e(TAG,"设备不支持，相机级别不够");
      break;
    case NOT_ENOUGH_FPS:
      Log.e(TAG,"设备不支持，相机不满足最低30FPS");
      break;
}
```

**市面上Android机型繁多，各手机厂商对原生摄像头存在不同程度的定制和校正，以上导致了采集自摄像头原始数据存在可用度低甚至部丢失的问题。同时可能会导致SDK对部分机型存在兼容性问题，如遇到类似情况请与我们反馈，感谢您的理解与支持。**

## 2. 接入须知
### 2.1 SDK
#### 2.1.1 SDK地址
* [Sample演示](https://github.com/xiaoyang-tech/PhysicalHealth.Android)

#### 2.1.2 目录结构
Sample项目说明

文件名|说明
:-|:-
libs |所需aar文件
assets |所需资源文件
MainActivity.java |进行一些初始化工作
HealthMeasureActivity.java |测量页面

### 2.2 流程

### 2.2.1 流程控制
<img src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/db0b5e34cd214649be154f1e854a9fe9~tplv-k3u1fbpfcp-zoom-1.image" style="width:50%" />

* 开始测量时SDK会自动进行鉴权和状态检查。
* 测量过程中SDK会周期(5s)性进行数据分析，数据质量满足条件时会产生一个临时周期测量结果。
* 每次测量持续时间为30s，测量结束后SDK将汇总出完整测量结果。
* 测量对环境光线有较高要求，建议在光线明亮且背景较纯净的环境中进行测量。

### 2.2.2 SDK接入

#### 1) 引入aar

将aar文件拷贝到libs目录下添加以下代码，添加所有的aar以及jar依赖

```java
implementation fileTree(dir: 'libs', include: ['*.jar','*.aar'])
```

#### 2) 拷贝资源文件
将`mobile20210122.dat`拷贝到`assets`目录下，此处的`studyPath`后续需要用到。

```java
public static String studyPath;
public static String STUDY_PATH = "mobile20210122.dat";

if (FileUtil.copyFileOrDir(STUDY_PATH,getApplication())) {
    studyPath = this.getFilesDir().getAbsolutePath() + File.separator + STUDY_PATH;
}
```
参数名|类型|含义
:-|:-|:-
path |String|assets目录下资源文件名称
application |Application| 用于获取assets目录


#### 3) 登录、鉴权以及初始化对象

创建一个`PhysicalHealth`对象,传入`AppId`,`SdkKey`，`studyPath`进行初始化，是否录制视频以及回调可根据需要设置
```java
private PhysicalHealth physicalHealth;
physicalHealth =  new PhysicalHealth.Builder()
                .AppId(AppId)//必填
                .SdkKey(SdkKey)//必填
                .studyPath(MainActivity.studyPath)//必填
                .recordVideo(false)
                .CloudAnalyzerResultListener(this)
                .CollectorListener(this)
                .MNNFaceDetectListener(this)
                .HttpResultListener(this)
                .RecordVideoListener(this)
                .MeasureProcessListener(this)
                .build();
```

参数名|类型|含义
:-|:-|:-
AppId|String| |
SdkKey|String| |
studyPath|String|模型文件地址
recordVideo|boolean|是否录制视频

可以设置`httpResultListener`获得登录以及鉴权的结果
```java
public interface HttpResultListener {

    void onHttpSuccess(int code);

    void onHttpError(int requestCode, int responseCode, String body);
}
```
参数名|类型|含义
:-|:-|:-
code|int|success中返回101代表登录成功，102代表鉴权成功
requestCode|int|返回101代表登录失败，102代表鉴权失败
responseCode|int|http响应码
body|String|返回对应的错误消息

# 4)  测量
将原始的camera1摄像头nv21格式的数据传入
```java
if (physicalHealth != null) {
    physicalHealth.onPreviewDataUpdate(data,width,height,displayOrientation,cameraId,previewFormat);
}
```
在合适的时候开始测量
```java
if(physicalHealth != null) {
    physicalHealth.startMeasurement();
}
```
在必要的时候结束测量

```java
if (physicalHealth != null) {
    physicalHealth.stopMeasurement();
}
```
当视频帧检测不符合要求时，会自动停止测量

销毁测量对象
```java
if (physicalHealth != null) {
    physicalHealth.onDestroy();
    physicalHealth = null;
}
```

#### 5) 结果

可在`CloudAnalyzerResultListener`的`onCloudAnalyzerResult`中获得云端返回结果

### 2.3 功能接口
在上述流程中，SDK公布了以下事件，开发者可以通过注册相应事件来实现流程干预控制。

#### 2.3.1 测量过程
```java
public interface MeasureProcessListener {

    //测量开始之前
    default FaceDetect onMeasureFaceDetect(){return null;}

    //测量结束或者中止
    void onMeasureStop(MeasureState measureState );

    //测量过程中更新数据
    default void onMeasureUpdate(String message){}

}
 
```
#### 2.3.2 人脸检测
```java
public interface MNNFaceDetectListener {
    //识别到人脸，可能有多个
    default void onFaceDetected(int faceNumber){}
    //未识别到人脸
    default void onNoFaceDetected(){}
}
```
#### 2.3.3 云端分析数据
```java
public interface CloudAnalyzerResultListener {

     //测量唯一ID
     default void onMeasurementId(String measurementId){}

     //开始分析数据
     default void onStartAnalyzing() {}

     //每次分析结束
     default void onEachFinishAnalyzing() {}

     //获得返回结果
     default void onCloudAnalyzerResult(String result){}

     //服务端返回错误消息
     default void onCloudAnalyzerError(WebError error){}
}

```
#### 2.3.4 视频帧检测
```java
public interface CollectorListener {

    //对视频帧的检测结果
    void onConstraintReceived(ConstraintResult status);

    //生成payload
    default void onChunkPayloadReceived(ChunkPayload chunkPayload,long chunkNumber){}

    //视频帧和耗时
    default void onFrameRateEvent(double frameRate, Long frameTimestamp){}

}
```

#### 2.3.5 摄像头数据
```java
public interface OnCameraStateListener {

	//预览成功
    void onPreviewSuccess(Camera camera, int width, int height);

	//预览失败
    void onPreviewFail(String message);

	//摄像头原始nv21格式数据
    void onPreviewDataUpdate(byte[] data, int width, int height, int displayOrientation, int cameraId);

	//摄像头关闭
    void onCameraClose();
}
```
#### 2.3.6 视频录制(可设置)

```java
public interface RecordVideoListener {

     //录制成功 返回最终视频路径
     void onRecordComplete(String videoPath);

     //开始录制 返回临时视频路径
     default void onRecordStart(String tempVideoPath){}

     //取消录制 返回是否删除成功
     default void onRecordCanceled(boolean isDelete){}
}


```

### 2.3 测量指标
参数名|类型|含义|重度|中度|轻度|良好|优秀
:-|:-|:-|:-|:-|:-|:-|:-
healtH_SCORE|float|综合健康分数|[0,20)|[20,40)|[40,60)|[60,80)|[80,100]
bP_STROKE|float|中风风险|[6.0,7.5]|[4.5,6.0)|[3.0,4.5)|[1.5,3.0)|[0.0,1.5)
msi|float|精神压力|[5,5.9]|[4.0-5.0)|[3.0-4.0)|[2.0-3.0)|[1.0-2.0)
deviceId|String|设备唯一标识|
hR_BPM|float|心率|||[40,60)U[100,140]||[60,100)
bP_RPP|float|心脏压力|[4.2,4.5]|[4.1,4.2)|[3.9,4.1)|[3.8,3.9)|[3.5,3.8)
rri|float|心理压力值|[4.0,6.0)|[3.0,4.0)||[1.0,3.0)|[0.0,1.0)
bP_CVD|float|心血管疾病风险| [12.00,15.00)|[9.00,12.00)|[6.00,9.00)|[3.00,6.00)|[0.00,3.00)
bP_HEART_ATTACK|float|心脏病风险| [6.0,7.5]|[4.5,6.0)|[3.0,4.5)|[1.5,3.0)|[0.0,1.5)
bP_SYSTOLIC|float|收缩压|[140,170]||[70,90)U[130,140)|[110,130)|[90,110)
bP_TAU|float|血管功能| [0.0,0.8)|[0.8,1.2)|[1.2,1.6)|[1.6,2.2)|[2.2,3.0]
bP_PP|float|脉压|||||
age|float|皮肤年龄|[1,150]|
bmi|float|体重指数|[35,45]|[30,35)|[12,18)U[25,30)||[18,25)
bP_DIASTOLIC|float|舒张压|[90,100]||[50,60)U[80,90)|[70,80)|[60,70)

注意：回传的数据，请根据需要进行解析，因视频质量原因，可能会有个别参数值为0或为空

## 3. 常见问题
### 3.1 FAQ
#### Q：测量异常中断

MeasureState|停止原因
:-|:-
NV21NULL|摄像头数据为空
USER_STOP|用户主动中止，不想继续测量
COMPLETED|测量完成
MNN_NULL|人脸引擎没有初始化
COLLECT_ERROR|收集器错误
STUDY_FILE_ERROR|加载学习文件失败

关于视频质量不佳的具体原因会在`CollectorListener`接口的`onConstraintReceived`会返回客户端对视频帧检测的结果，如果视频帧符合要求则返回`Good`,如果不符合则返回`Error`,并给出错误原因,如:

Error|说明
:-|:-
FACE_FAR|人脸过远
FACE_DIRECTION|人脸方向不正
FACE_MOVEMENT|人脸晃动
FACE_NONE|人脸丢失
FACE_OFFTARGET|人脸超出范围
IMAGE_BRIGHT|画面过亮
IMAGE_DARK|画面过暗
IMAGE_QUALITY|画面质量不佳
IMAGE_EMPTY|画面为空
IMAGE_BACKLIT|画面背景过亮
LOW_FPS|FPS低
CAMERA_MOVEMENT|相机晃动

#### Q：光线不足
除了客户端会对视频质量进行检测，云端也会对客户端传递过来的数据进行检测，如果云端返回`SNR`低，说明视频质量不高，则没有必要继续进行测量，客户端则停止测量


### 3.2 错误码
错误|错误码|含义
:-|:-|:-
UNAUTHRIZED|401|未授权

