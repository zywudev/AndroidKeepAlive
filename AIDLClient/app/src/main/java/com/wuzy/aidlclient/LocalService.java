package com.wuzy.aidlclient;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.wuzy.aidlserver.IMyAidlInterface;

public class LocalService extends Service {

    private static final String TAG = "LocalService";

    private IMyAidlInterface iMyAidlInterface;

    private boolean mIsBound;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: 创建 LocalService");

//        // 模拟 5 秒后解除绑定
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                unbindRemoteService();
//            }
//        }, 5000);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bindRemoteService();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: 绑定 LocalService");
        return stub;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: 解绑 LocalService");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: 销毁 LocalService");
        super.onDestroy();
    }

    private IMyAidlInterface.Stub stub = new IMyAidlInterface.Stub() {
        @Override
        public void bindSuccess() throws RemoteException {
            Log.e(TAG, "bindSuccess: RemoteService 绑定 LocalService 成功");
        }

        @Override
        public void unbind() throws RemoteException {
            getApplicationContext().unbindService(connection);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected: RemoteService 链接成功");
            mIsBound = true;
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                iMyAidlInterface.bindSuccess();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: RemoteService 断开连接，重新启动");
            mIsBound = false;
            createTransferActivity();
        }
    };

    private void createTransferActivity() {
        Intent intent = new Intent(this, TransferActivity.class);
        intent.setAction(TransferActivity.ACTION_FROM_SELF);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void bindRemoteService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.wuzy.aidlserver", "com.wuzy.aidlserver.RemoteService"));
        if (!getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "bindRemoteService: 绑定 RemoteService 失败");
            stopSelf();
        }
    }

    /**
     * 解除绑定 RemoteService
     */
    private void unbindRemoteService() {
        if (mIsBound) {
            try {
                // 先让 RemoteService 解除绑定 LocalService
                iMyAidlInterface.unbind();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            getApplicationContext().unbindService(connection);  // 解除 LocalService 与 RemoteService
            stopSelf();
        }
    }
}
