package pl.fulmanski.android.tutorial.temperaturestation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class ActivitySettings extends ActionBarActivity {

    private final static String STORETEXT="storetext.txt";

    private EditText txtEditor;
    private EditText txtEditor2;
    private RadioGroup group_button;
    private RadioButton button;
    private int selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_settings);

        getSupportActionBar().setTitle("Settings");

        txtEditor = (EditText) findViewById(R.id.editText);
        txtEditor2 = (EditText) findViewById(R.id.editText2);
        group_button = (RadioGroup) findViewById(R.id.prefgroup);

        SharedPreferences received_prefs = getSharedPreferences("my_prefs", 0);
        String username = received_prefs.getString("username", "");
        int buttonId = Integer.valueOf(received_prefs.getString("button_id", ""));
        String server = received_prefs.getString("server", "");

        group_button.check(buttonId);
        txtEditor.setText(username);
        txtEditor2.setText(server);
        Toast.makeText(this, username, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, server, Toast.LENGTH_SHORT).show();

        /*try {
            Bundle extras = getIntent().getExtras();
            String result = extras.getString("username");

            txtEditor.setText(result);
        }

        catch (java.lang.NullPointerException e) {}

        catch (Throwable t) {

            Toast

                    .makeText(this, "Exception: "+t.toString(), Toast.LENGTH_LONG)

                    .show();

        }*/

        /*try {

            InputStream in = openFileInput(STORETEXT);

            if (in != null) {

                InputStreamReader tmp=new InputStreamReader(in);

                BufferedReader reader=new BufferedReader(tmp);

                String str;

                StringBuilder buf=new StringBuilder();

                if ((str = reader.readLine()) != null) {

                    buf.append(str);

                }
                else
                    buf.append("Username");

                in.close();

                txtEditor.setText(buf.toString());

            }

        }

        catch (java.io.FileNotFoundException e) {

            // that's OK, we probably haven't created it yet

        }

        catch (Throwable t) {

            Toast

                    .makeText(this, "Exception: "+t.toString(), Toast.LENGTH_LONG)

                    .show();

        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_settings, menu);
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

    public void onClickSaveSettings(View view){

        try {

            OutputStreamWriter out=

                    new OutputStreamWriter(openFileOutput(STORETEXT, 0));

            /*RadioGroup group = (RadioGroup) findViewById(R.id.prefgroup);
            int selected = group.getCheckedRadioButtonId();

            Toast.makeText(this, "Username: " + String.valueOf(selected), Toast.LENGTH_SHORT).show();
            RadioButton button = (RadioButton) findViewById(selected);

            String value = (String) button.getText();

            if(value.equals("1 minute"))
                Toast.makeText(this, "Frequency: 1 minute", Toast.LENGTH_SHORT).show();
            else if(value.equals("5 minutes"))
                Toast.makeText(this, "Frequency: 5 minutes", Toast.LENGTH_SHORT).show();
            else if(value.equals("10 minutes"))
                Toast.makeText(this, "Frequency: 10 minutes", Toast.LENGTH_SHORT).show();*/

            //String value = (String) button.getText();
            out.write(txtEditor.getText().toString() + System.getProperty("line.separator"));

            out.write(txtEditor2.getText().toString() + System.getProperty("line.separator"));

            selected = group_button.getCheckedRadioButtonId();
            out.write(String.valueOf(selected) + System.getProperty("line.separator"));



            out.close();

            Toast

                    .makeText(this, "The contents are saved in the file.", Toast.LENGTH_LONG)

                    .show();

        }

        catch (Throwable t) {

            Toast

                    .makeText(this, "Exception: "+t.toString(), Toast.LENGTH_LONG)

                    .show();

        }

        final Context context = this;
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
    }
}
