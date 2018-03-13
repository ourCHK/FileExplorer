package com.chk.fileexplorer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chk.fileexplorer.Dialogs.WaitingDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity {

    static final String TAG = MainActivity.class.getSimpleName();

    static final int SEARCH_COMPLETED = 1;

    Button startCheck;

    ArrayList<String> mFileList;
    ArrayList<String> mRootPaths;

    ArrayList<String> mFilesName;

    Handler mHandler;
    ExecutorService mExecutorService;
    Runnable mRunnable;

    WaitingDialog mWaitingDialog;

    File tempTextFile;
    String tempPath = "/storage/emulated/0/temp.txt";
    FileOutputStream mFileOutputStream;
    OutputStreamWriter mOutputStreamWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
    }

    @SuppressLint("HandlerLeak")
    void init() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SEARCH_COMPLETED:
                        String newFileName = "/storage/emulated/0/"+FormatLongMillsToString(System.currentTimeMillis())+".txt";
                        tempTextFile.renameTo(new File(newFileName));
                        Toast.makeText(MainActivity.this, "已保存文件："+newFileName, Toast.LENGTH_SHORT).show();
                        mWaitingDialog.dismiss();
                        break;
                }
            }
        };
        viewInit();
        dataInit();
    }

    void viewInit() {
        mWaitingDialog = new WaitingDialog(this);
        mWaitingDialog.setCancelable(false);

        startCheck = findViewById(R.id.startCheck);
        startCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWaitingDialog.show();
                mExecutorService.execute(mRunnable);
            }
        });
    }

    void dataInit() {
        tempTextFile = new File(tempPath);
        if (tempTextFile.exists()) {
            tempTextFile.delete();
        } else {
            try {
                tempTextFile.createNewFile();
                mFileOutputStream = new FileOutputStream(tempTextFile);
                mOutputStreamWriter = new OutputStreamWriter(mFileOutputStream);
            } catch (IOException e) {
                Toast.makeText(this, "storage error!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        mExecutorService = Executors.newCachedThreadPool();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                for (String path:mRootPaths) {
                    openFolder(path);
                }
                try {
                    mOutputStreamWriter.flush();
                    mOutputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "文本保存失败", Toast.LENGTH_SHORT).show();
                }
                mHandler.sendEmptyMessage(SEARCH_COMPLETED);
            }
        };

        mFileList = new ArrayList<>();
        mRootPaths = new ArrayList<>();
        mFilesName = new ArrayList<>();

        getAllCardPath();
    }

    void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //大于等于M，需要动态权限
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "the app need the permission tu run,please grant it", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            } else {
                init();
            }
        } else {    //M以下直接初始化
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length >0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    Toast.makeText(this, "sorry you do not have granted the permission to me ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    /**
     * 获取存储路径
     */
    public void getAllCardPath() {
        mFileList.clear();
        StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
        try {
            Method getVolumePaths = StorageManager.class.getMethod("getVolumePaths",null);
            String[] paths = (String[]) getVolumePaths.invoke(sm,null);
            for (String path:paths) {
                Log.i(TAG,path);
                if (getStorageState(path).equals(Environment.MEDIA_MOUNTED)) {
                    Log.i(TAG,"Mounted:"+path);
                    mFileList.add(path);
                    mRootPaths.add(path);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断存储器的状态
     * @param path 存储器路劲
     * @return
     */
    public String getStorageState(String path) {
        StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
        try {
            Method getVolumeState = StorageManager.class.getMethod("getVolumeState",new Class[] {String.class});
            String state = (String) getVolumeState.invoke(sm,path);
            return state;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "null";
    }

    /**
     * 递归版本递归所有文件夹
     * @param originalFileName
     */
    public void openFolder(String originalFileName) {
        File file = new File(originalFileName);
        File[] files = file.listFiles();
        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        Collections.sort(fileList,new SortByName());
        for (File tempFile:fileList) {
            if (tempFile.isDirectory()) {
                String info = tempFile.getAbsolutePath()+"  文件夹大小："+ FormatFileSize(getFilesSize(tempFile.getAbsolutePath()))+"\n";
                try {
                    mOutputStreamWriter.write(info);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "storage error!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG,info);
                openFolder(tempFile.getAbsolutePath());
            }
            //这里做显示文件大小的，先注释，有需要再取消注释
//            else {
//                Log.i(TAG,tempFile.getAbsolutePath()+" 文件大小："+FormatFileSize(getFilesSize(tempFile.getAbsolutePath())));
//            }
        }
    }

    /**
     * 获取文件夹大小
     * @param originalFileName
     * @return
     */
    public long getFilesSize(String originalFileName) {
        long size=0;
        File file = new File(originalFileName);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File tempFile:files) {
                size = size + getFilesSize(tempFile.getAbsolutePath());
            }
        } else {
            size = size + file.length();
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private static String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    private static String FormatLongMillsToString(long currentTimeMills) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String sDateTime = sdf.format(currentTimeMills);  //得到精确到秒的表示：08/31/2006 21:08:00
        Log.i(TAG,sDateTime);
        return sDateTime;
    }


    private class SortByName implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            File file1 = o1;
            File file2 = o2;
            return file1.getName().compareToIgnoreCase(file2.getName());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWaitingDialog != null) {
            mWaitingDialog.dismiss();
            mWaitingDialog = null;
        }
    }
}
