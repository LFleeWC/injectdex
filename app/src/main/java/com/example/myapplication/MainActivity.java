package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.a.xphook.R;

import dalvik.system.DexClassLoader;


public class MainActivity extends Activity {
static {
    System.loadLibrary("inject-lib");
}
public static DexClassLoader classLoader=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        Button button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getdex2();
            }
        });
    }
    public native void getdex2();
}