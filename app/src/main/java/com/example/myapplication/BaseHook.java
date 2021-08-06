package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public abstract class BaseHook implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private static final String TAG = "BaseHook";

    private static  String package_name ;
    private Context vcontext = null;

    private Activity activity = null;

    private boolean isfrist = true;
    private XC_LoadPackage.LoadPackageParam lp;
    ClassLoader cl;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) throws Throwable {

        if(getpackage().equals("")){
            throw new Exception("未初始化包名!");
        }else {
            package_name=getpackage();
        }

        if (!package_name.equals(lp.packageName)) {
            return;
        }
        Log.i(TAG, "handleLoadPackage:" + lp.packageName);
        hookOriginNewCall(lp);

    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }

    private void hookOriginNewCall(final XC_LoadPackage.LoadPackageParam lps) {

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                if(isfrist){
                    isfrist=false;
                    cl = ((Context) param.args[0]).getClassLoader();
                    vcontext=(Context) param.args[0];
                    initActivity();
                    lp=lps;
                    starthook();
                }else {
                    super.afterHookedMethod(param);
                }
            }

        });
    }
    public abstract String getpackage();

    public abstract void starthook() throws ClassNotFoundException;

    //初始化当前activity
    public void initActivity() throws ClassNotFoundException {
        Class<?> aClass = cl.loadClass("android.app.Activity");
        findAndHookMethod(aClass, "onCreate", Bundle.class, new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Class s = param.thisObject.getClass();

                if( s.toString().contains("Fragment")){

                    Method method = s.getMethod("getActivity");
                    activity = (Activity) method.invoke(param.thisObject);
                }else {
                    activity=(Activity)param.thisObject;
                }
                writelog(new SimpleDateFormat("yyyyMMdd").format(new Date())+"----"+"初始化："+getpackage()+"\n");

            }


        });

    }

    public Context getcontext() {
        return vcontext;
    }

    public Activity getActivity() {
        return activity;
    }

    public ClassLoader getclassloader() {
        return cl;
    }
//写入日志
    public void writelog(String log){

        if (isVersionM()) {
            checkAndRequestPermissions();
        }

        String filepath1= Environment.getExternalStorageDirectory().toString();
        File file=new File(filepath1,"hooklog.txt");

        try {
            if(!file.exists()){
                file.createNewFile();
            }
            //追加
            FileOutputStream fout = new FileOutputStream(file,true);
            byte[] bytes = new String((log).getBytes(),"UTF-8").getBytes();
            fout.write(bytes);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> mMissPermissions = new ArrayList<>();
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private boolean isVersionM() {
        return Build.VERSION.SDK_INT >= 23;
    }

    private void checkAndRequestPermissions() {
        mMissPermissions.clear();
        for (String permission : REQUIRED_PERMISSION_LIST) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                mMissPermissions.add(permission);
            }
        }
        if (mMissPermissions.size() > 0) {
            ActivityCompat.requestPermissions(activity, mMissPermissions.toArray(new String[mMissPermissions.size()]), 123);
        }
    }

    public XC_LoadPackage.LoadPackageParam getPackageParm(){
        return lp;
    }
/**
 * Method []ms = c.getDeclaredMethods()---获取本类中的所有的方法
 * Method []ms = c.getMethods();--------获取本类以及父类接口中所有公共的方法（public）
 * getModifiers()---------返回int类型值表示该字段的修饰符，即这个方法就是返回一个int型的返回值，代表类、成员变量、方法的修饰符。
 * m.getParameterTypes()------按照声明顺序返回 Type 对象的数组，这些对象描述了此 Method 对象所表示的方法的形参类型的
 * /*for(Method m:ms) {
 * 		//修饰符
 * 		System.out.println(Modifier.toString(m.getModifiers()));
 * 		//方法的返回值类型
 * 		Class type = m.getReturnType();
 * 		System.out.println(type);
 * 		//获取方法的名
 * 		System.out.println(m.getName());
 * 		//形参
 * 		Class [] parameter = m.getParameterTypes();
 * 		for(Class C:parameter) {
 * 			System.out.println(C.getSimpleName());//得到类简写名称
 *        }
 *    }
 * */
    public String[] getMethos(Class<?> classes){

       Method []methods=classes.getDeclaredMethods();
       // StringBuffer sb  = new StringBuffer();
        String [] sb=new String[methods.length];
        for (int i = 0; i < methods.length; i++) {
            sb[i]=methods[i].getName();
        }
      return sb;
    }
//适用于同时hook一个类的多个函数
    public void fasthook(Class<?> classname, @Nullable String[] methods,watchcallback watchcallback){
        if(methods!=null){
            for(String method:methods){
                XposedBridge.hookAllMethods(classname, method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        //返回false修改
                        if(!watchcallback.beforehook(param.method.getName(),param)){
                            super.beforeHookedMethod(param);
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        if(!watchcallback.afterhook(param.method.getName(),param)){
                            super.afterHookedMethod(param);
                        }
                    }
                });
            }
        }else {
           String Str[]= getMethos(classname);
           for(String i: Str){
               XposedBridge.hookAllMethods(classname, i, new XC_MethodHook() {
                   @Override
                   protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                   }
               });

           }
        }

    }

    public interface watchcallback{
        boolean beforehook(String methodname,XC_MethodHook.MethodHookParam param);
        boolean afterhook(String methodname,XC_MethodHook.MethodHookParam param);
    }

    public abstract class nXC_MethodHook extends XC_MethodHook{

        public nXC_MethodHook(int priority) {
            super(priority);
        }

        public nXC_MethodHook() {
            super();
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

            super.beforeHookedMethod(param);
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
        }
    }

}
