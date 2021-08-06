package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Start {
    static Activity mactivity;
    static String path = "";
    static int gwidth;
    static int gheight;

    public static void Smain(Object contextoract) {

        Log.i("start_Smain","---------"+contextoract);
        Toast.makeText(mactivity,"执行Smain"+contextoract,Toast.LENGTH_LONG).show();
        if (contextoract instanceof Application) {
            return;
        }
        if (contextoract instanceof Activity) {
            mactivity = (Activity) contextoract;
        }

        if(contextoract instanceof XC_LoadPackage.LoadPackageParam){
            startHook((XC_LoadPackage.LoadPackageParam) contextoract);
        }
    }


    private static void showListDialog() {
        final String[] items = {"200x200", "300x300", "400x400", "500x500"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(mactivity);
        listDialog.setTitle("选择图片分辨率");
        listDialog.setCancelable(false);
        AlertDialog alertDialog=listDialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                // ...To-do
//                Toast.makeText(MainActivity.this,
//                        "你点击了" + items[which],
//                        Toast.LENGTH_SHORT).show();
                switch (which) {
                    case 0:
                        gwidth = 200;
                        gheight = 200;
                        break;
                    case 1:
                        gwidth = 300;
                        gheight = 300;
                        break;
                    case 2:
                        gwidth = 400;
                        gheight = 400;
                        break;
                    case 3:
                        gwidth = 500;
                        gheight = 500;
                        break;
                }
                dialog.dismiss();
            }
        });
        listDialog.show();
    }

   private static void startHook(XC_LoadPackage.LoadPackageParam loadPackageParam){
       XposedHelpers.findAndHookMethod("com.example.a.xphook.MainActivity", loadPackageParam.classLoader, "writeImage", String.class, new XC_MethodHook() {
           @Override
           protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
               path = (String) param.args[0];
               super.beforeHookedMethod(param);
           }
       });

       XposedHelpers.findAndHookMethod("com.example.a.xphook.MainActivity", loadPackageParam.classLoader, "toConformBitmap", Bitmap.class, new XC_MethodReplacement() {
           @RequiresApi(api = Build.VERSION_CODES.KITKAT)
           @Override
           protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

               showListDialog();

               if (!path.equals("")) {
                   Bitmap foreground = ImageScalingUtil.compressBitmapFromPath(path, gwidth, gheight);


                   foreground.getWidth();
                   foreground.getHeight();
                   Bitmap newbmp = Bitmap.createBitmap(gwidth+30, gheight+30, Bitmap.Config.ARGB_8888);
                   newbmp.eraseColor(Color.parseColor("#000000"));
                   Canvas cv = new Canvas(newbmp);
                   Matrix matrix = new Matrix();
                   matrix.setRotate((float) ((int) (Math.random() * 20.0d)), 100.0f, 100.0f);
                   matrix.postScale(280.0f / ((float) foreground.getWidth()), 340.0f / ((float) foreground.getHeight()));
                   matrix.postTranslate(100.0f, 50.0f);
                   cv.drawBitmap(foreground, matrix, null);
                   cv.save();
                   cv.restore();

               }

               return null;
           }
       });
   }
}
