package com.shuyu.gsyvideoplayer.utils;

import android.os.Environment;

import java.io.File;

public class FileUtils {

    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();

    public static final String NAME = "GSYVideo";

    public static final String NAME_TEST = "GSYVideoTest";


    public static String getAppPath(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(SD_PATH);
        sb.append(File.separator);
        sb.append(name);
        sb.append(File.separator);
        return sb.toString();
    }

    public static String getPath() {
        return getAppPath(NAME);
    }

    public static String getTestPath() {
        return getAppPath(NAME_TEST);
    }

    public static void deleteFiles(File root) {
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory() && f.exists()) { // 判断是否存在
                    try {
                        f.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}