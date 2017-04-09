package com.example.root.test;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class RemoteService extends Service {
    static public final int SAY_HELLO=0;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){
            //准备返回的信息
            if(message.replyTo!=null){
                Message toClient=this.obtainMessage();
                toClient.what=0;
                try{
                    ((Messenger)message.replyTo).send(toClient);
                }
                catch(RemoteException e){
                    e.printStackTrace();
                }
            }

            //接收发送来的信息
            switch(message.what){
                case SAY_HELLO:
                    Toast.makeText(RemoteService.this.getApplicationContext(),"Hello World Remote Service",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    };

     Messenger messenger=new Messenger(handler);

    public RemoteService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return messenger.getBinder();
    }
}
