package com.example.jelly.nfcusual.common;

import android.app.Application;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.example.jelly.nfcusual.R;

import java.io.File;
import java.util.Date;

import darks.log.appender.impl.FileDateSizeAppender;
import darks.log.layout.PatternLayout;

/**
 * Created by jelly on 2017/11/30.
 * 管理app生命周期
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //设置日志application对象并注册ANR错误处理器
        darks.log.Logger.Android.setApplication(this);
        darks.log.Logger.Android.registerCrashHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                new Thread()
                {
                    public void run()
                    {
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), getString(R.string.anr), Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }.start();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        setLogger();

    }

    public void setLogger(){
        File file = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File externalFilesDir = getApplicationContext().getExternalFilesDir(null);
            String path= (externalFilesDir != null ? externalFilesDir.getAbsolutePath() : null) +"/Log";
            file = new File(path);
            if (!file.exists()){
                file.mkdirs();
            }
        }
        if (file != null){
            System.out.println("Log file create succ");
            darks.log.Logger logger = darks.log.Logger.getRootLogger();
            FileDateSizeAppender fileDateSizeAppender = new FileDateSizeAppender();
            fileDateSizeAppender.setName("FILE");
            fileDateSizeAppender.setLayout(new PatternLayout());
            fileDateSizeAppender.getLayout().setPattern("%d{yyyy-MM-dd HH:mm:ss} [%f][%p] - %m%n");
            fileDateSizeAppender.setFileName(file.getAbsolutePath()
                    + "/log_"+(new Date().toString())+".txt");
            fileDateSizeAppender.setBuffered(false);
            fileDateSizeAppender.setMaxSize(10485760);
            fileDateSizeAppender.setKeepDay(7);
            fileDateSizeAppender.setAsync(true);
            logger.addAppender(fileDateSizeAppender);
        }else {
            System.out.println("create log file failure");
        }

    }
}
