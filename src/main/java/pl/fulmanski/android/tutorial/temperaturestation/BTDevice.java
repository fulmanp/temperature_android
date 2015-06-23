package pl.fulmanski.android.tutorial.temperaturestation;

import android.bluetooth.BluetoothDevice;

/**
 * Created by T540p on 2015-04-29.
 */
public class BTDevice {
    private String name;
    private String address;
    private BluetoothDevice device;

    public BTDevice(String name, String address, BluetoothDevice device){
        this.name = name;
        this.address = address;
        this.device = device;
    }

    public String toString(){
        return name + "\n" + address;
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    public String getName() {
        return name;
    }
}
