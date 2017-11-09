package com.example.bianyiso;

/**
 * Created by MirsMeng on 2017/11/9.
 * 1.定义本地方法
 * 2.加载so包
 */

public class JniSo {

    public native String getCstr();

    public native int getCsum(int a,int b);

    public native String addCstr(String str);

    static{
        System.loadLibrary("jniutils");
    }

}
