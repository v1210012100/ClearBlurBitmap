package com.example.donglai.clearblurbitmap;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class ClearBlurBitmap extends View {
    //缓存Canvas 用于 绘制固定大小的图片
    private Canvas mCanvas ;
    /**
     * 绘制线条的Paint,即用户手指绘制Path
     */
    private Paint mOutterPaint = new Paint();
    /**
     * 记录用户绘制的Path
     */
    private Path mPath = new Path();
    private Bitmap resBitmap;
    private Bitmap blurBitmap;
    private int mLastX;
    private int mLastY;

    public ClearBlurBitmap(Context context) {
        this(context,null);
    }

    public ClearBlurBitmap(Context context, AttributeSet attrs) {
        super(context, attrs);
        doInitial();
    }
    public  void doInitial(){
        setUpOutPaint();
    }
    /**
     * 设置画笔的一些参数
     */
    private void setUpOutPaint()
    {
        mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
        mOutterPaint.setAntiAlias(true);
        mOutterPaint.setDither(true);
        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutterPaint.setStrokeWidth(40);
    }
    /**
     * 绘制 擦除线条
     */
    private void drawPath()
    {
        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint
                .setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOutterPaint);
    }

    /**
     * 根据获取到的宽高参数 和 resBitmap
     * 先创建一个缩放的scaleBitmap。
     * 直接用原图创建毛玻璃图片，会很耗时，所以我们先创建一个缩放的图片。
     * 再利用scaleBitmap，使用 大名鼎鼎的高斯模糊 FastBlurUtil.doBlur 创建对应的毛玻璃图片。
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas=new Canvas(resBitmap);
        mCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.p2), null, new RectF(0, 0, w, h), null);
        int scaleRatio = 4;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(resBitmap,
                resBitmap.getWidth() / scaleRatio,
                resBitmap.getHeight() / scaleRatio,
                false);
        blurBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas=new Canvas(blurBitmap);
        mCanvas.drawBitmap(FastBlurUtil.doBlur(scaledBitmap,20,false), null, new RectF(0, 0, w, h), null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(resBitmap,0,0,null);
        drawPath();
        canvas.drawBitmap(blurBitmap,0,0,null);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:

                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);

                if (dx > 3 || dy > 3)
                    mPath.lineTo(x, y);

                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:

                break;
        }

        invalidate();
        return true;
    }

}
