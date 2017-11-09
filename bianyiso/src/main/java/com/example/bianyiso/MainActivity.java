package com.example.bianyiso;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 1.新建JniSo类 定义本地方法，加载so包
 * 2.创建jni目录  使用javah生成 .h文件  修改名字为jniutils.c 编写c代码
 * 3.添加Andorid.mk和Application.mk文件
 * 4.在grdle.properties文件中添加  android.useDeprecatedNdk=true
 * 5.在Module中的build.gradle中buildTypes下 添加
 *
 *   debug {
         ndk {
             moduleName "jniutils"
             abiFilters "armeabi", "armeabi-v7a", "x86"
         }
     }
 */
public class MainActivity extends AppCompatActivity {

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        JniSo jniSo = new JniSo();
        tv.setText(jniSo.addCstr("我是java字符串，"));
//        tv.setText(jniSo.getCstr());
    }
}
