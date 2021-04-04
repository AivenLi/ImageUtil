package com.bysj.imageutil.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseActivity;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.LogCat;
import com.bysj.imageutil.util.ShareUtil;
import com.yalantis.ucrop.view.UCropView;

public class SpliceResultActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "spliceResultActivity";
    private ImageView mImgResult;
    private TextView mTvSave;
    private TextView mTvShare;
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
        mImgResult.setImageURI(uri);
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
    protected int initLayout() {

        return R.layout.activity_splice_result;
    }

    @Override
    protected void initView() {

        mImgResult = findViewById(R.id.img_result);
        mTvSave    = findViewById(R.id.tv_save);
        mTvShare   = findViewById(R.id.tv_share);
    }

    @Override
    protected void setViewOnClick() {

        mTvShare.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

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
        }
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