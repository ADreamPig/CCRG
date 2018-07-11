package com.etop.MVIDetectLine.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.etop.MVIDetectLine.R;
import com.etop.MVIDetectLine.utils.ConstantUtil;
import com.etop.MVIDetectLine.utils.DataUtil;
import com.etop.MVIDetectLine.utils.ImgFileNameUtil;
import com.etop.MVIDetectLine.utils.ToastUtil;
import com.etop.MVInvoice.MVInvoiceAPI;
import com.etop.utils.DotRotateUtils;
import com.etop.view.CropImageView;

import java.util.ArrayList;

public class EditPhotoActivity extends Activity {
    private CropImageView ivImage;
    private Button cancel;
    private Button complete;
    private int[] m_lineX;
    private int[] m_lineY;
    private String strCaptureFilePath;
    private DotRotateUtils dotRotateUtils;
    private boolean isAutoPhoto;
    private Bitmap bitmap;
    private int disMode;
    private MVInvoiceAPI mvApi;
    private ArrayList<String> mResultList;
    private ProgressDialog progressDialog;
    private String UserID = ConstantUtil.getUserId();
    private String saveImagePATH;
    private boolean isSaveImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.etop_motor_activity_photo);

        ivImage = (CropImageView) findViewById(R.id.iv_image);
        cancel = (Button) findViewById(R.id.cancel);
        complete = (Button) findViewById(R.id.complete);

        Intent intent = getIntent();
        strCaptureFilePath = intent.getStringExtra("strCaptureFilePath");
        isAutoPhoto = intent.getBooleanExtra("isAutoPhoto", false);
        isSaveImage = intent.getBooleanExtra("isSaveImage", false);
        disMode = intent.getIntExtra("disMode", 0);
        saveImagePATH = intent.getStringExtra("saveImagePATH");
        m_lineX = intent.getIntArrayExtra("m_lineX");
        m_lineY = intent.getIntArrayExtra("m_lineY");

        if (mvApi == null) {
            mvApi = new MVInvoiceAPI();
            String userIdPath = this.getExternalCacheDir().getPath() + "/" + UserID + ".lic";
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int nRet = mvApi.MVKernalInit("", userIdPath, UserID, 61, 0x02, telephonyManager, this);
            if (nRet != 0) {
                Toast.makeText(EditPhotoActivity.this, "激活失败", Toast.LENGTH_SHORT).show();
                Log.e("激活失败：",nRet+"");
            }
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(strCaptureFilePath, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap = BitmapFactory.decodeFile(strCaptureFilePath, options);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        bitmap = Bitmap.createBitmap(bitmap,0,0,outWidth,outHeight,matrix,true);

        dotRotateUtils = new DotRotateUtils();
        dotRotateUtils.setBitWH(outHeight);

        int [] dotX = new int[4];
        int [] dotY = new int[4];

        int[][] dotXY = dotRotateUtils.DotXY(m_lineX, m_lineY);

        for (int i = 0;i<4;i++) {
            dotX[i]=dotXY[i][0];
            dotY[i]=dotXY[i][1];
        }

        ivImage.setLineXY(dotX, dotY);
        ivImage.setImageToCrop(this.bitmap);
        mOnClick();
    }

    private void mOnClick() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mvApi != null) {
                    mvApi.MVKernalUnInit();
                    mvApi = null;
                }
                startCameraActivity();
            }
        });
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ivImage.canRightCrop()) {
                    ToastUtil.show(EditPhotoActivity.this,"无法裁切图像！");
                    return;
                }
                int[] aarX = {0,0,0,0};
                int[] aarY = {0,0,0,0};

                Point[] cropPoints = ivImage.getCropPoints();
                Point cropPoint1 = cropPoints[0];
                aarX[0] = cropPoint1.x;aarY[0] = cropPoint1.y;
                Point cropPoint2 = cropPoints[1];
                aarX[1] = cropPoint2.x;aarY[1] = cropPoint2.y;
                Point cropPoint3 = cropPoints[2];
                aarX[2] = cropPoint3.x;aarY[2] = cropPoint3.y;
                Point cropPoint4 = cropPoints[3];
                aarX[3] = cropPoint4.x;aarY[3] = cropPoint4.y;

                int[][] m_lineXY = dotRotateUtils.completeXY(aarX, aarY);
                for (int i = 0;i<4;i++) {
                    m_lineX[i]=m_lineXY[i][0];
                    m_lineY[i]=m_lineXY[i][1];
                }
                mResultList = new ArrayList<>();

                final String strCropFilePath = saveImagePATH + ImgFileNameUtil.pictureName("Car");
                progressDialog = ProgressDialog.show(EditPhotoActivity.this, "", "正在识别...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] data = DataUtil.getData();
                        mvApi.MVSetRecogType(disMode);
                        final int nRet = mvApi.MVRecognizePhoto(data, data.length, m_lineX, m_lineY);
                        if (isSaveImage)mvApi.MVSaveRecogImage(strCropFilePath);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (nRet == 0) {
                                    if (disMode == 0) {
                                        for (int i = 0; i < 17; i++) {
                                            mResultList.add(mvApi.MVGetResult(i));
                                        }
                                    }else {
                                        for (int i = 0; i < 16; i++) {
                                            mResultList.add(mvApi.MVGetResult(i));
                                        }
                                    }
                                } else {
                                    mResultList.add("识别失败");
                                }
                                progressDialog.dismiss();
                                motorListener.getMotorResult(mResultList);
                                Log.e("识别结果：",mResultList.toString());
                                if (mvApi != null) {
                                    mvApi.MVKernalUnInit();
                                    mvApi = null;
                                }
                                EditPhotoActivity.this.finish();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startCameraActivity();
    }

    private void startCameraActivity() {
        Intent intent = new Intent(EditPhotoActivity.this, EtCameraActivity.class);
        intent.putExtra("isAutoPhoto", isAutoPhoto);
        intent.putExtra("disMode",disMode);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ToastUtil.cancelToast();
    }

    @Override
    protected void onDestroy() {
        if (bitmap!=null) {
            bitmap.recycle();
            bitmap=null;
        }
        if (progressDialog!=null)
            progressDialog.dismiss();
        if (mvApi != null) {
            mvApi.MVKernalUnInit();
            mvApi = null;
        }
        super.onDestroy();
    }

    private static MotorVehicleResultListener motorListener;
    public static void setMotorResult(MotorVehicleResultListener motorVehicleResultListener) {
        motorListener = motorVehicleResultListener;
    }
    public interface MotorVehicleResultListener {
        void getMotorResult(ArrayList<String> list);
    }
}
