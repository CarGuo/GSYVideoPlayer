package com.example.gsyvideoplayer.exo;

import com.shuyu.gsyvideoplayer.model.GSYModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guoshuyu on 2018/5/16.
 * 自定义列表数据model
 */

public class GSYExoModel extends GSYModel {

    List<String> urls = new ArrayList<>();

    boolean cache;

    public GSYExoModel(List<String> urls, Map<String, String> mapHeadData, boolean loop, float speed, boolean cache) {
        super("", mapHeadData, loop, speed);
        this.urls = urls;
        this.cache = cache;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }
}
