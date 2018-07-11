package com.etop.motor;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.etop.MVIDetectLine.activity.EditPhotoActivity;
import com.etop.MVIDetectLine.activity.EtRecogImgActivity;
import com.etop.MVIDetectLine.activity.EtCameraActivity;
import com.etop.MVIDetectLine.utils.ConstantUtil;
import com.etop.MVIDetectLine.utils.MotorInfoConfig;
import com.etop.MVIDetectLine.utils.StreamUtil;

import java.io.IOException;
import java.util.ArrayList;

import static com.etop.MVIDetectLine.activity.EditPhotoActivity.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int IMPORT_RECOG = 101;
    private static final int RESULT_CODE = 102;
    private int disMode = -1;
    private TextView textView;

    private String[] strlocomotiveCar = {"发票代码 : ", "发票号码 : ", "开票日期 : ", "机器编号 : ", "购方名称 : ",
            "身份号码 : ", "纳税人识别号 : ", "车辆类型 : ", "厂牌型号 : ", "产地 : ", "合格证号 : ", "进口证明书号 : ", "商检单号 : ",
            "发动机号 : ", "车辆识别代号 : ", "价税合计 : ", "不含税价 : "};
    private String[] strUsedCar = {"发票代码 : ", "发票号码 : ", "开票日期 : ", "购方名称 : ", "购方身份证号码 : ",
            "购方地址 : ", "卖方名称 : ", "卖方身份证号码 : ", "卖方地址 : ",
            "车牌照号 : ", "登记证号 : ", "车辆类型 : ", "车架号 : ", "厂牌型号 : ", "车辆管理所名称 : ", "车价合计 : "};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnMotorVehicle = (Button) findViewById(R.id.btn_motor_scan_vehicle);
        Button btnSecondHandCar = (Button) findViewById(R.id.btn_two_car_scan);
        Button btnMotorImportVehicle = (Button) findViewById(R.id.btn_motor_import_vehicle);
        Button btnTwoCarImport = (Button) findViewById(R.id.btn_two_car_import);
        textView = (TextView) findViewById(R.id.textview);

        //1.设置授权名称
        ConstantUtil.setUserId("7332DBAFD2FD18301EF6");
        //2.将授权文件写入到指定目录下，否则无法激活使用
        try {
            StreamUtil.copyDataBase(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * 返回拍照识别结果
         */
        EditPhotoActivity.setMotorResult(new MotorVehicleResultListener() {
            @Override
            public void getMotorResult(ArrayList<String> list) {
                String carInfo = "";
                //如果返回集合不等于1，代表识别成功，否则返回识别失败的结果
                if (list.size()!=1) {
                    for (int i = 0;i < list.size();i++) {
                        String element = list.get(i);
                        if (element.equals(""))element = "识别失败";
                        if (disMode==0) {
                            //机动车发票识别信息
                            carInfo += strlocomotiveCar[i] + element + "\r\n";
                        }else {
                            //二手车发票识别信息
                            carInfo += strUsedCar[i] + element + "\r\n";
                        }
                        textView.setText(carInfo);
                    }
                }else {
                    textView.setText(list.get(0));
                }
            }
        });

        btnMotorVehicle.setOnClickListener(this);
        btnSecondHandCar.setOnClickListener(this);
        btnMotorImportVehicle.setOnClickListener(this);
        btnTwoCarImport.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //进入机动车发票导入识别
            case R.id.btn_motor_import_vehicle:
                disMode = 0;
                importImgDiscern();
                break;
            //进入二手车发票导入识别
            case R.id.btn_two_car_import:
                disMode = 1;
                importImgDiscern();
                break;
            //进入机动车发票扫描识别
            case R.id.btn_motor_scan_vehicle:
                disMode = 0;
                startCameraRecog();
                break;
            //进入二手车发票扫描识别
            case R.id.btn_two_car_scan:
                disMode = 1;
                startCameraRecog();
                break;
        }
    }

    //3.跳转拍照页面，并配置保存图像信息
    private void startCameraRecog() {
        Intent intent = new Intent(MainActivity.this, EtCameraActivity.class);
        MotorInfoConfig config = new MotorInfoConfig();
        intent.putExtra("disMode", disMode);
        //是否保存图像
        //config.setIsSaveImage(true);
        //设置图像保存路径(格式为"/alpha/Motor/")
        //config.setStrSaveImagePath("/etop666/");
        intent.putExtra(ConstantUtil.INTENT_MOTOR_CONFIG, config);
        startActivity(intent);
    }

    //3.进入导入图像识别
    private void importImgDiscern() {
        Intent selectIntent = new Intent(Intent.ACTION_PICK);
        selectIntent.setType("image/*");
        if (selectIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(selectIntent, IMPORT_RECOG);
        }
    }

    private String filePath = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == IMPORT_RECOG) {
                if (data == null) {
                    return;
                }
                Uri imageFileUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(imageFileUri, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    filePath = cursor.getString(columnIndex);
                    cursor.close();
                } else {
                    filePath = imageFileUri.getPath();
                }

                Intent intent = new Intent(MainActivity.this, EtRecogImgActivity.class);
                intent.putExtra("disMode", disMode);
                intent.putExtra("fileImgPath", filePath);
                startActivityForResult(intent,RESULT_CODE);
                //4.返回导入识别结果
            }else if (requestCode == RESULT_CODE) {
                if (data!=null) {
                    ArrayList<String> mResultList = data.getStringArrayListExtra("mResultList");
                    String carInfo = "";
                    //如果返回集合不等于1，代表识别成功，否则返回识别失败的结果
                    if (mResultList.size()!=1) {
                        for (int i = 0;i < mResultList.size();i++) {
                            String element = mResultList.get(i);
                            if (element.equals(""))element = "识别失败";
                            if (disMode==0) {
                                //机动车发票识别信息
                                carInfo += strlocomotiveCar[i] + element + "\r\n";
                            }else {
                                //二手车发票识别信息
                                carInfo += strUsedCar[i] + element + "\r\n";
                            }
                            textView.setText(carInfo);
                        }
                    }else {
                        textView.setText(mResultList.get(0));
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
