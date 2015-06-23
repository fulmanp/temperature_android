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

    Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = msg.arg1;
            int end = msg.arg2;
            String temperatureInfoTimestamp = null;

            switch(msg.what) {
                case READ:
                    Toast.makeText(appContext, "Have just read some data", Toast.LENGTH_SHORT).show();
                    break;
                case NO_STREAM:
                    Toast.makeText(appContext, "Something wrong, no stream!", Toast.LENGTH_SHORT).show();
                    break;
                case THREAD_SLEEPING:
                    Toast.makeText(appContext, "Thread sleeping", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_WORKING:
                    Toast.makeText(appContext, "Please wait...", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TEMPERATURE:
                    String temperatureInfo = new String(writeBuf);
                    temperatureInfo = temperatureInfo.substring(begin, end);

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    temperatureInfoTimestamp = df.format(Calendar.getInstance().getTime());
                    //System.out.println(java.net.URLEncoder.encode("Hello World", "UTF-8").replaceAll("\\+", "%20"));


//                    Context context = getApplicationContext();
//                    Toast.makeText(appContext, temperatureInfoTimestamp + '\n' + temperatureInfo, Toast.LENGTH_SHORT).show();

                    // temperatureInfo is a string of the form:
                    // BEGINROM28a205cc050000c023.25ROM281a28230600006523.38END
                    // BEGINTEXTxxxxxDATAyyyyITEMTEXTxxxxxDATAyyyyITEMEND

                    Toast.makeText(appContext, temperatureInfo, Toast.LENGTH_SHORT).show();

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

                                Toast.makeText(appContext, i +":\nROM: "+rom+"\ntemperature: "+temp+"\nTime: "+temperatureInfoTimestamp, Toast.LENGTH_SHORT).show();
                                sendData("ds18b20_temp", rom, Double.toString(temp), temperatureInfoTimestamp.replaceAll(" ", "%20"));
                            } else if (type.equals("dht")){
                                int number = jsonObject.getInt("number");
                                double temp = jsonObject.getDouble("temp");
                                double hum = jsonObject.getDouble("hum");

                                sendData("dht_temp", Integer.toString(number), Double.toString(temp), temperatureInfoTimestamp.replaceAll(" ", "%20"));
                                Toast.makeText(appContext, i +":\nnumber: "+number+"\ntemperature: "+temp+"\nTime: "+temperatureInfoTimestamp, Toast.LENGTH_SHORT).show();
                                sendData("dht_hum", Integer.toString(number), Double.toString(hum), temperatureInfoTimestamp.replaceAll(" ", "%20"));
                                Toast.makeText(appContext, i +":\nnumber: "+number+"\nhumidity: "+hum+"\nTime: "+temperatureInfoTimestamp, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {e.printStackTrace();}

//                    int indexB = temperatureInfo.indexOf("BEGIN");
//                    int indexE = temperatureInfo.indexOf("END");
//                    if (indexB != -1 && indexE != -1) {
//                        temperatureInfo = temperatureInfo.replace("BEGIN", "");
//                        temperatureInfo = temperatureInfo.replace("END", "");
//                        String[] ti = temperatureInfo.split("ROM");
//                        String rom, temp;
//                        for (int i = 0; i < ti.length; i++) {
//                            if (ti[i].length() > 16){
//                                rom = ti[i].substring(0,16);
//                                temp = ti[i].substring(16, ti[i].length());
//                                Toast.makeText(appContext, i +":\nROM: "+rom+"\ntemperature: "+temp+"\nTime: "+temperatureInfoTimestamp, Toast.LENGTH_SHORT).show();
//                                sendData(rom, temp, temperatureInfoTimestamp.replaceAll(" ", "%20"));
//                            }
//                        }
//                    } else
//                        Toast.makeText(appContext, "Upsss...", Toast.LENGTH_SHORT).show();

                    break;
                default:
                    Toast.makeText(appContext, "Default action" + '\n' + "Something wrong?" + '\n' + msg.what, Toast.LENGTH_SHORT).show();
            }
        }
    };


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
