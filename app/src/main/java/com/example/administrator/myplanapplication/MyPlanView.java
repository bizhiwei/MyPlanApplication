package com.example.administrator.myplanapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/23 0023.
 */
public class MyPlanView extends SurfaceView implements SurfaceHolder.Callback {
    public MyPlanView(Context context) {
        super(context);
        init(context);
    }


    public MyPlanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyPlanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Context context;
    /**
     * 物体的画笔
     */
    private Paint paint;
    /**
     * 物体颜色
     */
    private int colorPaint = 0xaaff0000;
    private int windowW;
    private int windowH;
    private int minWH;
    /**
     * 半径
     */
    private int r;
    private SurfaceHolder sh;
    private int dotR;
    private Paint paintDot;
    private int dotColor = 0xaa00ff00;
    AlertDialog dialog;
    static Handler handler = new Handler();

    private void init(Context context) {

        sh = this.getHolder();
        sh.addCallback(this);
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(colorPaint);
        windowW = context.getResources().getDisplayMetrics().widthPixels;
        windowH = context.getResources().getDisplayMetrics().heightPixels;
        minWH = Math.min(windowH, windowW);
        r = 40;
        dotR = 20;

        paintDot = new Paint();
        paintDot.setAntiAlias(true);
        paintDot.setColor(dotColor);
        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new Dot(windowW / 2, windowH / 3, dotR));
        }
    }


    private void actionDown(MotionEvent event) {
        oldX = event.getX();
        oldY = event.getY();
    }

    private float oldX;
    private float oldY;

    /**
     * 手指的移动动作
     */

    private void actionMove(MotionEvent event) {
        float currentX = event.getX();//当前坐标值
        float X = currentX - oldX;//当前坐标与上次测量坐标差值
        oldX = currentX;
        float currentY = event.getY();//当前坐标值
        float Y = currentY - oldY;//当前坐标与上次测量坐标差值
        oldY = currentY;//设置当前坐标为上次坐标
        x += X;
        y += Y;
    }

    private List<Dot> list;
    private Canvas canvas;
    private int colorBg = 0xffffffff;
    /**
     * 坐标点
     */
    private float x, y;
    private boolean flag = true;
    private long startTime;
    private float tottleTime;

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).setMessage("开始").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread() {
                    @Override
                    public void run() {
                        startTime = System.currentTimeMillis();
                        while (flag) {
                            dotDraw(x, y, r, list, holder);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }.start();
            }
        }).create();
        alertDialog.show();
        x = windowW / 2;
        y = windowH / 3 * 2;
        dotDraw(x, y, r, list, holder);

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private void dotDraw(float x, float y, int r, List<Dot> listDot, SurfaceHolder holder) {
        canvas = holder.lockCanvas();
        canvas.drawColor(colorBg);
        canvas.drawCircle(x, y, r, paint);
        for (int i = 0; i < listDot.size(); i++) {
            if (getIsFinished(x, y, listDot, i)) {
                flag = false;
                tottleTime = (System.currentTimeMillis() - startTime) / 1000f;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog = new AlertDialog.Builder(context)
                                .setMessage("失败" + "\n" + tottleTime + " 秒")
                                .setPositiveButton("确定", null).create();
                        dialog.show();
                    }
                });
            }

            canvas.drawCircle(listDot.get(i).x
                    , listDot.get(i).y
                    , listDot.get(i).r
                    , paintDot);
            listDot.get(i).move();
        }
        holder.unlockCanvasAndPost(canvas);
    }

    /**
     * 得到是否已经结束游戏(即游戏的判断规则)
     *
     * @param x
     * @param y       圆点坐标
     * @param listDot
     * @param i
     * @return
     */
    private boolean getIsFinished(float x, float y, List<Dot> listDot, int i) {
        return Math.abs((listDot.get(i).x - x) * (listDot.get(i).x - x) + (listDot.get(i).y - y) * (listDot.get(i).y - y))
                <= (40 * 40 + 20 * 20);
    }

    class Dot {
        private int r;//半径
        private float x;//坐标x
        private float y;//坐标y
        private float speed = 10;//每次走的距离()
        private float angle;//角度
        private int directionX = 1;//x轴方向，+1为正向，-1为反向
        private int directionY = 1;
        private double speedX;//x轴速度，由speed计算得到
        private double speedY;

        public Dot(float x, float y, int r) {
            this.x = x;
            this.y = y;
            this.r = r;
            angle = (float) (Math.random() * 360);
            speedX = speed * Math.sin(angle);//每次走的距离的sin值是每次x轴走的值
            speedY = speed * Math.cos(angle);
        }

        public void move() {
            if (x >= windowW || x <= 0) {
                directionX = -directionX;//x轴碰的边就变向
            }
            if (y >= windowH || y <= 0) {
                directionY = -directionY;
            }
            x += speedX * directionX;
            y += speedY * directionY;
        }
    }
}
