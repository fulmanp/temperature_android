package pl.fulmanski.android.tutorial.temperaturestation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class ActivitySelectDevice extends ActionBarActivity {
    private ListView listViewPairedDevices;
    private ArrayAdapter<BTDevice> bTArrayAdapter;
    private Context appContext;
    //private BluetoothDevice selectedDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_select_device);
        setUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_select_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUp(){
        MyApplication myApplication = MyApplication.getInstance();
        BluetoothAdapter bluetoothAdapter = myApplication.getBluetoothAdapter();
        appContext = getApplicationContext();


        listViewPairedDevices = (ListView) findViewById(R.id.listViewPairedDevices);
        bTArrayAdapter = new ArrayAdapter<BTDevice>(this, android.R.layout.simple_list_item_1);
        listViewPairedDevices.setAdapter(bTArrayAdapter);

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            bTArrayAdapter.clear();
            //ArrayList listPairedDevices = new ArrayList();
            for (BluetoothDevice dev : pairedDevices) {
                bTArrayAdapter.add(new BTDevice(dev.getName(), dev.getAddress(), dev));
            }
        }

        listViewPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                BTDevice bTDevice = (BTDevice) listViewPairedDevices.getItemAtPosition(position);
                MyApplication myApplication = MyApplication.getInstance();
                myApplication.setSelectedDevice(bTDevice.getDevice());
                //TextView editText = (TextView) findViewById(R.id.textViewDeviceValue);
                //editText.setText(bTDevice.getName());
                Toast.makeText(appContext, bTDevice.toString(),Toast.LENGTH_LONG).show();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("BTName", bTDevice.getName());
                setResult(RESULT_OK, returnIntent);
                // or
                // setResult(RESULT_CANCELED, returnIntent);
                // if we don't want to return any data

                finish();
            }
        });
    }
}
