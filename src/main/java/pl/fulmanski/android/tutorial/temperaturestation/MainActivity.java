package pl.fulmanski.android.tutorial.temperaturestation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity {
    //private Thermometer thermometer;
    //private Context appContext;
    private MyApplication myApplication;
    BluetoothAdapter bluetoothAdapter;
    static final int ALARM_UNIQUE_ID = 1;
    static final int REQUEST_CODE = 0;
    private static final int settings= Menu.FIRST;
    Context appContext;
    private final static String STORETEXT="storetext.txt";
    //private EditText txtEditor;
    private int button_id;
    private RadioButton button;
    RadioGroup group_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //appContext = getApplicationContext();
        //MyApplication myApplication = MyApplication.getInstance(this);
        myApplication = MyApplication.getInstance();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        myApplication.setBluetoothAdapter(bluetoothAdapter);
        appContext = getApplicationContext();
        boolean isWorking = isSetTemperatureAlarm();
        TextView editText = (TextView) findViewById(R.id.textViewStatusValue);
        editText.setText(isWorking ? "Is running..." : "Stop");
        Button buttonStartStop = (Button)findViewById(R.id.buttonStartStopService);
        buttonStartStop.setText(isWorking ? getResources().getString(R.string.button_start_stop_service_stop) : getResources().getString(R.string.button_start_stop_service_start));
        group_button  = (RadioGroup) findViewById(R.id.prefgroup);

        try {

            InputStream in = openFileInput(STORETEXT);

            if (in != null) {

                InputStreamReader tmp=new InputStreamReader(in);

                BufferedReader reader=new BufferedReader(tmp);

                String str;

                StringBuilder buf = new StringBuilder();
                StringBuilder buf2 = new StringBuilder();
                StringBuilder buf3 = new StringBuilder();

                if ((str = reader.readLine()) != null) {

                    buf.append(str);
                }
                else
                    buf.append("None");

                if ((str = reader.readLine()) != null) {

                    buf2.append(str);
                }
                else
                    buf2.append("None");

                if((str = reader.readLine()) != null) {
                    buf3.append(str);
                }
                else
                    buf3.append("5");
                in.close();

                SharedPreferences prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();

                button_id = Integer.valueOf(buf3.toString());

                edit.putString("username", buf.toString());
                edit.putString("server", buf2.toString());
                edit.putString("button_id", buf3.toString());
                edit.commit();

                Toast.makeText(this, "Welcome, " + buf.toString(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Server: " + buf2.toString(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Button ID: " + buf3.toString(), Toast.LENGTH_SHORT).show();
            }

        }

        catch (java.io.FileNotFoundException e) {
            // that's OK, we probably haven't created it yet
        }

        catch (Throwable t) {

            Toast

                    .makeText(this, "Exception: "+t.toString(), Toast.LENGTH_LONG)

                    .show();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(bTDiscoveryReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch  (id) {
            case  R.id.action_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ActivitySettings.class);
                startActivity(intent);
//                return  true;
                break;
            default:
                return  super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // It's not real service. Instead of service we use Alarms
    public void onClickStartStopService(View view) {
        // Check first if BT and Network is turn on
        if ( !(isBlueToothOn() && isNetworkOn())){
            return;
        }
        // If it's ok, do what you want to do...
        boolean isWorking = isSetTemperatureAlarm();
        if (isWorking){ // Stop
            cancelTemperatureAlarm();
        } else { // Start
            setTemperatureAlarm();
        }
        TextView editText = (TextView) findViewById(R.id.textViewStatusValue);
        editText.setText(!isWorking ? "Is running..." : "Stop");

        Button buttonStartStop = (Button)findViewById(R.id.buttonStartStopService);
        buttonStartStop.setText(!isWorking ? getResources().getString(R.string.button_start_stop_service_stop) : getResources().getString(R.string.button_start_stop_service_start));
    }

    // Code taken from:
    // http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
    private boolean isNetworkOn() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean networkOn = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!networkOn)
            Toast.makeText(this, "No connection to Network", Toast.LENGTH_SHORT).show();
        return networkOn;
    }

    private boolean isBlueToothOn(){
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "This device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                return true;
            } else {
                Toast.makeText(this, "Please turn on bluetooth", Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

    public void onClickSelectPairedDevice(View view) {
        if (isBlueToothOn()){
            Intent intent = new Intent(this, ActivitySelectDevice.class);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {
            String bTName = getResources().getString(R.string.unknown);
            if (resultCode == RESULT_OK) {
                bTName = data.getStringExtra("BTName");
            }
            else if (resultCode == RESULT_CANCELED) {
                bTName = "No BT device";
            }
            TextView editText = (TextView) findViewById(R.id.textViewDeviceValue);
            editText.setText(bTName);
        }
    }

    public void onClickTemperatureShow(View view) {

    }

    private boolean isSetTemperatureAlarm(){
        Intent intent = new Intent(appContext, AlarmManagerBroadcastReceiver.class);
        boolean isWorking = (PendingIntent.getBroadcast(appContext,
                                                        ALARM_UNIQUE_ID,
                                                        intent,
                                                        PendingIntent.FLAG_NO_CREATE) != null);
                                                        //just changed the flag
        return isWorking;
        //Log.d(TAG, "alarm is " + (isWorking ? "" : "not") + " working...");
    }

    private void cancelTemperatureAlarm(){
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(appContext, AlarmManagerBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, ALARM_UNIQUE_ID, intent, 0);

        alarmManager.cancel(pendingIntent);
    }

    private void setTemperatureAlarm(){
        int everyXMinutes;
        button = (RadioButton) findViewById(button_id);

        if(button.getText().equals("1 minute")) {
            everyXMinutes = 1;
            Toast.makeText(this, "Frequency: 1 minute", Toast.LENGTH_SHORT).show();
        }
        else if(button.getText().equals("5 minutes")) {
            everyXMinutes = 5;
            Toast.makeText(this, "Frequency: 5 minutes", Toast.LENGTH_SHORT).show();
        }
        else if(button.getText().equals("10 minutes")) {
            everyXMinutes = 10;
            Toast.makeText(this, "Frequency: 10 minutes", Toast.LENGTH_SHORT).show();
        }
        else {
            everyXMinutes = 5; //1; //default value
            Toast.makeText(this, "Default frequency: 10 minutes", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Set repeated alarm", Toast.LENGTH_SHORT).show();
        // Set alarm
        AlarmManager alarmManager=(AlarmManager)appContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(appContext, AlarmManagerBroadcastReceiver.class);

        // It is possible to do this because BluetoothDevice class implements Parcelable
        //intent.putExtra("selectedDevice", selectedDevice);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, ALARM_UNIQUE_ID, intent, 0);

        Calendar cal = Calendar.getInstance();
        // Set data and time; remember that for months 0 is January
        //cal.set(2015, 4, 21, 22, 47, 0);

        // One time alarm
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

        // Next repeat every 5 minutes staring at the time with minutes % everyXMinutes = 0
        cal.set(Calendar.SECOND, 0);
        int min = cal.get(Calendar.MINUTE);
        int rest = min % everyXMinutes;
        cal.add(Calendar.MINUTE, everyXMinutes - rest);

        // Repeating alarm
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1000 * 60 * everyXMinutes, pendingIntent);
    }
}
