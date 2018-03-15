package com.chk.fileexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.chk.fileexplorer.Interfaces.OnDialogButtonClickListener;
import com.chk.fileexplorer.R;

/**
 * Created by chk on 18-2-3.
 */

public class WaitingDialog extends Dialog {

    Context mContext;
    int mLayoutId;
    TextView curPath;

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
//        Display display = getWindow().getWindowManager().getDefaultDisplay();
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = display.getWidth(); // 设置dialog宽度为屏幕的4/5
//        lp.height = display.getHeight();
//        lp.gravity = Gravity.CENTER;
//        getWindow().setAttributes(lp);

        curPath = findViewById(R.id.curPath);
    }

    public void setCurPath(String path) {
        curPath.setText("curPath:"+path);
    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener mOnDialogButtonClickListener) {
        this.mOnDialogButtonClickListener = mOnDialogButtonClickListener;
    }
}
