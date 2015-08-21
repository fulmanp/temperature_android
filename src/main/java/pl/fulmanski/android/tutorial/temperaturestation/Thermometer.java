package pl.fulmanski.android.tutorial.temperaturestation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by T540p on 2015-05-21.
 */
public class Thermometer {
    static final int MESSAGE_TEMPERATURE = 1;
    static final int MESSAGE_WORKING = 2;
    static final int THREAD_SLEEPING = 3;
    static final int NO_STREAM = 4;
    static final int READ = 5;

    Context appContext;
    BTConnectThread bTConnectThread;
    BluetoothDevice selectedDevice;
    private NetworkManager networkManager = null;


//    Thermometer(Context appContext){
//        this.appContext = appContext;
//        networkManager = new NetworkManager();
//    }

    Thermometer(Context appContext, BluetoothDevice selectedDevice){
        this.appContext = appContext;
        this.selectedDevice = selectedDevice;
        networkManager = new NetworkManager();
    }

    public void setSelectedDevice(BluetoothDevice selectedDevice){
        this.selectedDevice = selectedDevice;
    }



    /* The code below

    Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    makes the following warning:

    This Handler class should be static or leaks might occur

    so we have to replace it by following code */

    // === FIX BEGIN ===

    // For some explanation why do this as followe, see:
    // http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
    // http://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
    /**
     * Instances of static inner classes do not hold an implicit
     * reference to their outer class.
     */
    private static class MyHandler extends Handler {
        private final WeakReference<Thermometer> mActivity;

        public MyHandler(Thermometer activity) {
            mActivity = new WeakReference<Thermometer>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Thermometer activity = mActivity.get();
            if (activity != null) {
                byte[] writeBuf = (byte[]) msg.obj;
                int begin = msg.arg1;
                int end = msg.arg2;
                String temperatureInfoTimestamp;

                switch(msg.what) {
                    case READ:
                        Toast.makeText(activity.appContext, "Have just read some data", Toast.LENGTH_SHORT).show();
                        break;
                    case NO_STREAM:
                        Toast.makeText(activity.appContext, "Something wrong, no stream!", Toast.LENGTH_SHORT).show();
                        break;
                    case THREAD_SLEEPING:
                        Toast.makeText(activity.appContext, "Thread sleeping", Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_WORKING:
                        Toast.makeText(activity.appContext, "Please wait...", Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_TEMPERATURE:
                        String temperatureInfo = new String(writeBuf);
                        temperatureInfo = temperatureInfo.substring(begin, end);

                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        temperatureInfoTimestamp = df.format(Calendar.getInstance().getTime());

                        Toast.makeText(activity.appContext, temperatureInfo, Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject jsonRootObject = new JSONObject(temperatureInfo);
                            int items = jsonRootObject.optInt("items"); // Returns the value mapped by name if it exists
                            // and is an int or can be coerced to an int,
                            // or 0 otherwise.
                            for(int i=0; i < items; i++){
                                JSONObject jsonObject = jsonRootObject.getJSONObject(Integer.toString(i+1));
                                String type = jsonObject.getString("type");
                                if (type.equals("ds18b20")){
                                    String rom = jsonObject.getString("rom");
                                    double temp = jsonObject.getDouble("temp");

                                    Toast.makeText(activity.appContext, i +":\nROM: "+rom+"\ntemperature: "+temp+"\nTime: "+temperatureInfoTimestamp, Toast.LENGTH_SHORT).show();
                                    activity.sendData("ds18b20_temp", rom, Double.toString(temp), temperatureInfoTimestamp.replaceAll(" ", "%20"));
                                } else if (type.equals("dht")){
                                    int number = jsonObject.getInt("number");
                                    double temp = jsonObject.getDouble("temp");
                                    double hum = jsonObject.getDouble("hum");

                                    activity.sendData("dht_temp", Integer.toString(number), Double.toString(temp), temperatureInfoTimestamp.replaceAll(" ", "%20"));
                                    Toast.makeText(activity.appContext, i +":\nnumber: "+number+"\ntemperature: "+temp+"\nTime: "+temperatureInfoTimestamp, Toast.LENGTH_SHORT).show();
                                    activity.sendData("dht_hum", Integer.toString(number), Double.toString(hum), temperatureInfoTimestamp.replaceAll(" ", "%20"));
                                    Toast.makeText(activity.appContext, i +":\nnumber: "+number+"\nhumidity: "+hum+"\nTime: "+temperatureInfoTimestamp, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {e.printStackTrace();}
                        break;
                    default:
                        Toast.makeText(activity.appContext, "Default action" + '\n' + "Something wrong?" + '\n' + msg.what, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private final MyHandler messageHandler = new MyHandler(this);

    // === FIX END ===

    public void temperatureRead(){
        if (selectedDevice != null) {
            if (bTConnectThread != null && bTConnectThread.isAlive()) {
                Toast.makeText(appContext, "Cancel existing connection with\n"+selectedDevice.toString(),
                        Toast.LENGTH_LONG).show();
                bTConnectThread.cancel();
            }
            Toast.makeText(appContext, "Connect with device\n"+selectedDevice.toString(),
                    Toast.LENGTH_LONG).show();
            bTConnectThread = new BTConnectThread(selectedDevice);
            bTConnectThread.start();
        } else {
            Toast.makeText(appContext, "Please select a device",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void sendData(String description, String name, String value, String temperatureInfoTimestamp){
        String url = "http://fulmanski.pl/php/temperature/?login=fulmanp&timestamp="+temperatureInfoTimestamp+"&name="+description+"_"+name+"&type=float&value="+value;
        //Log.e("t",url);
        try {
            networkManager.sendGET(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class BTConnectThread extends Thread {
        private final BluetoothSocket socket;
        ConnectedThread connectedThread = null;
        // Default UUID
        private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public BTConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            //ParcelUuid[] uuids = device.getUuids();
            //try {
            //    tmp = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            //} catch (IOException e) { }

            // Get a BluetoothSocket to connect with the given BluetoothDevice. This code below
            // show how to do it and handle the case that the UUID from the device is not found
            // and trying a default UUID.
            // See:
            // http://stackoverflow.com/questions/21457175/android-bluetooth-uuid-connecting-app-to-android

            //try {
            // Use the UUID of the device that discovered // TODO Maybe need extra device object
            //    if (device != null)
            //    {
            //        tmp = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            //    }
            // }
            //catch (NullPointerException e)
            //{
            try {
                tmp = device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
            } catch (IOException e1) {
                Toast.makeText(appContext, "Problems while creating socket", Toast.LENGTH_SHORT).show();
            }
            //}
            //catch (IOException e) { }

            socket = tmp;
        }

        public void run() {
            // Make sure that bluetooth is not in discovery mode.
            // In other case turn off this mode.
            // bluetoothAdapter.cancelDiscovery();

            if (socket != null) {
                try {
                    socket.connect();
                } catch (IOException connectException) {
                    try {
                        socket.close();
                    } catch (IOException closeException) {
                    }
                    return;
                }
            }

            connectedThread = new ConnectedThread(socket, messageHandler);
            connectedThread.start();
        }

        public void cancel() {
            //try {
            connectedThread.cancel();
            //socket.close();
            // catch (IOException e) { }
        }
    }
}
