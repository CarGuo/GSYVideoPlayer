package com.example.gsyvideoplayer.utils;

import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

public abstract class MemoryCallBack extends Callback<Boolean> {


    @Override
    public Boolean parseNetworkResponse(Response response, int id) throws Exception {
        return saveFile(response, id);
    }


    //// 模拟下载，这样可以和 proxy cache 公用一个本地cache
    public boolean saveFile(Response response, final int id) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        try {
            is = response.body().byteStream();
            final long total = response.body().contentLength();

            long sum = 0;

            while ((len = is.read(buf)) != -1) {
                sum += len;
                final long finalSum = sum;
                OkHttpUtils.getInstance().getDelivery().execute(new Runnable() {
                    @Override
                    public void run() {

                        Debuger.printfLog("######### inProgress" + finalSum * 1.0f / total);
                        inProgress(finalSum * 1.0f / total, total, id);
                    }
                });
            }
            return true;

        } finally {
            try {
                response.body().close();
                if (is != null) is.close();
            } catch (IOException e) {
            }

        }
    }
}