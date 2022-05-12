package com.zhumj.batteryview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * @author Created by zhumj
 * @date 2022/5/11 9:06
 * @description : 电池电量View
 */
public class BatteryView extends View {

    private int orientation;//方向，vertical：0，horizontal：1

    private int minWidth;//最小宽度
    private int minHeight;//最大宽度

    private Paint borderPaint;//电池外框的画笔
    private float borderWidth;//电池外框宽度
    private float borderRadius;//电池外框半径
    private @ColorInt int borderColor;//电池外框颜色

    private Paint headPaint; //电池头画笔
    private float headWidth;//电池头宽度
    private float headHeight;//电池头高度
    private float headPadding;//电池头距电池外框的距离
    private @ColorInt int headColor;// 电池头颜色

    private Paint insidePaint; // 电池内部画笔
    private float insidePadding; // 电池内框距外框的距离
    private float insideRadius; // 电池内框四角半径

    private @ColorInt int lowPowerColor; // 低电量颜色
    private @ColorInt int highPowerColor; // 高电量颜色
    private @ColorInt int chargingColor; // 充电中颜色

    //是否处于充电状态
    private boolean isCharging;
    //电量最大值
    private int maxPower;
    //电量
    private int power;

    private final Handler batteryHandler = new Handler();
    /**
     * 充电动画
     */
    private final Runnable chargingTask = () -> {
        if (power >= maxPower) {
            power = maxPower/5;
        } else {
            if (power == maxPower/5 || power == maxPower*2/5 || power == maxPower*3/5 || power == maxPower*4/5) {
                power += maxPower/5;
            } else {
                if (power < maxPower/5) {
                    power = maxPower/5;
                }
                else if (power < maxPower*2/5) {
                    power = maxPower*2/5;
                }
                else if (power < maxPower*3/5) {
                    power = maxPower*3/5;
                }
                else if (power < maxPower*4/5) {
                    power = maxPower*4/5;
                } else {
                    power = maxPower;
                }
            }
        }

        invalidate();
    };

    /**
     * 电量、充电状态监听
     */
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                setMaxPower(intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
                int currentPower = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
                boolean currentCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
                if (isCharging() != currentCharging) {
                    setCharging(currentCharging);
                } else {
                    setPower(currentPower);
                }
            }
        }
    };

    public BatteryView(Context context) { this(context, null); }

    public BatteryView(Context context, @Nullable AttributeSet attrs) { this(context, attrs, 0); }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(attrs);
        initPaint();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);//电量变化
        context.registerReceiver(batteryReceiver, filter);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BatteryView);
        orientation = a.getInt(R.styleable.BatteryView_orientation, 0);
        int defMinWidth = 24;
        int defHeight = 45;
        int defHeadWidth = 12;
        int defHeadHeight = 4;
        if (orientation == 1) {
            defMinWidth = 45;
            defHeight = 24;
            defHeadWidth = 4;
            defHeadHeight = 12;
        }

        minWidth = (int) a.getDimension(R.styleable.BatteryView_minWidth, defMinWidth);
        minHeight = (int) a.getDimension(R.styleable.BatteryView_minHeight, defHeight);
        maxPower = a.getInt(R.styleable.BatteryView_maxPower, 100);
        power = maxPower;

        borderWidth = a.getDimension(R.styleable.BatteryView_border_width, 3);
        borderRadius = a.getDimension(R.styleable.BatteryView_border_radius, 4);
        borderColor = a.getColor(R.styleable.BatteryView_border_color, Color.BLACK);

        headWidth = a.getDimension(R.styleable.BatteryView_head_width, defHeadWidth);
        headHeight = a.getDimension(R.styleable.BatteryView_head_height, defHeadHeight);
        headPadding = a.getDimension(R.styleable.BatteryView_head_padding, 2);
        headColor = a.getColor(R.styleable.BatteryView_head_color, Color.BLACK);

        insidePadding = a.getDimension(R.styleable.BatteryView_inside_padding, 3);
        insideRadius = a.getDimension(R.styleable.BatteryView_inside_radius, 3);

        lowPowerColor = a.getColor(R.styleable.BatteryView_lowPowerColor, Color.RED);
        highPowerColor = a.getColor(R.styleable.BatteryView_highPowerColor, Color.BLACK);
        chargingColor = a.getColor(R.styleable.BatteryView_chargingColor, Color.GREEN);
        a.recycle();
    }

    private void initPaint() {
        //外框
        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);

        //电池头
        headPaint = new Paint();
        headPaint.setAntiAlias(true);
        headPaint.setStyle(Paint.Style.FILL);

        //内部
        insidePaint = new Paint();
        insidePaint.setAntiAlias(true);
        insidePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mMeasureWidth = measureSize(widthMeasureSpec, minWidth);
        int mMeasureHeight = measureSize(heightMeasureSpec, minHeight);
        setMeasuredDimension(mMeasureWidth, mMeasureHeight);
    }

    private int measureSize(int measureSpec, int defValue) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        //设置一个默认值，这个看我们自定义View的要求
        int result = defValue;
        if (specMode == MeasureSpec.EXACTLY) {//相当于我们设置为match_parent或者为一个具体的值
            result = Math.max(specSize, defValue);
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defValue, specSize);
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isCharging) {
            borderPaint.setColor(chargingColor);
            headPaint.setColor(chargingColor);
            insidePaint.setColor(chargingColor);
        } else {
            if (power <= maxPower/10) {
                borderPaint.setColor(lowPowerColor);
                headPaint.setColor(lowPowerColor);
                insidePaint.setColor(lowPowerColor);
            } else {
                borderPaint.setColor(borderColor);
                headPaint.setColor(headColor);
                insidePaint.setColor(highPowerColor);
            }
        }

        if (orientation == 0) {
            drawVertical(canvas);
        } else {
            drawHorizontal(canvas);
        }

        batteryHandler.removeCallbacks(chargingTask);
        if (isCharging) {
            batteryHandler.postDelayed(chargingTask, 1000);
        }
    }

    /**
     * 画竖向电池
     */
    private void drawVertical(Canvas canvas) {
        // 画外框
        RectF borderRectF = new RectF(
                borderWidth /2 + getPaddingLeft(),
                borderWidth /2 + getPaddingTop() + headHeight + headPadding,
                getWidth() - borderWidth /2 - getPaddingRight(),
                getHeight() - borderWidth /2 - getPaddingBottom()
        );
        canvas.drawRoundRect(borderRectF, borderRadius, borderRadius, borderPaint);

        // 画头部
        RectF headRectF = new RectF(
                (getWidth() - headWidth) / 2,
                getPaddingTop(),
                (getWidth() + headWidth) / 2,
                getPaddingTop() + headHeight
        );
        canvas.drawRoundRect(headRectF, headHeight/2, headHeight/2, headPaint);

        // 画内部
        float insideHeight = getHeight() - getPaddingTop() - getPaddingBottom() - headHeight - headPadding - borderWidth*2 - insidePadding*2;
        float insideTop = insideHeight * power / maxPower;
        float mInsideRadius = insideRadius;
        if (insideTop < mInsideRadius) {
            mInsideRadius = insideTop;
        }

        RectF insideRectF = new RectF(
                getPaddingLeft() + borderWidth + insidePadding,
                getHeight() - getPaddingBottom() - borderWidth - insidePadding - insideTop,
                getWidth() - getPaddingRight() - borderWidth - insidePadding,
                getHeight() - getPaddingBottom() - borderWidth - insidePadding
        );
        canvas.drawRoundRect(insideRectF, mInsideRadius, mInsideRadius, insidePaint);
    }

    /**
     * 画横向电池
     */
    private void drawHorizontal(Canvas canvas) {
        // 画外框
        RectF borderRectF = new RectF(
                borderWidth /2 + getPaddingLeft(),
                borderWidth /2 + getPaddingTop(),
                getWidth() - borderWidth /2 - getPaddingRight() - headWidth - headPadding,
                getHeight() - borderWidth /2 - getPaddingBottom()
        );
        canvas.drawRoundRect(borderRectF, borderRadius, borderRadius, borderPaint);

        // 画头部
        RectF headRectF = new RectF(
                getWidth() - getPaddingRight() - headWidth,
                (getHeight() - headHeight) / 2f,
                getWidth() - getPaddingRight(),
                (getHeight() + headHeight) / 2f
        );
        canvas.drawRoundRect(headRectF, headWidth/2, headWidth/2, headPaint);

        // 画内部
        float insideWidth = getWidth() - getPaddingLeft() - getPaddingRight() - headWidth - headPadding - borderWidth*2 - insidePadding*2;
        float insideRight = insideWidth * power / maxPower;
        float mInsideRadius = insideRadius;
        if (insideWidth < mInsideRadius) {
            mInsideRadius = insideRight;
        }
        RectF insideRectF = new RectF(
                getPaddingLeft() + borderWidth + insidePadding,
                getPaddingTop() + borderWidth + insidePadding,
                getPaddingLeft() + borderWidth + insidePadding + insideRight,
                getHeight() - getPaddingBottom() - borderWidth - insidePadding
        );
        canvas.drawRoundRect(insideRectF, mInsideRadius, mInsideRadius, insidePaint);
    }

    /**
     * 获取当前电量
     */
    private int getCurrentPower() {
        Intent batteryStatus = getContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //当前剩余电量
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        return verifyPower(level);
    }

    /**
     * 验证数值，把电量分成 10 个等级
     */
    private int verifyPower(int power) {
        int p;
        if (power <= maxPower/10) {
            p = maxPower/10;
        } else if (power <= maxPower*2/10) {
            p = maxPower/5;
        } else if (power <= maxPower*3/10) {
            p = maxPower*3/10;
        } else if (power <= maxPower*4/10) {
            p = maxPower*4/10;
        } else if (power <= maxPower*5/10) {
            p = maxPower*5/10;
        } else if (power <= maxPower*6/10) {
            p = maxPower*6/10;
        } else if (power <= maxPower*7/10) {
            p = maxPower*7/10;
        } else if (power <= maxPower*8/10) {
            p = maxPower*8/10;
        } else if (power <= maxPower*9/10) {
            p = maxPower*9/10;
        } else {
            p = maxPower;
        }
        return p;
    }

    /**
     * 获取当前充电状态
     */
    public boolean isCharging() {
        return isCharging;
    }

    /**
     * 设置方向，vertical：0，horizontal：1
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
        invalidate();
    }

    /**
     * 设置最小宽度
     */
    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
        invalidate();
    }

    /**
     * 设置最小高度
     */
    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
        invalidate();
    }

    /**
     * 设置最大电量
     */
    public void setMaxPower(int maxPower) {
        this.maxPower = maxPower;
    }

    /**
     * 设置电量
     */
    public void setPower(int power) {
        this.power = verifyPower(power);
        invalidate();
    }

    /**
     * 设置充电状态
     */
    public void setCharging(boolean charging) {
        isCharging = charging;
        if (!charging) {
            batteryHandler.removeCallbacks(chargingTask);
        }
        setPower(getCurrentPower());
    }

    /**
     * 当view离开附着的窗口时，释放资源
     */
    @Override
    protected void onDetachedFromWindow() {
        batteryHandler.removeCallbacks(chargingTask);
        getContext().unregisterReceiver(batteryReceiver);
        super.onDetachedFromWindow();
    }

}
