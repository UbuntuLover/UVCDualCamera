package com.newmobi.business;

/**
 * Created by Alextao on 2018/7/23,星期一.
 * Email : tao_xue@new-mobi.com
 * refactor of the code in mainActivity.
 */
public class TrackThread implements Runnable {
    private static final int CACHED_FRAME_COUNTS = 4;

    private boolean mIsLicenseInited = false;
    private boolean mIsHandleCreated = false;
    private boolean mIsExit = false;
    private int mCurrentCacheCount = 0;

    private float mFrameScoreCache = 0.0f;

    private Object mTrackHandle = null;
    private Object mSelectorHandle = null;


    @Override
    public void run() {

    }
}
