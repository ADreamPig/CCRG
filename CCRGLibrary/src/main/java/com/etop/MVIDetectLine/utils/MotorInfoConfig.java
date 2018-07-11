package com.etop.MVIDetectLine.utils;

import java.io.Serializable;

/**
 * 机动车发票页面配置保存图像
 */

public class MotorInfoConfig implements Serializable{
    //是否保存发票图像
    private Boolean isSaveImage = false;
    //保存图像的路径
    private String strSaveImagePath = "/alpha/Motor/";

    public String getStrSaveImagePath() {
        return strSaveImagePath;
    }

    public void setStrSaveImagePath(String strSaveImagePath) {
        this.strSaveImagePath = strSaveImagePath;
    }

    public void setIsSaveImage(Boolean isSaveImage) {
        this.isSaveImage = isSaveImage;
    }

    public Boolean getIsSaveImage() {
        return isSaveImage;
    }
}
