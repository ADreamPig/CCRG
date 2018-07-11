package com.etop.MVIDetectLine.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.etop.MVIDetectLine.utils.ConstantUtil;
import com.etop.MVInvoice.MVInvoiceAPI;

import java.util.ArrayList;
import java.util.List;

public class EtRecogImgActivity extends Activity {

    private ProgressDialog progressDialog;
    private MVInvoiceAPI eiapi = null;
    private int disMode;
    private ArrayList<String> mResultList;
    private String UserID = ConstantUtil.getUserId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        disMode = intent.getIntExtra("disMode", 0);
        final String fileImgPath = intent.getStringExtra("fileImgPath");

        if (eiapi == null) {
            eiapi = new MVInvoiceAPI();
            String userIdPath = this.getExternalCacheDir().getPath() + "/" + UserID + ".lic";
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int nRet = eiapi.MVKernalInit("", userIdPath, UserID, 61, 0x02, telephonyManager, this);
            if (nRet != 0) {
                Toast.makeText(getApplicationContext(), "激活失败"+nRet, Toast.LENGTH_SHORT).show();
            }
        }
        mResultList = new ArrayList<>();
        if (eiapi != null) {
            eiapi.MVSetRecogType(disMode);
            progressDialog = ProgressDialog.show(EtRecogImgActivity.this, "", "正在识别...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final int nRet = eiapi.MVRecogImage(fileImgPath);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (nRet == 0) {
                                if (disMode == 0) {
                                    for (int i = 0; i < 17; i++) {
                                        mResultList.add(eiapi.MVGetResult(i));
                                    }
                                }else {
                                    for (int i = 0; i < 16; i++) {
                                        mResultList.add(eiapi.MVGetResult(i));
                                    }
                                }
                            } else {
                                mResultList.add("识别失败");
                            }
                            progressDialog.dismiss();
                            Intent intent2 = new Intent();
                            intent2.putStringArrayListExtra("mResultList", mResultList);
                            EtRecogImgActivity.this.setResult(RESULT_OK, intent2);
                            if (eiapi != null) {
                                eiapi.MVKernalUnInit();
                                eiapi = null;
                            }
                            EtRecogImgActivity.this.finish();
                        }
                    });

                }
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        if (progressDialog!=null)progressDialog.dismiss();
        if (eiapi != null) {
            eiapi.MVKernalUnInit();
            eiapi = null;
        }
        super.onDestroy();
    }
}
