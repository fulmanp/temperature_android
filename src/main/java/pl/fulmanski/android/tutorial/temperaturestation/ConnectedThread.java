package pl.fulmanski.android.tutorial.temperaturestation;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by T540p on 2015-04-29.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket socket;
    private final InputStream streamIn;
    private final OutputStream streamOut;
    private Handler messageHandler;
    private boolean cancel = false;

    public ConnectedThread(BluetoothSocket socket, Handler messageHandler) {
        this.socket = socket;
        this.messageHandler = messageHandler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            messageHandler.obtainMessage(10).sendToTarget();
        }

        streamIn = tmpIn;
        streamOut = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int begin = 0;
        int bytes = 0;

        byte[] msg = "1".getBytes();
        boolean send = false;
        //byte[] b = string.getBytes(Charset.forName("UTF-8"));

        if(streamIn == null || streamOut == null){
            messageHandler.obtainMessage(Thermometer.NO_STREAM).sendToTarget();
            return;
        }

        write(msg);
//        try {
//            messageHandler.obtainMessage(7).sendToTarget();
//            Thread.sleep(1500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        int counterOpen = 0;
        int counterClose = 0;
        while (!cancel) {
            try {
                if(0==streamIn.available())
                {
                    messageHandler.obtainMessage(Thermometer.THREAD_SLEEPING).sendToTarget();
                    Thread.sleep(1000);
                    //write(msg);
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messageHandler.obtainMessage(Thermometer.MESSAGE_WORKING).sendToTarget();
            try {
                bytes += streamIn.read(buffer, bytes, buffer.length - bytes);
                messageHandler.obtainMessage(Thermometer.READ).sendToTarget();
                for(int i = begin; i < bytes; i++) {
//                    if(buffer[i] == "#".getBytes()[0]) {
                    if(buffer[i] == "{".getBytes()[0]) { counterOpen++; }
                    else if(buffer[i] == "}".getBytes()[0]) { counterClose++; }

                    if(counterOpen == counterClose) {
                        messageHandler.obtainMessage(Thermometer.MESSAGE_TEMPERATURE, begin, i+1, buffer).sendToTarget();
                        send = true;
                        begin = i + 1;
                        if(i == bytes - 1) {
                            bytes = 0;
                            begin = 0;
                        }
                    }
                }
            } catch (IOException e) {
                break;
            }

            if (send)
                break;
        }

        try {
            //streamOut.flush();
            streamIn.close();
            streamOut.close();
            socket.close();
        } catch (IOException e) { }
    }

    public void write(byte[] bytes) {
        try {
            streamOut.write(bytes);
            streamOut.flush();
        } catch (IOException e) { }
    }

    public void cancel() {
        cancel = true;
    }
}