package com.example.a.xphook;

import android.app.Application;

import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class myapp extends Application {
    public static DexClassLoader classLoader= null;
    static {
        System.loadLibrary("inject-lib");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        start(this);
    }
    public static void start(Object object){
        if(classLoader!=null){
            try {
                Class<?> Start = classLoader.loadClass("com.example.myapplication.Start");
                Method method = Start.getDeclaredMethod("Inject",Object.class);
                method.setAccessible(true);
                method.invoke(null,object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
public native void getdex();
}
