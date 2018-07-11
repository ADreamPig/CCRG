package com.etop.MVIDetectLine.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.etop.MVIDetectLine.R;
import com.etop.MVIDetectLine.utils.ConstantUtil;
import com.etop.MVIDetectLine.utils.DataUtil;
import com.etop.MVIDetectLine.utils.ActualHeightUtil;
import com.etop.MVIDetectLine.utils.MotorInfoConfig;
import com.etop.MVIDetectLine.utils.StreamUtil;
import com.etop.MVIDetectLine.utils.ToastUtil;
import com.etop.MVIDetectLine.view.LineViewfinderView;
import com.etop.MVInvoice.MVInvoiceAPI;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class EtCameraActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback,OnClickListener {
    private boolean bInitKernal = false;
    private MVInvoiceAPI eiapi = null;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private RelativeLayout mainRl;
    private SurfaceHolder surfaceHolder;
    private ImageButton ibBack;
    private ImageButton ibFlash;
    private ImageButton ibChange;
    private ImageButton ibTakePic;
    private TextView mTvRemind;
    private TextView mTvMode;
    private int screenWidth;
    private int screenHeight;
    private int preWidth = 0;
    private int preHeight = 0;
    private int photoWidth = 0;
    private int photoHeight = 0;
    private int[] m_lineX = {0, 0, 0, 0};
    private int[] m_lineY = {0, 0, 0, 0};
    private LineViewfinderView myView;
    private SoundPool soundPool;

    HashMap musicId = new HashMap();
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:

                    llProgressBar.setVisibility(View.GONE);
                    if (strCaptureFilePath!=null&&m_lineX[0]!=m_lineX[2]&&m_lineX[1]!=m_lineX[3]&&m_lineY[1]!=m_lineX[3]&&m_lineY[0]!=m_lineX[2]) {
                        Intent intent = new Intent(EtCameraActivity.this,EditPhotoActivity.class);
                        intent.putExtra("strCaptureFilePath",strCaptureFilePath);
                        intent.putExtra("isAutoPhoto", isAutoPhoto);
                        intent.putExtra("disMode",disMode);
                        intent.putExtra("isSaveImage",isSaveImage);
                        intent.putExtra("saveImagePATH",saveImagePATH);
                        intent.putExtra("m_lineX", m_lineX);
                        intent.putExtra("m_lineY", m_lineY);
                        startActivity(intent);
                        finish();
                    }else {//说明没有剪到线
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(strCaptureFilePath, options);
                        int bitWidth = options.outWidth;
                        int bitHeight = options.outHeight;
                        m_lineX[0]=20;
                        m_lineX[1]= bitWidth-20;
                        m_lineX[2]= bitWidth-20;
                        m_lineX[3]=20;
                        m_lineY[0]=20;
                        m_lineY[1]=20;
                        m_lineY[2]= bitHeight-20;
                        m_lineY[3]= bitHeight-20;
                        Intent intent = new Intent(EtCameraActivity.this,EditPhotoActivity.class);
                        intent.putExtra("strCaptureFilePath", strCaptureFilePath);
                        intent.putExtra("isAutoPhoto", isAutoPhoto);
                        intent.putExtra("disMode",disMode);
                        intent.putExtra("isSaveImage",isSaveImage);
                        intent.putExtra("saveImagePATH",saveImagePATH);
                        intent.putExtra("m_lineX", m_lineX);
                        intent.putExtra("m_lineY", m_lineY);
                        startActivity(intent);
                        finish();
                    }
                    break;
            }
        }
    };
    private String strCaptureFilePath;
    private LinearLayout llProgressBar;
    private boolean isAutoPhoto;
    private int disMode;
    private MotorInfoConfig cardInfoConfig;
    private String saveImagePATH;
    private String UserID = ConstantUtil.getUserId();
    private Boolean isSaveImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.etop_motor_activity_camera);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮

        screenWidth = getWindowManager().getDefaultDisplay().getWidth();//1920
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();//1080

        Configuration cf = this.getResources().getConfiguration(); //获取设置的配置信息
        int noriention = cf.orientation;
        File file = new File(ConstantUtil.PATH);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        if (noriention == cf.ORIENTATION_LANDSCAPE) {
            initKernal();//初始化核心
        }

        cardInfoConfig = (MotorInfoConfig) getIntent().getExtras().get(ConstantUtil.INTENT_MOTOR_CONFIG);
        if (cardInfoConfig == null) {
            cardInfoConfig = new MotorInfoConfig();
        }
        isSaveImage = cardInfoConfig.getIsSaveImage();
        saveImagePATH = Environment.getExternalStorageDirectory() + cardInfoConfig.getStrSaveImagePath();
        if (saveImagePATH !=null) {
            File file2 = new File(saveImagePATH);
            if (!file2.exists() && !file2.isDirectory()) {
                file2.mkdirs();
            }
        }else {
            Toast.makeText(this,"路径不正确",Toast.LENGTH_SHORT).show();
        }

        findView();
        Intent intent = getIntent();
        isAutoPhoto = intent.getBooleanExtra("isAutoPhoto", false);
        disMode = intent.getIntExtra("disMode", 0);
        if (isAutoPhoto) {
            isAutoTakePhoto(true);
        }else {
            isAutoTakePhoto(false);
        }
    }

    private void findView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceViwe);
        mainRl = (RelativeLayout) findViewById(R.id.re_c);
        ibBack = (ImageButton) findViewById(R.id.back_camera_etop);
        ibFlash = (ImageButton) findViewById(R.id.flash_camera_etop);
        ibChange = (ImageButton) findViewById(R.id.etop_change);
        mTvRemind = (TextView) findViewById(R.id.remind);
        ibTakePic = (ImageButton) findViewById(R.id.take_pic_etop);
        mTvMode = (TextView) findViewById(R.id.text);
        llProgressBar = (LinearLayout) findViewById(R.id.ll_jdt);

        soundPool = new SoundPool(12, AudioManager.STREAM_SYSTEM, 5);
        //通过load方法加载指定音频流，并将返回的音频ID放入musicId中
        musicId.put(2, soundPool.load(this, R.raw.etop_motor_closer, 1));
        musicId.put(4, soundPool.load(this, R.raw.etop_motor_photo, 1));

        surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        ibBack.setOnClickListener(this);
        ibFlash.setOnClickListener(this);
        ibChange.setOnClickListener(this);
        ibTakePic.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.back_camera_etop) {
            finish();

        } else if (i == R.id.flash_camera_etop) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                ToastUtil.show(this, "当前设备不支持闪光灯");
            } else {
                if (mCamera != null) {
                    Parameters parameters = mCamera.getParameters();
                    String flashMode = parameters.getFlashMode();
                    if (flashMode.equals(Parameters.FLASH_MODE_TORCH)) {
                        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                        parameters.setExposureCompensation(0);
                        ibFlash.setBackgroundResource(R.drawable.etop_motor_flash_off);
                    } else {
                        parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);// 闪光灯常亮
                        parameters.setExposureCompensation(-1);
                        ibFlash.setBackgroundResource(R.drawable.etop_motor_flash_on);
                    }
                    try {
                        mCamera.setParameters(parameters);
                    } catch (Exception e) {
                        ToastUtil.show(this, "当前设备不支持闪光灯");
                    }
                    mCamera.startPreview();
                }
            }

        } else if (i == R.id.etop_change) {
            if (isAutoPhoto) {
                isAutoTakePhoto(false);
                isAutoPhoto = false;
            } else {
                isAutoTakePhoto(true);
                isAutoPhoto = true;
            }

        } else if (i == R.id.take_pic_etop) {
            if (mCamera != null) {
                try {
                    isTakePicture();//去手动拍照
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void isTakePicture() {
        Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    try {
                        mTvRemind.setVisibility(View.GONE);
                        llProgressBar.setVisibility(View.VISIBLE);
                        mCamera.takePicture(null, null, picturecallback);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });
    }
    private void isAutoTakePhoto(Boolean isAuto) {
        if (!isAuto) {
            mTvMode.setText("当前为手动拍照识别模式");
            mTvRemind.setVisibility(View.GONE);
            mTvMode.setTextColor(Color.WHITE);
            mTvMode.setTextSize(TypedValue.COMPLEX_UNIT_PX, EtCameraActivity.this.screenHeight / 18);
            ibTakePic.setVisibility(View.VISIBLE);
            mTvRemind.setVisibility(View.GONE);
        } else if (isAuto) {
            mTvMode.setText("当前为自动拍照识别模式");
            mTvRemind.setVisibility(View.VISIBLE);
            mTvRemind.setText("正在查找证件...");
            mTvMode.setTextColor(Color.WHITE);
            mTvMode.setTextSize(TypedValue.COMPLEX_UNIT_PX, EtCameraActivity.this.screenHeight / 18);
            ibTakePic.setVisibility(View.INVISIBLE);
            mTvRemind.setVisibility(View.VISIBLE);
        }
    }

    private PictureCallback picturecallback = new PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            strCaptureFilePath = ConstantUtil.PATH + "/mvi_temp";
            if (mCamera == null) {
                return;
            }
            for (int i = 0; i < 4; i++) {
                m_lineX[i] = m_lineX[i] * photoWidth / preWidth;
            }
            for (int i = 0; i < 4; i++) {
                m_lineY[i] = m_lineY[i] * photoHeight / preHeight;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (eiapi != null) {
                        eiapi.MVKernalUnInit();
                        eiapi = null;
                    }
                    DataUtil.setData(data);
                    try {
                        //保存原图
                        StreamUtil.saveBitmapFile(data, strCaptureFilePath);
                    } catch (Exception e) {
                        System.out.println("图像写入失败！");
                    }

                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }).start();
        }
    };


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.show(EtCameraActivity.this, "无法启用相机");
                return;
            }
        }
        initCamera(holder);
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 初始化核心
     */
    private void initKernal() {
        if (eiapi == null) {
            eiapi = new MVInvoiceAPI();
            String userIdPath = this.getExternalCacheDir().getPath() + "/" + UserID + ".lic";
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int nRet = eiapi.MVKernalInit("", userIdPath, UserID, 61, 0x02, telephonyManager, this);
            if (nRet != 0) {
                Toast.makeText(getApplicationContext(), "激活失败", Toast.LENGTH_SHORT).show();
                System.out.print("nRet=" + nRet);
                bInitKernal = false;
            }  else {
                bInitKernal = true;
                String endTime = eiapi.MVGetEndTime();//获取一个授权结束的日期（2018-3-31）
                String[] time = endTime.split("-");
                int year1 = Integer.parseInt(time[0]);
                int month1 = Integer.parseInt(time[1]);
                int day1 = Integer.parseInt(time[2]);
                //Toast.makeText(getApplicationContext(), endTime, Toast.LENGTH_SHORT).show();

                Time timeSystem = new Time();
                timeSystem.setToNow(); // 取得系统时间。
                int year = timeSystem.year;//年
                int month = timeSystem.month + 1;//月
                int day = timeSystem.monthDay;//日

                if (year1 == year && month1 == month) {//说明年月相同
                    int endDay = day1 - day+1;
                    if (endDay <= 7 && endDay >= 0) {
                        Toast.makeText(EtCameraActivity.this, "授权将于" + endDay + "天后到期", Toast.LENGTH_SHORT).show();
                    }
                    //说明年份相同月份不同，且授权截止日期在下月7号之前
                }else if (year1 == year && month1 - month == 1 && day1 < 7) {
                    int days = getDays(year, month);//返回当月天数
                    int endDay = days + day1 - day+1;
                    if (endDay <= 7 && endDay >= 0) {
                        Toast.makeText(EtCameraActivity.this, "授权将于" + endDay + "天后到期", Toast.LENGTH_SHORT).show();
                    }
                    //跨年，授权截止日期在1月份，并且在下月7号之前
                }else if (year1-year==1&&month1==1&&day1 < 7) {
                    int endDay = 32 + day1 - day;
                    if (endDay <= 7 && endDay >= 0) {
                        Toast.makeText(EtCameraActivity.this, "授权将于" + endDay + "天后到期", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }

    @TargetApi(14)
    private void initCamera(SurfaceHolder holder) {
        Parameters parameters = mCamera.getParameters();

        List<Size> previewList = parameters.getSupportedPreviewSizes();
        List<Size> photoList = parameters.getSupportedPictureSizes();
        Size previewSize = getAdapterPreviewSize(previewList, ActualHeightUtil.getWidthDpi(this), screenHeight);
        if (previewSize != null) {
            preWidth = previewSize.width;
            preHeight = previewSize.height;
        }else {
            Toast.makeText(this,"相机无法兼容！",Toast.LENGTH_LONG).show();
            return;
        }
        Size pictureSize = getAdapterPictureSize(photoList, preWidth, preHeight);

        if (getSystemModel().equals("MI 5X")) {
            photoWidth = 3840;
            photoHeight = 2160;
        } else {
            photoWidth = pictureSize.width;
            photoHeight = pictureSize.height;
        }

        if (myView == null) {
            myView = new LineViewfinderView(this, screenWidth, screenHeight);
            mainRl.addView(myView);
        }
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setJpegQuality(100);
        parameters.setPreviewSize(preWidth, preHeight);
        parameters.setPictureSize(photoWidth, photoHeight);

        if (parameters.getSupportedFocusModes().contains(
                parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.setPreviewCallback(this);
        mCamera.setParameters(parameters);
        if (parameters.isZoomSupported()) {
            parameters.setZoom(2);
        }
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Parameters parameters = camera.getParameters();
        AudioManager mgr = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        ((Activity) this).setVolumeControlStream(AudioManager.STREAM_SYSTEM);
        float streamVolumeCurrent = mgr
                .getStreamVolume(AudioManager.STREAM_SYSTEM);
        float streamVolumeMax = mgr
                .getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        float volume = streamVolumeCurrent / streamVolumeMax;


        int r = eiapi.MVDetectLine(data,
                parameters.getPreviewSize().width,
                parameters.getPreviewSize().height, m_lineX, m_lineY);
        if (myView != null) {
            myView.setDisMode(disMode);
            myView.SetLine(m_lineX, m_lineY,
                    parameters.getPreviewSize().width,
                    parameters.getPreviewSize().height);
        }

        if (isAutoPhoto) {
            if (r == 0) {
                mTvRemind.setText("准备拍照，请不要移动...");
                soundPool.play((Integer) (musicId.get(4)), volume, volume, 0,
                        0, 1);
                isTakePicture();
            } else if (r == 31) {
                mTvRemind.setText("请靠近点...");
                        soundPool.play((Integer) (musicId.get(2)), volume,
                                volume, 0, 0, 1);
            } else if (r == 33) {
                mTvRemind.setText("正在查找机动车发票...");
            } else if(r==32){
                mTvRemind.setText("正在检线...");
            }
        }

    }
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }
    //返回当月天数
    int getDays(int year, int month) {
        int days;
        int FebDay = 28;
        if (isLeap(year))
            FebDay = 29;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                days = 31;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                days = 30;
                break;
            case 2:
                days = FebDay;
                break;
            default:
                days = 0;
                break;
        }
        return days;
    }

    private boolean isLeap(int year) {
        if (((year % 100 == 0) && year % 400 == 0) || ((year % 100 != 0) && year % 4 == 0))
            return true;
        else
            return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.etop_motor_menu, menu);
        return true;
    }
    @Override
    protected void onStop() {
        super.onStop();
        ToastUtil.cancelToast();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        if (eiapi != null) {
            eiapi.MVKernalUnInit();
            eiapi = null;
        }
        super.onDestroy();
    }

    private Size getAdapterPictureSize(List<Size> list, int preWidth, int preHeight) {
        double ASPECT_TOLERANCE = 0.002;//允许的比例误差
        double targetRatio = (double) preHeight / preWidth;
        if (targetRatio > 1) {
            targetRatio = (double) preWidth / preHeight;
        }
        Size optimalSize = null;
        for (Size size : list) {
            double ratio = (double) size.height / size.width;
            if (ratio > 1) {
                ratio = (double) size.width / size.height;
            }
            if (size.height < 1000) continue;
            if (Math.abs(ratio - targetRatio) < ASPECT_TOLERANCE) {
                if (optimalSize != null) {
                    if (optimalSize.width < size.width || optimalSize.height < size.height) {
                        optimalSize = size;
                    }
                } else {
                    optimalSize = size;
                }
            }
        }
        return optimalSize;
    }

    private Size getAdapterPreviewSize(List<Size> list, int screenWidth, int screenHeight) {
        double ASPECT_TOLERANCE = 0.026;//允许的比例误差
        double targetRatio = (double) screenHeight / screenWidth;
        if (targetRatio > 1) {
            targetRatio = (double) screenWidth / screenHeight;
        }
        if (targetRatio < 0.5) targetRatio = 0.5;
        Size optimalSize = null;
        for (Size size : list) {
            double ratio = (double) size.height / size.width;
            if (ratio > 1) {
                ratio = (double) size.width / size.height;
            }
            if (size.height < 700) continue;
            if (size.height > 1200) continue;
            if (ratio == targetRatio) {
                if (optimalSize != null) {
                    if (optimalSize.width > size.width || optimalSize.height > size.height) {
                        optimalSize = size;
                    }
                } else {
                    optimalSize = size;
                }
            }
        }

        if (optimalSize==null) {
            for (Size size : list) {
                double ratio = (double) size.height / size.width;
                if (ratio > 1) {
                    ratio = (double) size.width / size.height;
                }
                if (size.height < 600) continue;
                if (Math.abs(ratio - targetRatio) < ASPECT_TOLERANCE) {
                    if (optimalSize != null) {
                        if (optimalSize.width > size.width || optimalSize.height > size.height) {
                            optimalSize = size;
                        }
                    } else {
                        optimalSize = size;
                    }
                }
            }
        }
        return optimalSize;
    }
}
