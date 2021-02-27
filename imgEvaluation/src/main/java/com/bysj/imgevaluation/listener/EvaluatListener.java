package com.bysj.imgevaluation.listener;

import android.graphics.Bitmap;

import com.bysj.imgevaluation.bean.EvaluatBean;

/**
 * 异步处理回调
 *
 * Create on 2021-2-27
 */

public interface EvaluatListener {

    void success(EvaluatBean evaluatBean);
    void failure(String error);
}
