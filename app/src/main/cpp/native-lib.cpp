#include <jni.h>
#include <string>
#include <android/log.h>
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "SharkChilli", __VA_ARGS__))

extern jbyte blob[];
extern int blob_size;
jobject getGlobalContext(JNIEnv *env,jobject sthis);

static JavaVM *gJavaVM;
void injectdex(JNIEnv *genv,jobject sthis);


extern "C" JNIEXPORT void JNICALL Java_com_example_a_xphook_myapp_getdex( JNIEnv *genv, jobject sthis ) {
    injectdex(genv,sthis);
}

extern "C" JNIEXPORT void JNICALL Java_com_example_myapplication_MainActivity_getdex2(JNIEnv *env, __attribute__((__unused__)) jobject This)
{
//    jbyteArray ret = env -> NewByteArray(blob_size);
//    env -> SetByteArrayRegion(ret, 0, blob_size, blob);
//
//    return  ret;
    injectdex(env,This);
}

//JNIEnv 不能做全局函数
void injectdex(JNIEnv *genv,jobject sthis){
    jbyteArray ret = genv -> NewByteArray(blob_size);
    genv -> SetByteArrayRegion(ret, 0, blob_size, blob);

    jobject mcontext = getGlobalContext(genv,sthis);
    jmethodID getDir = genv ->GetMethodID(genv->FindClass("android/content/ContextWrapper"),"getDir",
                                          "(Ljava/lang/String;I)Ljava/io/File;");

     jobject optfile = genv -> CallObjectMethod(mcontext,getDir,genv->NewStringUTF("opt_dex"),0);
//    jobject libfile = genv -> CallObjectMethod(mcontext,getDir,genv->NewStringUTF("lib_path"),0);


    jclass File =genv -> FindClass("java/io/File");
    jmethodID file=genv ->GetMethodID(File,"<init>","(Ljava/io/File;Ljava/lang/String;)V");
    //生成对象
    jobject cfile = genv ->NewObject(genv -> FindClass("java/io/File"),file,optfile,genv->NewStringUTF("mydex.dex"));

    jmethodID exists =  genv ->GetMethodID(genv -> FindClass("java/io/File"),"exists", "()Z");

    if(!genv->CallBooleanMethod(cfile,exists)){

        genv ->CallBooleanMethod(cfile,genv->GetMethodID(File,"createNewFile", "()Z"));

        jmethodID  fileOutputStream =  genv ->GetMethodID(genv ->FindClass("java/io/FileOutputStream"),"<init>","(Ljava/io/File;)V");
        jobject outputStream = genv -> NewObject( genv ->FindClass("java/io/FileOutputStream"),fileOutputStream,cfile);
        genv ->CallVoidMethod(outputStream,genv->GetMethodID( genv->FindClass("java/io/FileOutputStream"),"write","([B)V"),ret);


        genv ->CallVoidMethod(outputStream,genv->GetMethodID( genv->FindClass("java/io/FileOutputStream"),"flush","()V"));
        genv ->CallVoidMethod(outputStream,genv->GetMethodID( genv->FindClass("java/io/FileOutputStream"),"close","()V"));


    }


    //jmethodID  PathClassLoader = genv ->GetMethodID(genv ->FindClass("dalvik/system/PathClassLoader"),"<init>","(Ljava/lang/String;Ljava/lang/ClassLoader;)V");
    jmethodID  dexClassLoader = genv ->GetMethodID(genv ->FindClass("dalvik/system/PathClassLoader"),"<init>",
                                                   "(Ljava/lang/String;Ljava/lang/ClassLoader;)V");

    jobject currentcl = genv ->CallObjectMethod(mcontext,genv->GetMethodID(genv->FindClass("android/content/Context"),"getClassLoader","()Ljava/lang/ClassLoader;"));

    jobject pclassloader = genv -> NewObject(genv ->FindClass("dalvik/system/PathClassLoader"),
                                             dexClassLoader,genv->CallObjectMethod(cfile,genv->GetMethodID(genv -> FindClass("java/io/File"),"getPath", "()Ljava/lang/String;")),
                                             currentcl);
//  genv->CallObjectMethod(optfile,genv->GetMethodID(genv -> FindClass("java/io/File"),"getAbsolutePath","()Ljava/lang/String;")),
//  genv->CallObjectMethod(libfile,genv->GetMethodID(genv -> FindClass("java/io/File"),"getAbsolutePath","()Ljava/lang/String;")),
/*
    jmethodID plclass = genv->GetMethodID(genv->FindClass("java/lang/Object"),"getClass","()Ljava/lang/Class;");
 //   jobject Stringclass =genv->NewObject(genv->FindClass("java/lang/String"),genv->GetMethodID(genv->FindClass("java/lang/String"),"<init>", "(Ljava/lang/String;)V"),genv->NewStringUTF("123"));
//    jobject Stringclass1 =genv->CallObjectMethod(Stringclass,plclass);
//    jobject ss={Stringclass1};
    jobject classs = genv->CallObjectMethod(pclassloader,plclass);
     genv ->CallObjectMethod(classs,genv->GetMethodID(genv->FindClass("java/lang/Class"),"getDeclaredMethod","(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"),
                                               genv->NewStringUTF("loadClass"),genv->FindClass("java/lang/Class"));
*/
/*
    jmethodID cmethid =genv->GetMethodID(genv->FindClass("java/lang/ClassLoader"),"loadClass","(Ljava/lang/String;)Ljava/lang/Class;");


    genv ->CallNonvirtualObjectMethod(pclassloader,genv->FindClass("java/lang/ClassLoader"),cmethid,genv->NewStringUTF("com.huruwo.hposed.Gsons"));  //调用父类的方法
*/



    jfieldID  jfieldId=genv->GetStaticFieldID(  genv->GetObjectClass(sthis),"classLoader","Ldalvik/system/DexClassLoader;");
    genv->SetStaticObjectField(genv->GetObjectClass(sthis),jfieldId,pclassloader);
}

jobject getGlobalContext(JNIEnv *env,jobject sthis)
{
    //获取Activity Thread的实例对象
    jclass activityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(activityThread, "currentActivityThread", "()Landroid/app/ActivityThread;");
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);
    //获取Application，也就是全局的Context
    jmethodID getApplication = env->GetMethodID(activityThread, "getApplication", "()Landroid/app/Application;");
//    jobject application =
     env->CallObjectMethod(at, getApplication);

   jobject context = env -> CallObjectMethod(sthis,env->GetMethodID(env->FindClass("android/app/Application"),"getApplicationContext","()Landroid/content/Context;"));
    return context;
}



//当动态库被加载时这个函数被系统调用
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
//    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK)
//    {
//        return -1;
//    }
    if (vm->AttachCurrentThread(&env, NULL) != JNI_OK){
        return -1;
    }
    gJavaVM =vm;
    //injectdex(env);

    return JNI_VERSION_1_4;
}

//当动态库被卸载时这个函数被系统调用
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
    LOGI("JNI_OnUnload");
}
//加载so时执行so方法 hook
extern "C" void _init(void) {

}