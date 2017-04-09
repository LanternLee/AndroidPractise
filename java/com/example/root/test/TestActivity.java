package com.example.root.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.test.R;

import java.io.FileDescriptor;

public class TestActivity extends AppCompatActivity {
    private TextView animText;

    private TextView output;

    private Button animStart;

    private Button animState;

    private Button animClear;

    private Animation rotateAnim;

    private Button TestFd;

    //线程间通信测试
    public static final int SAY_HELLO=0;

    private ServiceConnection connection;

    private Messenger messenger=new Messenger(new mHandler());

    class mHandler extends Handler{
        @Override
        public void handleMessage(Message message){
            switch(message.what){
                case SAY_HELLO:
                    Toast.makeText(TestActivity.this.getApplicationContext(),"Hello World Client",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        animText=(TextView)findViewById(R.id.AnimText);
        output=(TextView)findViewById(R.id.outputText);
        animStart=(Button)findViewById(R.id.AnimStart);
        animState=(Button)findViewById(R.id.AnimState);
        animClear=(Button)findViewById(R.id.AnimClear);
        TestFd=(Button)findViewById(R.id.FDTest);

        init();
    }

    private void init(){
        final Interpolator interpolator = new LinearInterpolator();
        rotateAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateAnim.setInterpolator(interpolator);
        rotateAnim.setDuration(400);
        rotateAnim.setFillAfter(false);

        animStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animText.clearAnimation();
                animText.startAnimation(rotateAnim);
                output.setText("animation running");
            }
        });

        animState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim=animText.getAnimation();
                if(anim!=null){
                    output.setText("animation running");
                }
                else{
                    output.setText("no animation");
                }
            }
        });

        TestFd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(TestActivity.this,FileService.class);
                ServiceConnection sc=new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        IMyAidlService mAS=IMyAidlService.Stub.asInterface(service);
                        try{
                            mAS.sayHello();
                            Toast.makeText(TestActivity.this,"5 + 6 ="+mAS.plus(5,6),Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
//                        FileService.MyBinder b=(FileService.MyBinder)service;
//                        FileDescriptor FD=b.getFD().getFileDescriptor();
//
//                        try {
//                            MemoryFile file = new MemoryFile("tmp", 1);
//                            file.close();
//                            MemoryFile.class.getDeclaredMethod("native_mmap").invoke()
//                        }
//                        catch (Exception e){
//                            e.printStackTrace();
//                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                };

                bindService(intent,sc,BIND_AUTO_CREATE);
            }
        });

        animClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animText.clearAnimation();
            }
        });

        connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Messenger sendMessenger=new Messenger(service);
                Message mess=new Message();
                mess.what=RemoteService.SAY_HELLO;
                mess.replyTo=messenger;
                try{
                    sendMessenger.send(mess);
                }
                catch(RemoteException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    @Override
    public void onStart(){
        super.onStart();
        Intent intent=new Intent(this,RemoteService.class);
        this.bindService(intent,connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop(){
        super.onStop();
        unbindService(connection);
    }
}
