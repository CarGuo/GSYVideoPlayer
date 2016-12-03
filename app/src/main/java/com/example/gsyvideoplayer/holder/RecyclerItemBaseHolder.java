package com.example.gsyvideoplayer.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.gsyvideoplayer.adapter.RecyclerBaseAdapter;
import com.shuyu.gsyvideoplayer.utils.ListVideoUtil;

/**
 * Created by shuyu on 2016/12/3.
 */

public class RecyclerItemBaseHolder extends RecyclerView.ViewHolder {

    RecyclerBaseAdapter recyclerBaseAdapter;

    ListVideoUtil listVideoUtil;

    public RecyclerItemBaseHolder(View itemView) {
        super(itemView);
    }

    public RecyclerBaseAdapter getRecyclerBaseAdapter() {
        return recyclerBaseAdapter;
    }

    public void setRecyclerBaseAdapter(RecyclerBaseAdapter recyclerBaseAdapter) {
        this.recyclerBaseAdapter = recyclerBaseAdapter;
    }

    public ListVideoUtil getListVideoUtil() {
        return listVideoUtil;
    }

    public void setListVideoUtil(ListVideoUtil listVideoUtil) {
        this.listVideoUtil = listVideoUtil;
    }
}
