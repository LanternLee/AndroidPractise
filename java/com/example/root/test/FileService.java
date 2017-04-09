package com.example.root.test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

public class FileService extends Service {
    private MemoryFile file=null;

    public FileService() {
        try{
            file=new MemoryFile("Ashmem",4);
            setValue(319);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setValue(int val){
        if(file==null){
            return;
        }

        byte[] buffer=new byte[4];
        buffer[0]=(byte)((val >>> 24)&0xFF);
        buffer[1]=(byte)((val >>> 16)&0xFF);
        buffer[2]=(byte)((val >>> 8)&0xFF);
        buffer[3]=(byte)(val&0xFF);

        try{
            file.writeBytes(buffer,0,0,4);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public class MyBinder extends Binder{
        public ParcelFileDescriptor getFD(){
            ParcelFileDescriptor fd=null;

            try{
                fd=(ParcelFileDescriptor) MemoryFile.class.getDeclaredMethod("getParcelFileDescriptor").invoke(file);
            }catch(Exception e){
                e.printStackTrace();
            }

            return fd;
        }
    }

    IMyAidlService.Stub mBinder=new IMyAidlService.Stub() {
        @Override
        public int plus(int a, int b) throws RemoteException {
            return a+b;
        }

        @Override
        public void sayHello() throws RemoteException {
            Log.d("FileService","say hello pid:"+ Process.myPid());
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
//        return new MyBinder();
    }
}
