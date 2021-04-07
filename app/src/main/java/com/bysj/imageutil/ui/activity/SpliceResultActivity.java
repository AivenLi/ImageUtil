package com.bysj.imageutil.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseActivity;
import com.bysj.imageutil.ui.components.ZoomImageView;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.LogCat;
import com.bysj.imageutil.util.ShareUtil;
import com.yalantis.ucrop.view.UCropView;

import java.io.File;
import java.lang.reflect.Method;

public class SpliceResultActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "spliceResultActivity";

    private UCropView uCropView;
    private Uri uri;

    private boolean needShare = false;
    private boolean isSaved  = false;

    private String shareImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if ( intent == null ) {

            finishThis();
        } else {

            uri = intent.getData();
            if ( uri == null ) {

                finishThis();
            }
        }
        try {

            uCropView.getCropImageView().setImageUri(uri, null);
            uCropView.getOverlayView().setShowCropFrame(false);
            uCropView.getOverlayView().setShowCropGrid(false);
            uCropView.getOverlayView().setDimmedColor(Color.TRANSPARENT);
        } catch (Exception e) {

            e.printStackTrace();
        }
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        if ( actionBar != null ) {

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.crop_result_title));
        }
    }

    @Override
    protected void onHandleMessage(Message msg) {

        int what = msg.what;

        if ( what == HandleKeys.SAVE_IMAGE_SUCCESS ) {

            isSaved = true;
            shareImagePath = (String)msg.obj;
            myToast(getString(R.string.saved, shareImagePath));
            if ( needShare ) {

                ShareUtil.share(mContext, shareImagePath);
                needShare = false;
            }
        } else if ( what == HandleKeys.SAVE_IMAGE_FAILURE ) {

            isSaved = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.crop_result_menu, menu);
        return true;
    }

    /**
     * 利用反射机制调用MenuBuilder的setOptionalIconsVisible方法设置mOptionalIconsVisible为true，给菜单设置图标时才可见
     *     让菜单同时显示图标和文字
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {

        if ( menu != null ) {

            if ( menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder") ) {

                try {

                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {

                    e.printStackTrace();
                    LogCat.d(TAG, e.toString());
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if ( id == R.id.tab_save ) {

            if ( !isSaved ) {

                saveImage();
            }
        } else if ( id == R.id.tab_share ) {

            if ( isSaved && !TextUtils.isEmpty(shareImagePath) ) {

                ShareUtil.share(mContext, shareImagePath);
            } else {

                needShare = true;
                saveImage();
            }
        } else if ( id == android.R.id.home ) {

            finishThis();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int initLayout() {

        return R.layout.activity_splice_result;
    }

    @Override
    protected void initView() {

        uCropView  = findViewById(R.id.ucrop);
    }

    @Override
    protected void setViewOnClick() {

    }

    @Override
    public void onClick(View view) {
        /*
        int id = view.getId();

        if ( id == R.id.tv_save && !isSaved ) {

            saveImage();
        } else if ( id == R.id.tv_share ) {

            if ( isSaved && !TextUtils.isEmpty(shareImagePath) ) {

                ShareUtil.share(mContext, shareImagePath);
            } else {

                needShare = true;
                saveImage();
            }
        }*/
    }

    private void saveImage() {

        Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
        FileUtils.saveImageToGallery(mContext, bitmap, new FileUtils.SaveImageListener() {
            @Override
            public void success(String filePath) {

                sendMessage(HandleKeys.SAVE_IMAGE_SUCCESS, filePath);
            }

            @Override
            public void failure(String error) {

                sendMessage(HandleKeys.SAVE_IMAGE_FAILURE, error);
            }
        });
    }
}