package com.example.mirsmeng.ndk01;

public class JniUtils {

    static {
//        System.loadLibrary("native-lib");
        System.loadLibrary("TestSo");
    }

    public native String stringFromJNI();

    public native void intArrFromJNI(int[] arr);

}
