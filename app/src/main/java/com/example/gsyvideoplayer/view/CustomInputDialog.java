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

public class CustomInputDialog extends Dialog {

    private Context context;
    private EditText editMessage;

    public EditText getEditMessage() {
        return editMessage;
    }

    public void setEditMessage(EditText editMessage) {
        this.editMessage = editMessage;
    }

    private TextView txtTitle;
    private TextView btnPostive;
    private TextView btnNegative;
    private CheckBox cacheCheck;
    private OnClickListener onPostiveClickListener;
    private OnClickListener onNegativeClickListener;
    private String postiveText;
    private String negativeText;
    private String input;
    private String hintInput;
    private String title;

    private int maxLength = 500;
    private boolean needDismissOnClick = true;
    private boolean cache = false;
    private boolean postiveButtonVisiable = false;
    private boolean negativeButtonVisiable = false;

    public CustomInputDialog(Context context) {
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
        View view = inflater.inflate(R.layout.layout_custom_dialog, null);
        setContentView(view);
        txtTitle = (TextView) view.findViewById(R.id.dialog_input_title);
        editMessage = (EditText) view.findViewById(R.id.dialog_input_dialog_edit);
        btnPostive = (TextView) view.findViewById(R.id.dialog_input_dialog_postive);
        btnNegative = (TextView) view.findViewById(R.id.dialog_input_dialog_negative);
        cacheCheck = (CheckBox) view.findViewById(R.id.dialog_input_check);

        cacheCheck.setChecked(cache);

        if (postiveButtonVisiable) {
            btnPostive.setVisibility(View.VISIBLE);
        } else {
            btnPostive.setVisibility(View.GONE);
        }
        if (negativeButtonVisiable) {
            btnNegative.setVisibility(View.VISIBLE);
        } else {
            btnNegative.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(title)) {
            txtTitle.setVisibility(View.GONE);
        } else {
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(title);
        }
        editMessage.setText(input);
        if (input != null) {
            editMessage.setSelection(input.length());
        }
        if (!TextUtils.isEmpty(hintInput)) {
            editMessage.setHint(hintInput);
        }
        btnPostive.setText(postiveText);
        btnNegative.setText(negativeText);
        btnPostive.setOnClickListener(new OnButtonClickListener());
        btnNegative.setOnClickListener(new OnButtonClickListener());
        InputFilter[] filters = {new InputFilter.LengthFilter(maxLength)};
        editMessage.setFilters(filters);
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(lp);
    }

    public void setInput(int msgId) {
        input = context.getString(msgId);
    }

    public void setInput(String msg) {
        input = msg;
    }

    public void setHintInput(String msg) {
        hintInput = msg;
    }

    public void setTitle(int msgId) {
        title = context.getString(msgId);
    }

    public void setTitle(String msg) {
        title = msg;
    }


    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public void setNeedDismissOnClick(boolean needDismissOnClick) {
        this.needDismissOnClick = needDismissOnClick;
    }

    public void setButton(int which, String text, OnClickListener onClickListener) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                postiveButtonVisiable = true;
                postiveText = text;
                setOnPostiveClickListener(onClickListener);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                negativeButtonVisiable = true;
                negativeText = text;
                setOnNegativeClickListener(onClickListener);
                break;
            default:
                break;
        }
    }

    public void setOnPostiveClickListener(OnClickListener onPostiveClickListener) {
        this.onPostiveClickListener = onPostiveClickListener;
    }

    public void setOnNegativeClickListener(OnClickListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
    }

    private class OnButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (R.id.dialog_input_dialog_postive == v.getId()) {
                if (onPostiveClickListener != null) {
                    onPostiveClickListener.onClick(CustomInputDialog.this, DialogInterface.BUTTON_POSITIVE);
                    String newInput = editMessage.getText().toString();
                    if (!newInput.equals(input)) {
                        input = newInput;
                        onPostiveClickListener.onInputChanged(input, cacheCheck.isChecked());
                    }
                }
            } else if (R.id.dialog_input_dialog_negative == v.getId()) {
                if (onNegativeClickListener != null) {
                    onNegativeClickListener.onClick(CustomInputDialog.this, DialogInterface.BUTTON_NEGATIVE);
                }
            }
            if (needDismissOnClick) {
                dismiss();
            }
        }

    }

    public interface OnClickListener extends DialogInterface.OnClickListener {
        public void onInputChanged(String input, boolean cache);
    }


    public void collapseSoftInputMethod() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void dismiss() {
        collapseSoftInputMethod();
        super.dismiss();
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

}