package com.example.diaoyongso;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.bianyiso.JniSo;
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
public class MainActivity extends AppCompatActivity {

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        JniSo jniSo = new JniSo();
        tv.setText(jniSo.addCstr("java字符串，"));
    }
}
