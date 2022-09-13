package me.jiangwan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class XEdit extends EditText{
    private int lineColor;//横线颜色
    private float lineWidth;//横线宽度

    public XEdit(Context context) {
        super(context);
        init();
    }

    public XEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public void init(){
        setBackgroundColor(0x00000000);
        lineColor = 0xFFC8CAD3;//默认颜色
        lineWidth = 8f;//宽度
        setHint("输入本地图片路径/网络图片URL链接");
        setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        setTextSize(18);
        setHeight(120);
        setHintTextColor(0xFFC8CAD3);
        setMaxLines(1);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
		//创建画笔
        Paint mPaint = new Paint();
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(lineColor);
		//获取参数
        int padL = this.getPaddingLeft();//获取左边留白
        int padR = this.getPaddingRight();//获取右边留白
        canvas.drawLine(padL//startX
                , this.getHeight()
                , this.getWidth() - padR//endX
                , this.getHeight()//endY
                , mPaint);
    }
}

