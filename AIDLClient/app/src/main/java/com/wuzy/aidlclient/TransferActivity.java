package com.wuzy.aidlclient;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;

public class TransferActivity extends AppCompatActivity {

    public static final String ACTION_FROM_SELF = "com.wuzy.aidlclient.TransferActivity.FROM_SELF";
    public static final String ACTION_FROM_OTHER = "com.wuzy.aidlclient.TransferActivity.FROM_OTHER";

    private static final String TAG = "TransferActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: 创建中转 Activity");

        Window window = getWindow();
        window.setGravity(Gravity.START | Gravity.TOP);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = attributes.height = 1;
        attributes.x = attributes.y = 0;

        if (getIntent() != null) {
            Intent intent;
            if (ACTION_FROM_OTHER.equals(getIntent().getAction())) {
                intent = new Intent(this, LocalService.class);
                startService(intent);
            } else if (ACTION_FROM_SELF.equals(getIntent().getAction())) {
                intent = new Intent("com.wuzy.aidlserver.TransferActivity.FROM_OTHER");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(new ComponentName("com.wuzy.aidlserver", "com.wuzy.aidlserver.TransferActivity"));
                startActivity(intent);
            }
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: 销毁中转 Activity");
    }

    //判断Service是否在运行

    private boolean isServiceRunning(Context context, String serviceName) throws ClassNotFoundException {

        if (("").equals(serviceName) || serviceName == null) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(Integer.MAX_VALUE);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(serviceName)) {
                return true;
            }
        }
        return false;

    }

}
