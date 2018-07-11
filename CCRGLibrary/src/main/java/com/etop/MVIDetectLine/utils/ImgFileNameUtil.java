package com.etop.MVIDetectLine.utils;

import android.text.format.Time;

import java.util.Random;

/**
 * 保存图片文件命名规则
 */
public class ImgFileNameUtil {
    public static String pictureName(String tag) {
        String str = "ETOP_" + tag + "_";
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        Time time = new Time();
        time.setToNow(); // 取得系统时间。
        sb.append(time.year);
        int month = time.month + 1;
        int date = time.monthDay;
        if (month < 10) {
            sb.append("0"+month);
        }else {
            sb.append(month);
        }
        if (date < 10) {
            sb.append("0"+date+"_"+getFourRandom()+".jpg");
        }else {
            sb.append(date+"_"+getFourRandom()+".jpg");
        }
        return sb.toString();
    }
    /**
     * 产生4位随机数(0000-9999)
     * @return 4位随机数
     */
    public static String getFourRandom(){
        Random random = new Random();
        String fourRandom = random.nextInt(10000) + "";
        int randLength = fourRandom.length();
        if(randLength<4){
            for(int i=1; i<=4-randLength; i++)
                fourRandom = "0" + fourRandom  ;
        }
        return fourRandom;
    }
}
