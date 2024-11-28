package com.example.gsyvideoplayer.holder;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.VideoModel;
import com.shuyu.gsyvideoplayer.utils.GSYVideoHelper;


/**
 * Created by GUO on 2015/12/3.
 */
public class RecyclerPageItemViewHolder extends RecyclerItemBaseHolder {

    public final static String TAG = "RecyclerPageItemViewHolder";

    protected Context context ;
    protected TextView textView ;

    public RecyclerPageItemViewHolder(Context context, View v) {
        super(v);
        this.context = context;
        textView = v.findViewById(R.id.viewpage_demo_text);
    }

    public void setText(String text) {
        textView.setText(text);
    }
}





