package com.manvir.SnapColors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;

import com.manvir.logger.Logger;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveSnapTask implements Runnable {
    private final String mSender;
    private final Object mSnap;
    private final int mMediaType;
    private final Context mContext;

    public SaveSnapTask(Context context, String mSender, Object mSnap, int mMediaType) {
        this.mContext = context;
        this.mSender = mSender;
        this.mSnap = mSnap;
        this.mMediaType = mMediaType;
    }

    @Override
    public void run() {
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
        switch (mMediaType) {
            case 0: //Image
                String outFolderImage;
                if (App.prefs.getBoolean("shouldSaveInSub", true)) {
                    outFolderImage = App.prefs.getString("saveLocation", Util.SDCARD_SNAPCOLORS) + "/" + mSender;
                } else {
                    outFolderImage = App.prefs.getString("saveLocation", Util.SDCARD_SNAPCOLORS);
                }
                File outFileImage = new File(outFolderImage, mSender + "_" + date + ".png");
                Util.saveBitmap((Bitmap) mSnap, outFileImage);
                Util.runMediaScanner(mContext, outFileImage);
                break;
            case 1: //Video
                FileInputStream videoData = (FileInputStream) mSnap;
                try {
                    String outFolderVideo;
                    if (App.prefs.getBoolean("shouldSaveInSub", true)) {
                        outFolderVideo = App.prefs.getString("saveLocation", Util.SDCARD_SNAPCOLORS) + "/" + mSender;
                    } else {
                        outFolderVideo = App.prefs.getString("saveLocation", Util.SDCARD_SNAPCOLORS);
                    }
                    File outFileVideo = new File(outFolderVideo, mSender + "_" + date + ".mp4");
                    FileUtils.copyInputStreamToFile(videoData, outFileVideo);
                    Util.runMediaScanner(mContext, outFileVideo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Logger.log("Unknown snap type");
                break;
        }
    }
}
