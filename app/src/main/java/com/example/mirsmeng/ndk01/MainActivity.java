package com.example.mirsmeng.ndk01;

import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


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

    private TextView tv;
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        tv.setText(stringFromJNI());

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"ddd333");
                JniUtils jniUtils = new JniUtils();
                String ss = jniUtils.stringFromJNI();
                Log.i(TAG,"ss:"+ss);
                tv.setText(ss);
                int[] arr = new int[10];
                jniUtils.intArrFromJNI(arr);
                Log.i(TAG,"arr len: "+arr.length);
                for (int i = 0; i < arr.length; i++) {
                    Log.i(TAG,"arr["+i+"]: "+arr[i]);
                }
            }
        });

    }

    //获取c中的字符串
    public native String stringFromJNI();

    static {
        System.loadLibrary("native-lib");
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
}
