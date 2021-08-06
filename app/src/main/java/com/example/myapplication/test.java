package com.example.myapplication;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class test extends BaseHook {
    @Override
    public String getpackage() {
        return "com.example.a.xphook";
    }

    @Override
    public void starthook() throws ClassNotFoundException {
        XposedHelpers.findAndHookMethod("com.example.a.xphook.myapp", getclassloader(), "start", Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }
}
