package com.example.gsyvideoplayer.exo;

import com.shuyu.gsyvideoplayer.model.GSYModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guoshuyu on 2018/5/16.
 * 自定义列表数据model
 */

public class GSYExoModel extends GSYModel {

    List<String> urls = new ArrayList<>();
    int index = 0;

    public GSYExoModel(List<String> urls, Map<String, String> mapHeadData, int index, boolean loop, float speed, boolean cache, File cachePath, String overrideExtension) {
        super("", mapHeadData, loop, speed, cache, cachePath, overrideExtension);
        this.urls = urls;
        this.index = index;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

}
