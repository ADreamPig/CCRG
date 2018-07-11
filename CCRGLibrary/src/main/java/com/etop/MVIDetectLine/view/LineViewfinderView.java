package com.etop.MVIDetectLine.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.etop.MVIDetectLine.R;


public class LineViewfinderView extends View {
    private static final long ANIMATION_DELAY = 5L;
    private float num;
    private int TEXT_SIZE;
    private Paint paintLine;
    private Paint mTextPaint;
    private int frameColor;
    private Rect[] line = new Rect[4];
    int w, h;
    private String mText;
    private float lineWidth;
    private int disMode;

    public LineViewfinderView(Context context, int w, int h) {
        super(context);
        this.w = w;
        this.h = h;

        Resources resources = getResources();
        frameColor = resources.getColor(R.color.green_color);
        TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.text_size);
        lineWidth = resources.getDimension(R.dimen.line_width);
        num = resources.getDimension(R.dimen.num);
        for (int i = 0; i < 4; i++) {
            line[i] = new Rect(0, 0, 0, 0);
        }

        paintLine = new Paint();
        paintLine.setColor(frameColor);
        paintLine.setStrokeWidth(lineWidth);//设置描边的宽度
        paintLine.setAntiAlias(true);


        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(TEXT_SIZE);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void SetLine(int[] ArrayX, int[] ArrayY, int nW, int nH) {
        this.line[0].left = ArrayX[0];
        this.line[0].right = ArrayY[0];
        this.line[0].top = ArrayX[3];
        this.line[0].bottom = ArrayY[3];

        this.line[1].left = ArrayX[0];
        this.line[1].right = ArrayY[0];
        this.line[1].top = ArrayX[1];
        this.line[1].bottom = ArrayY[1];

        this.line[2].left = ArrayX[1];
        this.line[2].right = ArrayY[1];
        this.line[2].top = ArrayX[2];
        this.line[2].bottom = ArrayY[2];

        this.line[3].left = ArrayX[3];
        this.line[3].right = ArrayY[3];
        this.line[3].top = ArrayX[2];
        this.line[3].bottom = ArrayY[2];
        for (int i = 0; i < 4; i++) {
            this.line[i].left = this.line[i].left * this.w / nW;
            this.line[i].right = this.line[i].right * this.h / nH;
            this.line[i].top = this.line[i].top * this.w / nW;
            this.line[i].bottom = this.line[i].bottom * this.h / nH;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int t = 5;
        int l = 5;
        int r = width-5;
        int b = height-5;

        //绘制四个角绿线
        canvas.drawLine(l - 4, t, l + num, t, paintLine);
        canvas.drawLine(l, t, l, t + num, paintLine);

        canvas.drawLine(r, t, r - num, t, paintLine);
        canvas.drawLine(r, t - 4, r, t + num, paintLine);

        canvas.drawLine(l - 4, b, l + num, b, paintLine);
        canvas.drawLine(l, b, l, b - num, paintLine);

        canvas.drawLine(r, b, r - num, b, paintLine);
        canvas.drawLine(r, b + 4, r, b - num, paintLine);

        //绘制裁剪框
        for (int i = 0; i < 4; i++) {
            if ((line[i].left != 0 && line[i].top != 0 && line[i].right != 0 && line[i].bottom != 0)
                    || (line[i].left != -1 && line[i].top != -1 && line[i].right != -1 && line[i].bottom != -1)) {
                canvas.drawLine(line[i].left, line[i].right, line[i].top, line[i].bottom, paintLine);
            }
        }
        if (disMode==0) {
            mText = "请将机动车发票放置框内";
        }else {
            mText = "请将二手车发票放置框内";
        }
        canvas.drawText(mText, w / 2,num*2, mTextPaint);
        postInvalidateDelayed(ANIMATION_DELAY);
    }

    public void setDisMode(int disMode) {
        this.disMode = disMode;
    }
}
