# BatteryView

[![](https://jitpack.io/v/com.gitee.zhuminjun/BatteryView.svg)](https://jitpack.io/#com.gitee.zhuminjun/BatteryView)

#### 介绍
自定义电池电量View

## 使用

1. 在 settings.gradle 里面

    ```
    dependencyResolutionManagement {
        repositories {
            // 添加下面这行配置
            maven { url 'https://jitpack.io' }
        }
    }
    ```

2. 在 gradle.properties 里面

    ```
    // 添加这两行使应用对support库的依赖自动转换为androidx的依赖
    android.useAndroidX=true
    android.enableJetifier=true   
    ```

3. 在应用 build.gradle 里面

    ```
    dependencies {
        implementation 'com.gitee.zhuminjun:BatteryView:last-release'
    }
    ```

## 功能

```
/**
 * 方向：竖向，电池头在上面
 */
public static final int VERTICAL = 0;
/**
 * 方向：横向，电池头在右边
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

<declare-styleable name="BatteryView">
     //是否自动检测系统电量
     <attr name="isAutoDetect" format="boolean"/>
     //方向
     <attr name="orientation" format="enum">
         <enum name="vertical" value="0"/>
         <enum name="horizontal" value="1"/>
     </attr>
     //最小宽度
     <attr name="minWidth" format="dimension"/>
     //最小高度
     <attr name="minHeight" format="dimension"/>
     //电量最大值
     <attr name="maxPower" format="integer"/>
     //电池外框宽度
     <attr name="border_width" format="dimension"/>
     //电池外框半径
     <attr name="border_radius" format="dimension"/>
     //电池外框颜色
     <attr name="border_color" format="color"/>
     //电池头宽度
     <attr name="head_width" format="dimension"/>
     //电池头高度
     <attr name="head_height" format="dimension"/>
     //电池头距电池外框的距离
     <attr name="head_padding" format="dimension"/>
     //电池头颜色
     <attr name="head_color" format="color"/>
     //电池内框距外框的距离
     <attr name="inside_padding" format="dimension"/>
     //电池内框四角半径
     <attr name="inside_radius" format="dimension"/>
     //低电量颜色
     <attr name="lowPowerColor" format="color"/>
     //高电量颜色
     <attr name="highPowerColor" format="color"/>
     //充电中颜色
     <attr name="chargingColor" format="color"/>
     //充电动画
     <attr name="chargingMode" format="enum">
         <enum name="lightning" value="0"/>
         <enum name="step" value="1"/>
     </attr>
</declare-styleable>

isAutoDetect：获取是否自动检测系统电量，true: 启动内置的电量广播监听系统电量和状态
isCharging：获取当前充电状态
getOrientation：获取当前方向
getChargingAnimMode：获取当前充电动画

setAutoDetect：设置是否自动检测系统电量
setOrientation：设置方向
setMinWidth：设置最小宽度
setMinHeight：设置最小高度
setMaxPower：设置最大电量
setPower：设置电量
setCharging：设置充电状态
setChargingAnimMode：设置充电动画
```
