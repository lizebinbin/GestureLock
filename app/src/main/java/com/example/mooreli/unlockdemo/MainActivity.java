package com.example.mooreli.unlockdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private LockView mLockView;
    private Button mBtShowPath;
    private boolean isShow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLockView = (LockView) findViewById(R.id.lockView);
        mBtShowPath = (Button) findViewById(R.id.btnShowPath);
        mLockView.setOnUnLockListener(new LockView.UnLockCallback() {
            @Override
            public void lockFinish(int length, String psw) {
                Toast.makeText(MainActivity.this, "length:" + length + "  psw:" + psw, Toast.LENGTH_SHORT).show();
                if("1236".equals(psw)){
                    mLockView.setIsMeasureTrue(true);
                }else{
                    mLockView.setIsMeasureTrue(false);
                }
            }
        });
        mBtShowPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow) {
                    isShow = false;
                    mBtShowPath.setText("显示路径");
                    mLockView.setIsShowPath(false);
                } else {
                    isShow = true;
                    mBtShowPath.setText("隐藏路径");
                    mLockView.setIsShowPath(true);
                }
            }
        });
    }
}
