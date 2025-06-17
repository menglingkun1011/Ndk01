#include <jni.h>
#include <string>

//com_example_mirsmeng_ndk01
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_mirsmeng_ndk01_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}