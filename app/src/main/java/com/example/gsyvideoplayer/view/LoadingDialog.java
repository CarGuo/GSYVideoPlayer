package com.example.gsyvideoplayer.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.gsyvideoplayer.R;

public class LoadingDialog extends Dialog {

    private Context context;

    public LoadingDialog(Context context) {
        super(context, R.style.dialog_style);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_loading_dialog, null);
        setContentView(view);

        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }
}