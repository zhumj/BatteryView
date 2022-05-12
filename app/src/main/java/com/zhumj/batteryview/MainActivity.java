package com.zhumj.batteryview;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    BatteryView batteryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryView = findViewById(R.id.batteryView);

    }

    /**
     * 设置充电状态
     */
    public void btnChargingClick(View view) {
        batteryView.setCharging(!batteryView.isCharging());
    }

    /**
     * 设置充电动画模式
     */
    public void btnModeClick(View view) {
        if (batteryView.getChargingAnimMode() == BatteryView.LIGHTNING) {
            batteryView.setChargingAnimMode(BatteryView.STEP);
        } else {
            batteryView.setChargingAnimMode(BatteryView.LIGHTNING);
        }
    }

    /**
     * 设置方向
     */
    public void btnOrientationClick(View view) {
        if (batteryView.getOrientation() == BatteryView.VERTICAL) {
            batteryView.setOrientation(BatteryView.HORIZONTAL);
        } else {
            batteryView.setOrientation(BatteryView.VERTICAL);
        }
    }

}