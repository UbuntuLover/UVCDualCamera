package com.newmobi.business;


/**
 * Created by Alextao on 2018/7/23,星期一.
 * Email : tao_xue@new-mobi.com
 */
public class ImageData {
    private byte[] data;
    private int width;
    private int height;
    private int format;
    private int faceOrientation;

    ImageData copy() {
        ImageData copyData = new ImageData();
        copyData.data = data;
        copyData.width = width;
        copyData.height = height;
        copyData.format = format;
        copyData.faceOrientation = faceOrientation;
        return copyData;
    }

    void clear() {
        data = null;
        width = 0;
        height = 0;
        format = 0;
        faceOrientation = 0;
    }

    boolean isEmpty() {
        return data == null;
    }
}

