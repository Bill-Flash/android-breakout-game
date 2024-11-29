package com.example.watchoutbricks;

import android.app.Application;
import android.util.Log;

import com.example.watchoutbricks.storage.SaveRank;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

import cn.bmob.v3.Bmob;

/**
 * use TPush lib and get doc from https://console.cloud.tencent.com/tpns/push/create-push
 * The App is used to load data from cloud database and getPush message from server
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // db
        Bmob.initialize(this, "76c3b628f7856089302d52eea80bbbf8");
        SaveRank.query("bb36837e4d", 0);
        SaveRank.query("cd22d93ff7", 1);
        SaveRank.query("93c2b14458", 2);
        // TPush to receive the nofication e.g. the new records!
        XGPushConfig.enableDebug(this,false);
        XGPushManager.registerPush(this, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                //token will change for different device
                Log.d("TPush", "Success, token is：" + data);
            }
            @Override
            public void onFail(Object data, int errCode, String msg) {
                Log.d("TPush", "Fail, error code is：" + errCode + ",error info：" + msg);
            }
        });
    }

}
