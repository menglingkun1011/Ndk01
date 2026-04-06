package com.example.mirsmeng.ndk01;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;


/**
 * 简化步骤
 * 1.创建本地方法，并在调用处进行调用
 * 2.创建jni目录，新建c/cpp文件，编写c代码
 * 3.添加Android.mk和Application.mk文件
 * 4.添加静态代码块，引入so包
 * 5.在gradle.properties中添加android.useDeprecatedNdk=true
 * 6.在buildTypes下添加cpu架构支持
 *
 *      debug {
        ndk {

            abiFilters "armeabi", "armeabi-v7a", "x86"
        }
 }
 *
 * ndk开发步骤：
 * 1.新建本地方法： public native String getStrAdd(String str,int len); 添加静态代码块引入System.loadLibrary("hello");
 * 2.选中app  右键NEW-》FOLDER--》JNI FOLDER  生出cpp目录，在cpp目录下新建hello.c文件
 * 3.在hello.c引入包 #include<stdio.h>  #include<stdlib.h>   #include<cpp.h>
 * 4.打开这个目录C:\Users\MirsMeng\Desktop\Ndk01\app\src\main\java   右键在此处打开命令行窗口，输入javah com.example.mirsmeng.ndk01.MainActivity
 * 生成.h文件，用notepad打开  复制方法名（JNIEXPORT jstring JNICALL Java_com_example_mirsmeng_ndk01_MainActivity_getStrAdd(JNIEnv *, jobject, jstring, jint)）
 * 到hello.c文件中，编写c代码
 * 5.添加Android.mk和多平台支持Application.mk
 *
 * Android.mk文件内容
 *
 *  LOCAL_PATH := $(call my-dir)
    include $(CLEAR_VARS)
    LOCAL_MODULE := hello //so包名字
    LOCAL_SRC_FILES := hello.c //c文件名字
    include $(BUILD_SHARED_LIBRARY)

    Application.mk文件内容

    APP_ABI := all
 *
 *
 * 快速生成头文件的方法 http://blog.csdn.net/fangyoayu2013/article/details/51094061
 */
public class MainActivity extends AppCompatActivity {

    private Button btn_start,btn_stop;
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_RECORD_AUDIO = 100;
    private MediaRecorder recorder;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_start.setOnClickListener(v -> {
            Log.i(TAG, "onClick: btn_start");
            startAudioLoop();
        });
        btn_stop.setOnClickListener(v -> {
            Log.i(TAG, "onClick: btn_stop");
//            stopAudioLoop();
        });
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }

        findViewById(R.id.btn_record_start2).setOnClickListener(v -> {

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            Log.i(TAG, "onCreate: path = "+getExternalFilesDir(null));
            recorder.setOutputFile(getExternalFilesDir(null) + "/audiorecord.3gp");
            try {
                recorder.prepare();
                recorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.btn_record_stop2).setOnClickListener(v -> {
            try {
                // 假设在某个时间点停止录制
                recorder.stop();
                recorder.release();
                recorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.btn_player_start2).setOnClickListener(v -> {
            try {
                player = new MediaPlayer();
                player.setDataSource(getExternalFilesDir(null) + "/audiorecord.3gp");
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.btn_player_stop2).setOnClickListener(v -> {
            try {
                // 假设在某个时间点停止播放
                player.stop();
                player.release();
                player = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "录音权限已授权", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "录音权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     *
     * 如何使用编译好的so包呢？
     * 1.把编译好的几种cpu架构的so包放到libs目录下
     * 2.在app目录下的  build.gradle中的 android里面添加以下代码
     *      sourceSets {
                    main {
                            jniLibs.srcDirs = ['libs']
                         }
            }
     *3.把定义本地方法的类在需要调用的地方new出来  在调用本地方法。
     */

    static {
        System.loadLibrary("native-lib");
    }

    public native int startAudioLoop();
//    public native void stopAudioLoop();
}
