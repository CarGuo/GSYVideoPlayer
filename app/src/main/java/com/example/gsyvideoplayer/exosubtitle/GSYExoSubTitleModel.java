package com.example.gsyvideoplayer.exosubtitle;

import com.google.android.exoplayer2.text.TextOutput;
import com.shuyu.gsyvideoplayer.model.GSYModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guoshuyu on 2018/5/16.
 * 自定义列表数据model
 */

public class GSYExoSubTitleModel extends GSYModel {

    private String subTitle;
    private TextOutput textOutput;

    public GSYExoSubTitleModel(String url, String subTitle, TextOutput textOutput, Map<String, String> mapHeadData, boolean loop, float speed, boolean cache, File cachePath, String overrideExtension) {
        super(url, mapHeadData, loop, speed, cache, cachePath, overrideExtension);
        this.subTitle = subTitle;
        this.textOutput = textOutput;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public TextOutput getTextOutput() {
        return textOutput;
    }

    public void setTextOutput(TextOutput textOutput) {
        this.textOutput = textOutput;
    }
}
