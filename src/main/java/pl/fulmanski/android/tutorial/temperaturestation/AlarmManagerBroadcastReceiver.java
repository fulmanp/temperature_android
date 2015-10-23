package pl.fulmanski.android.tutorial.temperaturestation;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

/**
 * Created by T540p on 2015-05-21.
 */
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        // If you hold a partial wake lock, the CPU will continue to run,
        // regardless of any display timeouts or the state of the screen and even after the user
        // presses the power button. In all other wake locks, the CPU will run, but the user can
        // still put the device to sleep using the power button.
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Thermometer power lock");
        //Acquire the lock
        wl.acquire();

        MyApplication myApplication = MyApplication.getInstance();
        BluetoothDevice bluetoothDevice = myApplication.getSelectedDevice();
        Thermometer thermometer = new Thermometer(context, bluetoothDevice);
        thermometer.temperatureRead();

        //Release the lock
        wl.release();
    }
}
