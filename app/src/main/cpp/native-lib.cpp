#include <jni.h>
#include <string>

//future features :)
extern "C" JNIEXPORT jstring JNICALL
Java_com_wallme_wallpaper_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
