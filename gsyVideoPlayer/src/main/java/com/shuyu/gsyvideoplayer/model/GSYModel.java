package com.shuyu.gsyvideoplayer.model;

import java.util.Map;

/**
 * 视频内部接受数据model
 * Created by shuyu on 2016/11/11.
 */

public class GSYModel {

    String url;
    Map<String, String> mapHeadData;
    boolean looping;

    public GSYModel(String url, Map<String, String> mapHeadData, boolean loop) {
        this.url = url;
        this.mapHeadData = mapHeadData;
        this.looping = loop;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getMapHeadData() {
        return mapHeadData;
    }

    public void setMapHeadData(Map<String, String> mapHeadData) {
        this.mapHeadData = mapHeadData;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }
}
