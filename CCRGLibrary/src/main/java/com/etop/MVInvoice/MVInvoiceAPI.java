package com.etop.MVInvoice;

import android.content.Context;
import android.telephony.TelephonyManager;

public class MVInvoiceAPI {
	static {
		System.loadLibrary("AndroidMVInvoice");
	}
	public native int MVKernalInit(String szSysPath,String FilePath,String CommpanyName,int nProductType,int nAultType,TelephonyManager telephonyManager,Context context);
	public native void MVKernalUnInit();
	public native int MVRecognizePhoto(byte[] ImageStreamNV21, int nLen, int []LineX, int[]LineY);
	public native int MVRecognizePhotoEx(byte[] ImageStreamNV21, int nLen);
	public native String MVGetResult(int nIndex);
	public native int EISaveRecogImg(String imgPath);
	public native int MVDetectLine(byte[] streamnv21, int cols, int raws, int []LineX,int[]LineY);
	public native int MVSaveRecogImage(String imgPath);
	public native int MVRecogImage(String imgPath);//导入识别
	public native String MVGetEndTime();//获取授权截止日期
	public native int MVSetRecogType(int nType);//设置识别类型，nType为0表示识别机动车发票，1表示识别二手车发票
}
