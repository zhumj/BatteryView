package com.zhumj.batteryview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * @author Created by zhumj
 * @date 2022/5/11 9:06
 * @description : 电池电量View
 */
public class BatteryView extends View {

    /**
     * 方向：竖向
     */
    public static final int VERTICAL = 0;
    /**
     * 方向：横向
     */
    public static final int HORIZONTAL = 1;
    /**
     * 充电状态动画：闪电
     */
    public static final int LIGHTNING = 0;
    /**
     * 充电状态动画：步进动画
     */
    public static final int STEP = 1;

    private boolean isAutoDetect;//是否自动检测系统电量
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

    private Paint lightningPaint; // 电池内部画笔
    private int chargingAnimMode;//充电状态动画，LIGHTNING = 0：闪电，STEP = 1：步进动画

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
     * 充电步进动画
     */
    private final Runnable chargingTask = () -> {
        if (power >= maxPower) {
            power = maxPower/5;
        } else {
            if (power == maxPower/5 || power == maxPower*2/5 || power == maxPower*3/5 || power == maxPower*4/5) {
                power += maxPower/5;
            } else {
                if (power <= maxPower/5) {
                    power = maxPower/5;
                }
                else if (power <= maxPower*2/5) {
                    power = maxPower*2/5;
                }
                else if (power <= maxPower*3/5) {
                    power = maxPower*3/5;
                }
                else if (power <= maxPower*4/5) {
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
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction()) && isAutoDetect()) {
                setMaxPower(intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
                int currentPower = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
                boolean currentCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
                if (isCharging() != currentCharging) {
                    setCharging(currentCharging);
                } else {
                    if (currentCharging && chargingAnimMode == STEP) {
                        return;
                    }
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
        isAutoDetect = a.getBoolean(R.styleable.BatteryView_isAutoDetect, false);
        orientation = a.getInt(R.styleable.BatteryView_orientation, VERTICAL);

        int defMinWidth = 72;
        int defHeight = 135;
        int defHeadWidth = 36;
        int defHeadHeight = 8;
        if (orientation == 1) {
            defMinWidth = 135;
            defHeight = 72;
            defHeadWidth = 8;
            defHeadHeight = 36;
        }

        minWidth = (int) a.getDimension(R.styleable.BatteryView_minWidth, defMinWidth);
        minHeight = (int) a.getDimension(R.styleable.BatteryView_minHeight, defHeight);
        maxPower = a.getInt(R.styleable.BatteryView_maxPower, 100);
        power = maxPower;

        borderWidth = a.getDimension(R.styleable.BatteryView_border_width, 6);
        borderRadius = a.getDimension(R.styleable.BatteryView_border_radius, 6);
        borderColor = a.getColor(R.styleable.BatteryView_border_color, Color.BLACK);

        headWidth = a.getDimension(R.styleable.BatteryView_head_width, defHeadWidth);
        headHeight = a.getDimension(R.styleable.BatteryView_head_height, defHeadHeight);
        headPadding = a.getDimension(R.styleable.BatteryView_head_padding, 4);
        headColor = a.getColor(R.styleable.BatteryView_head_color, Color.BLACK);

        insidePadding = a.getDimension(R.styleable.BatteryView_inside_padding, 4);
        insideRadius = a.getDimension(R.styleable.BatteryView_inside_radius, 4);

        chargingAnimMode = a.getInt(R.styleable.BatteryView_chargingMode, LIGHTNING);

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

        //闪电
        lightningPaint = new Paint();
        lightningPaint.setAntiAlias(true);
        lightningPaint.setStyle(Paint.Style.FILL);
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

        insidePaint.setAlpha(255);
        if (isCharging) {
            borderPaint.setColor(chargingColor);
            headPaint.setColor(chargingColor);
            insidePaint.setColor(chargingColor);

            if (chargingAnimMode == LIGHTNING) {
                insidePaint.setAlpha(72);
                lightningPaint.setColor(chargingColor);
            }
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

        if (orientation == VERTICAL) {
            drawVertical(canvas);
        } else {
            drawHorizontal(canvas);
        }

        batteryHandler.removeCallbacks(chargingTask);
        if (isCharging) {
            if (chargingAnimMode == LIGHTNING) {
                canvas.drawPath(getLightningPath(), lightningPaint);
            } else {
                batteryHandler.postDelayed(chargingTask, 1000);
            }
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

    private Path getLightningPath() {
        Path path = new Path();
        if (orientation == VERTICAL) {
            float lightningWidth = getWidth() - getPaddingLeft() - getPaddingRight() - borderWidth*2 - insidePadding*2;
            float lightningPaddingH = lightningWidth/10;
            lightningWidth = lightningWidth - lightningPaddingH*2;

            float lightningHeight = getHeight() - getPaddingTop() - getPaddingBottom() - headHeight - headPadding - borderWidth*2 - insidePadding*2;
            float lightningPaddingV = lightningHeight/10;
            lightningHeight = lightningHeight - lightningPaddingV*2;

            float startX = getPaddingLeft() + borderWidth + insidePadding + lightningPaddingH;
            float startY = getPaddingTop() + headHeight + headPadding + borderWidth + insidePadding + lightningPaddingV;
            path.moveTo(startX + lightningWidth*2/3, startY);
            path.lineTo(startX + lightningWidth*2/3, startY + lightningHeight*2/5);
            path.lineTo(startX + lightningWidth, startY + lightningHeight*2/5);
            path.lineTo(startX + lightningWidth/3, startY + lightningHeight);
            path.lineTo(startX + lightningWidth/3, startY + lightningHeight*3/5);
            path.lineTo(startX, startY + lightningHeight*3/5);
            path.lineTo(startX + lightningWidth*2/3, startY);
        } else {
            float lightningWidth = getWidth() - getPaddingLeft() - getPaddingRight() - headWidth - headPadding - borderWidth*2 - insidePadding*2;
            float lightningPaddingH = lightningWidth/10;
            lightningWidth = lightningWidth - lightningPaddingH*2;

            float lightningHeight = getHeight() - getPaddingTop() - getPaddingBottom() - borderWidth*2 - insidePadding*2;
            float lightningPaddingV = lightningHeight/10;
            lightningHeight = lightningHeight - lightningPaddingV*2;

            float startX = getPaddingLeft() + borderWidth + insidePadding + lightningPaddingH;
            float startY = getPaddingTop() + borderWidth + insidePadding + lightningPaddingV;
            path.moveTo(startX, startY + lightningHeight/3);
            path.lineTo(startX + lightningWidth*2/5, startY + lightningHeight/3);
            path.lineTo(startX + lightningWidth*2/5, startY);
            path.lineTo(startX + lightningWidth, startY + lightningHeight*2/3);
            path.lineTo(startX + lightningWidth*3/5, startY + lightningHeight*2/3);
            path.lineTo(startX + lightningWidth*3/5, startY + lightningHeight);
            path.lineTo(startX, startY + lightningHeight/3);
        }
        path.close();
        return path;
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
        if (power <= 0) {
            p = 0;
        } else if (power <= maxPower/10) {
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
     * 获取是否自动检测系统电量
     */
    public boolean isAutoDetect() {
        return isAutoDetect;
    }

    /**
     * 获取当前充电状态
     */
    public boolean isCharging() {
        return isCharging;
    }

    public int getOrientation() {
        return orientation;
    }

    public int getChargingAnimMode() {
        return chargingAnimMode;
    }

    /**
     * 设置是否自动检测系统电量
     */
    public void setAutoDetect(boolean autoDetect) {
        isAutoDetect = autoDetect;
    }

    /**
     * 设置方向，vertical：0，horizontal：1
     */
    public void setOrientation(int orientation) {
        if (orientation != getOrientation()) {
            // 使用异或方法交换数值
            minWidth = minWidth^minHeight;
            minHeight = minWidth^minHeight;
            minWidth = minWidth^minHeight;

            // float 用不了异或方法，那就使用加减
            float totalHead = headWidth + headHeight;
            headWidth = totalHead - headWidth;
            headHeight = totalHead - headHeight;

            this.orientation = orientation;
            requestLayout();
        }
    }

    /**
     * 设置最小宽度
     */
    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
        requestLayout();
    }

    /**
     * 设置最小高度
     */
    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
        requestLayout();
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
        if (isCharging() != charging) {
            isCharging = charging;
            if (!charging) {
                batteryHandler.removeCallbacks(chargingTask);
            }
            if (isAutoDetect()) {
                setPower(getCurrentPower());
            } else {
                invalidate();
            }
        }
    }

    /**
     * 设置充电状态模式
     */
    public void setChargingAnimMode(int chargingMode) {
        if (getChargingAnimMode() != chargingMode) {
            this.chargingAnimMode = chargingMode;
            if (isCharging()) {
                batteryHandler.removeCallbacks(chargingTask);
                if (isAutoDetect()) {
                    setPower(getCurrentPower());
                } else {
                    invalidate();
                }
            }
        }
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
