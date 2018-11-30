package com.wuzy.aidlserver;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class RemoteService extends Service {

    private static final String TAG = "RemoteService";

    private IMyAidlInterface iMyAidlInterface;

    private boolean mIsBound;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: 创建 RemoteService");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bindLocalService();
        return START_STICKY;
    }

    private IMyAidlInterface.Stub stub = new IMyAidlInterface.Stub() {
        @Override
        public void bindSuccess() throws RemoteException {
            Log.e(TAG, "bindSuccess: LocalService 绑定 RemoteService 成功");
        }

        @Override
        public void unbind() throws RemoteException {
            Log.e(TAG, "unbind: 此处解除 RemoteService 与 LocalService 的绑定");
            getApplicationContext().unbindService(connection);
        }
    };

    /**
     * 绑定 LocalService
     */
    private void bindLocalService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.wuzy.aidlclient", "com.wuzy.aidlclient.LocalService"));
        if (!getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "bindLocalService: 绑定 LocalService 失败");
            stopSelf();
        }
    }

    /**
     * 解除绑定 LocalService
     */
    private void unbindLocalService() {
        if (mIsBound) {
            try {
                iMyAidlInterface.unbind();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            getApplicationContext().unbindService(connection);
            stopSelf();
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected: LocalService 链接成功");
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
            Log.e(TAG, "onServiceDisconnected: LocalService 断开链接，重新启动");
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

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: 解绑RemoteService");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: 销毁 RemoteService");
        super.onDestroy();
    }

}
