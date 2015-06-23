package pl.fulmanski.android.tutorial.temperaturestation;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * Created by T540p on 2015-05-30.
 */
public class MyApplication extends Application {
    private static MyApplication singleton;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice selectedDevice;

    public synchronized static MyApplication getInstance(){
        if (null == singleton) {
            singleton = new MyApplication();
        }
        return singleton;
    }

    public synchronized void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public synchronized BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public synchronized void setSelectedDevice(BluetoothDevice selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    public synchronized BluetoothDevice getSelectedDevice() {
        return selectedDevice;
    }
}
