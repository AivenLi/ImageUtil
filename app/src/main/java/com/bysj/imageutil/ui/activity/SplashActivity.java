package com.bysj.imageutil.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import com.bysj.imageutil.R;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.util.ActivityManageHelper;
import com.bysj.imageutil.util.LogCat;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    private static final long DELAY = 3000;
    //private DialogPrompt dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 全屏
         * */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        ActivityManageHelper.getInstance().addActivity(this);
        //dialog = new DialogPrompt(this);

        checkPerimssion();
    }

    /**
     * 重写本页面重新启动时的动作，避免在申请权限的过程中用户先切入后台再进入而获取不到权限
     */
    @Override
    public void onRestart() {

        super.onRestart();
        checkPerimssion();
    }

    /**
     * 检查权限，Android 6.0之后有的需要动态权限
     */
    private void checkPerimssion() {

        new RxPermissions(this).request(
                /*
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                */
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
                .subscribe( granted -> {

                    if ( granted ) {

                        jumpToMainActivity();
                    } else {

                        openSettingActivity(this, getString(R.string.app_perimision_prompt));
                    }
                });
    }

    /**
     * 跳到MainActivity
     */
    private void jumpToMainActivity() {

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(intent);
                ActivityManageHelper.getInstance().finshActivity(SplashActivity.this);
            }
        }, DELAY);
    }

    /**
     * 提示开启权限
     */
    public static void openSettingActivity(final Activity activity, String message) {

        showMessageOKCancel(activity, message, (dialog, which) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            LogCat.d("Perimmsion", uri.toString());
            intent.setData(uri);
            activity.startActivity(intent);
        }, (dialog, which) -> {
            //cancel
            if (ActivityManageHelper.isTextForeground(activity,"SplashActivity")){
                ActivityManageHelper.getInstance().finishAllActivity();
            }
        });
    }

    /**
     * 提示用户授予本app权限
     * @param context
     * @param message
     * @param okListener
     * @param cancleListener
     */
    public static void showMessageOKCancel(final Activity context, String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancleListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("去设置", okListener)
                .setCancelable(false)
                .setNegativeButton("取消", cancleListener)
                .create().show();
    }
}