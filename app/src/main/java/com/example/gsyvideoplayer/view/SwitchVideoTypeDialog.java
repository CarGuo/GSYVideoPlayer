package com.example.gsyvideoplayer.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.SwitchVideoModel;

import java.util.List;

public class SwitchVideoTypeDialog extends Dialog {

    private Context mContext;

    private ListView listView = null;

    private ArrayAdapter<SwitchVideoModel> adapter = null;

    private OnListItemClickListener onItemClickListener;

    private List<SwitchVideoModel> data;

    public interface OnListItemClickListener {
        void onItemClick(int position);
    }

    public SwitchVideoTypeDialog(Context context) {
        super(context, R.style.dialog_style);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initList(List<SwitchVideoModel> data, OnListItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.data = data;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.switch_video_dialog, null);
        listView = (ListView) view.findViewById(R.id.switch_dialog_list);
        setContentView(view);
        adapter = new ArrayAdapter<>(mContext, R.layout.switch_video_dialog_item, data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener());

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = mContext.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
    }

    private class OnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            dismiss();
            onItemClickListener.onItemClick(position);
        }
    }


}