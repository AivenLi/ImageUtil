package com.bysj.imgevaluation.listener;

import com.bysj.imgevaluation.bean.EvaluatBean;

import java.util.ArrayList;

/**
 * 获取图片的所有参数回调。
 * 如果回调成功方法，它的参数是一个列表。
 *
 * Create on 2021-2-27
 */
public interface EvaluatAllListener<T> {

    void success(ArrayList<T> data);
    void failure(String error);
}
