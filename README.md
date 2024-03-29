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
#### 2.2.1 流程控制
<img src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/db0b5e34cd214649be154f1e854a9fe9~tplv-k3u1fbpfcp-zoom-1.image" style="width:50%" />

* 开始测量时SDK会自动进行鉴权和状态检查。
* 测量过程中SDK会周期(5s)性进行数据分析，数据质量满足条件时会产生一个临时周期测量结果。
* 每次测量持续时间为30s，测量结束后SDK将汇总出完整测量结果。
* 测量对环境光线有较高要求，建议在光线明亮且背景较纯净的环境中进行测量。

#### 2.2.2 SDK接入
##### (1) 引入aar

将aar文件拷贝到libs目录下添加以下代码，添加所有的aar以及jar依赖

```java
implementation fileTree(dir: 'libs', include: ['*.jar','*.aar'])
```

##### (2) 拷贝资源文件
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

##### (3) 登录、鉴权以及初始化对象

创建一个`PhysicalHealth`对象,传入`AppId`,`SdkKey`，`studyPath`进行初始化，是否录制视频以及回调可根据需要设置
```java
private PhysicalHealth physicalHealth;
physicalHealth =  new PhysicalHealth.Builder()
                .activity(this)//必填
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

##### (4)  测量
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

##### (5) 结果

可在`CloudAnalyzerResultListener`的`onCloudAnalyzerResult`中获得云端返回结果

### 2.3 功能接口
在上述流程中，SDK公布了以下事件，开发者可以通过注册相应事件来实现流程干预控制。

#### 2.3.1 测量过程
```java
public interface MeasureProcessListener {

    //测量开始之前
    default FaceDetect onMeasureFaceDetect(){return null;}

    //测量结束或者中止
    void onMeasureStop(String measureState );

    void onMeasureStart();

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
    default void onEachFinishAnalyzing(int index, int heartRate) {}

     //获得返回结果
     default void onCloudAnalyzerResult(String result){}

     //服务端返回错误消息
     default void onCloudAnalyzerError(String error){}
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
    default void onFrameRateEvent(double frameRate){}

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

### 2.4 测量指标
#### 2.4.1 指标含义
参数名|类型|含义|重度不健康|中度不健康|轻度不健康|良好|优秀
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

#### 2.4.2 健康建议
综合说明：个人的测量数值会随着情绪变化、运动、饮食、饮酒等个人行为的变化而产生一定程度的波动，这种波动属于正常。建议采用静息状态（饭后半小时以上，运动后1小时以上，静坐5分钟之后）下的坐姿测量，以确保排除干扰因素。

* 综合得分 
    * [0,20)分：重度不健康，建议立刻就医
    * [20,40)分：重度不健康，建议立刻就医
    * [40,60)分：身体状态欠佳，建议尽快去做体检
    * [60,80)分：说明状态一般，需要调整。建议均衡饮食，适度运动。
    * [80,100]分：状态良好，继续保持
* 心率
    * [,60)次/分：心率过缓。可能原因：运动员、先天性心率过缓，建议用户咨询更专业的心脏专家。
    * [60,100)次/分：心率正常，继续保持；
    * [100,]次/分：心率过速。可能原因：饭后、运动之后、身体超重、先天性心率过速等；建议：需要适当的药物调整，建议用户咨询更专业的心脏专家。
  
* 收缩压
    * [,70)mmHg:血压过低，建议咨询大夫进行血压控制。
    * [70,90)mmHg：血压稍低，建议咨询大夫进行血压控制。
    * [90,130)mmHg：血压在正常范围之内。
    * [130,140)mmHg:血压稍高，注意控制血压。
    * [140,]mmHg：高血压，建议用户咨询大夫进行控制血压。需要调整自己的生活方式，比如适度运动、健康饮食（少盐、少油、不含酒精和咖啡因）。
* 舒张压
    * [,50)mmHg:低血压，建议咨询大夫进行血压控制
    * [50,60)mmHg,血压稍低，注意控制血压。
    * [60,80)mmHg：血压在正常范围之内。
    * [80,90)mmHg:血压稍微偏高，注意控制血压。
    * [90,]mmHg：高血压，建议用户咨询大夫进行控制血压。需要调整自己的生活方式，比如适度运动、健康饮食（少盐、少油、不含酒精和咖啡因）。
* 体重指数
    * [,18)：体重过轻；可能原因：遗传基因；消化不良。建议：如有不适，到医院检查，并适度锻炼。
    * [18,25)：完美体形，继续保持
    * [25,30)：体重偏重；可能原因：遗传因素；饮食过量。建议：适度锻炼，控制饮食；
    * [30,35) ： 体重超重；可能原因：遗传因素；饮食过量，偏食严重。建议：到医院检查，减少肥肉、油炸食品、淀粉、糖的摄入量，并适度锻炼。
    * [35,]：体重严重超重，建议联系专业大夫进行处理。
* 血管功能
    * [0,0.8):血管弹性非常弱，建议联系专业大夫进行处理。 
    * [0.8,1.2)：血管弹性弱，晚年容易导致的脑出血；建议适当控制情绪，适度运动，多喝绿茶等软化血管的食品，戒烟戒酒。
    * [1.2,1.6)：血管弹性一般，建议多运动，均衡饮食。
    * [1.6,3]：血管弹性非常好，保持好的生活习惯。 
    * (3,]：血管功能异常，建议联系大夫进行处理
* 心脏压力
    * [,3.5)：心脏负荷异常，建议联系专业大夫进行处理。
    * [3.5,3.9)：心脏负荷适当，心脏功能正常
    * [3.9,4.1)：心脏负荷偏重，注意休息，加强锻炼
    * [4.1,4.2) ：心脏负荷过重，身体可能严重疲劳或过度肥胖，注意休息并加强锻炼 
    * [4.2,]：心脏超负荷运行，建议联系专业大夫进行处理。
* 精神压力：
    * [,2)：精神压力小，心态比较平和，感知力相对低
    * [2,3)：精神状态较好，能够很好的调整心态，既能应对压力，也能享受平静
    * [3,4)：精神压力稍大，请注意调整心态，注意休息，调整心态。
    * [4,5)：精神压力偏大，有些焦虑，敏感，注意自我心理调整和身体锻炼
    * [5,]：精神压力异常，建议联系大夫进行处理

* 心脏病风险
    * [,1.5)：风险很低，请继续保持良好的工作和生活习惯。
    * [1.5,3)：风险较低；请继续保持良好的工作和生活习惯。
    * [3,4.5)：风险较高；建议：适度运动，均衡饮食
    * [4.5,6)：风险偏高；建议：适度运行，均衡饮食，减少脂肪、淀粉、糖含量高的食物的摄入。
    * [6,]：风险很高；可能原因：心脏负荷过重，冠状动脉硬化等；建议：适度运动，减少脂肪、淀粉、糖含量高的食物摄入，戒烟戒酒、戒剧烈运动；寻求专业医生帮助。
* 中风风险 
    * [0,1.5)风险很低；继续保持良好的工作和生活习惯。
    * [1.5,3)：风险较低；继续保持良好的工作和生活习惯。
    * [3,4.5)：风险较高；建议：适度运动，均衡饮食
    * [4.5,6)：风险偏高；建议：适度运行，均衡饮食，减少脂肪、淀粉、糖含量高的食物的摄入。
    * [6,]：风险很高；可能原因：血管内壁的血栓的沉积等；建议：适度运动，减少脂肪、淀粉、糖含量高的食物摄入，戒烟戒酒、戒剧烈运动；寻求专业医生帮助。
* 心血管病风险
    * [0,3)风险很低；继续保持良好的工作和生活习惯。
    * [3,6)：风险较低；继续保持良好的工作和生活习惯
    * [6,9)：风险较高；建议：适度运动，均衡饮食
    * [9,12)：风险偏高；建议：适度运行，均衡饮食，减少脂肪、淀粉、糖含量高的食物的摄入。
    * [12,]：风险很高；可能原因：血管硬化、血管壁磨损等；建议：适度运动，减少脂肪、淀粉、糖含量高的食物摄入，戒烟戒酒、戒剧烈运动；寻求专业医生帮助。

## 3. 常见问题
### 3.1 FAQ
**Q：测量异常中断**

MeasureState|停止原因
:-|:-
USER_STOP|用户主动中止
COMPLETED|测量已完成
MNN_NULL|人脸引擎初始化失败，请联系客服
COLLECT_ERROR|数据采集异常，请联系客服
STUDY_FILE_ERROR|加载学习文件失败，请联系客服
UNKNOWN_ERROR|发生未知异常，请联系客服
NO_FACE_ERROR|人脸丢失，请保持人脸在圆框区域，重新开始测量
WEB_SOCKET_ERROR|测量服务网络连接异常断开，请保持网络畅通并稍后重试
LAST_CHUNK_ERROR|测量服务网络请求不稳定，请保持网络畅通并稍后重试
REQUEST_RESULT_ERROR|测量服务网络请求超时，请保持网络畅通并稍后重试
LOW_SNR_ERROR|测量信号不满足测量条件，请确保环境光线明亮，脸部面对光源且光照均匀；不要化妆，保持面部无遮挡，测量过程中保持静止，重新开始测量

关于视频质量不佳的具体原因会在`CollectorListener`接口的`onConstraintReceived`会返回客户端对视频帧检测的结果，如果视频帧符合要求则返回`Good`,如果不符合则返回`Error`,并给出错误原因,如:

Error|说明
:-|:-
FACE_FAR|"人脸过远，请保持人脸与摄像头40-60cm距离
FACE_DIRECTION|人脸方向不正，请保持人脸正对摄像头
FACE_MOVEMENT|人脸发生晃动，请保持测量过程人脸静止
FACE_NONE|人脸丢失，请保持人脸在圆框区域
FACE_OFFTARGET|人脸超出范围，请保持人脸在圆框区域
IMAGE_BRIGHT|画面过亮，请适当调低环境光线亮度
IMAGE_DARK|画面过暗，请适当调高环境光线亮度
IMAGE_QUALITY|画面质量不佳，请确保环境光线充足或更换其它设备测量
IMAGE_BACKLIT|画面背景过亮，请适当调低背景光线亮度
LOW_FPS|FPS低


**Q：光线不足**
除了客户端会对视频质量进行检测，云端也会对客户端传递过来的数据进行检测，如果云端返回`SNR`低，说明视频质量不高，则没有必要继续进行测量，客户端则停止测量


### 3.2 错误码
错误|错误码|含义
:-|:-|:-
UNAUTHRIZED|401|未授权

