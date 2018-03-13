package com.chk.fileexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.chk.fileexplorer.Interfaces.OnDialogButtonClickListener;
import com.chk.fileexplorer.R;

/**
 * Created by chk on 18-2-3.
 */

public class WaitingDialog extends Dialog {

    Context mContext;
    int mLayoutId;

    OnDialogButtonClickListener mOnDialogButtonClickListener;

    public WaitingDialog(@NonNull Context context) {
        super(context,R.style.Custom_Dialog_Style);
        this.mContext = context;
    }

    public WaitingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    public WaitingDialog(@NonNull Context context, int themeResId, int layoutId) {
        super(context,themeResId);
        this.mContext = context;
        this.mLayoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_waiting);
    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener mOnDialogButtonClickListener) {
        this.mOnDialogButtonClickListener = mOnDialogButtonClickListener;
    }
}
