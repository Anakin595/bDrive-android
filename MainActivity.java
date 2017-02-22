package com.example.krystian.bdrive;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;




public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "bluetooth1";
        //public static final int DISCOVERY_REQUEST = 1;
    Button aimleft,aimright,speedreset;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static SeekBar speed;
    private static TextView speedmeter,orient,lewo,prawo;
    private static int progShare=130,counter;
    private static boolean lock=false;

    static SensorManager sensorManager;      // Inicjowanie sensora


    //public String toastText="";
    //private BluetoothDevice remoteDevice;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // MAC-address of Bluetooth module
    private static String address = "98:D3:31:FB:1C:CF"; // MAC modułu Bluetooth
    //private static String address = "20:68:9D:7A:BC:DE"; // MAC laptopa

    public int part[] = {0, 0, 230, 230, 0};

    /*
        //BroadcastReveiver
        BroadcastReceiver bluetoothState = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String stateExtra = BluetoothAdapter.EXTRA_STATE;
                int state = intent.getIntExtra(stateExtra, -1);
                //String toastText="";
                switch(state){
                    case(BluetoothAdapter.STATE_TURNING_ON) :{
                        toastText="Bluetooth turning ON.";
                        Toast.makeText(getApplicationContext(), toastText,Toast.LENGTH_SHORT).show();

                        break;
                    }case(BluetoothAdapter.STATE_ON) :{
                        toastText="Bluetooth ON.";
                        Toast.makeText(getApplicationContext(), toastText,Toast.LENGTH_SHORT).show();

                        break;
                    }
                    case(BluetoothAdapter.STATE_TURNING_OFF) :{
                        toastText="Bluetooth turning OFF.";
                        setupUI();
                        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();

                        break;
                    }
                    case(BluetoothAdapter.STATE_OFF) :{
                        toastText="Bluetooth OFF";
                        Toast.makeText(getApplicationContext(), toastText,Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };

    */
    /*  *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setupUI();

        lewo = (TextView) findViewById(R.id.Left);
        prawo = (TextView) findViewById(R.id.Right);
        aimright = (Button) findViewById(R.id.aimright);    //obrót lufą
        aimleft = (Button) findViewById(R.id.aimleft);

        speed = (SeekBar) findViewById(R.id.speed1);            //suwak prędkości
        speedreset = (Button) findViewById(R.id.speedres);      //hamulec
        speedmeter = (TextView) findViewById(R.id.speedmeter);
        orient = (TextView) findViewById(R.id.orient);
        btAdapter = BluetoothAdapter.getDefaultAdapter();       //bluetooth

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);       //żyroskop
        Sensor mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);

        checkBTState();
        speedfun();


        aimright.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Toast.makeText(MainActivity.this, "right DOWN", Toast.LENGTH_SHORT).show();
                    part[1]=2;
                    sendData(""+join_msg(part));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(MainActivity.this, "right UP", Toast.LENGTH_SHORT).show();
                    part[1]=0;
                    sendData(""+join_msg(part));
                }
                return false;
            }
        }); // Obróć lufę w PRAWO

        aimleft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Toast.makeText(MainActivity.this, "left DOWN", Toast.LENGTH_SHORT).show();
                    part[1] = 1;
                    sendData("" + join_msg(part));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(MainActivity.this, "left UP", Toast.LENGTH_SHORT).show();
                    part[1] = 0;
                    sendData("" + join_msg(part));
                }
                return false;
            }
        }); // Obróć lufę w LEWO
        // Ustaw stan suwaka w Połowie (wartość 0)
        speedreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed.setProgress(speed.getMax() / 2);
            }
        });            // hamulec - ustawianie suwaka w połowie

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

/*
    private void setupUI() {
        final ToggleButton connect;
        toggleLight= (ToggleButton) findViewById(R.id.lights);
        connect = (ToggleButton) findViewById(R.id.connect);
        final TextView statusUpdate = (TextView) findViewById(R.id.textView);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter.isEnabled()){
            String adress = btAdapter.getAddress();
            String name = btAdapter.getName();
            String statusText = name + " : " + adress;
            statusUpdate.setText(statusText);
        }else{
            statusUpdate.setText("Bluetooth is not ON.");
        }


        connect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(connect.isChecked()){
                    Toast.makeText(getApplicationContext(), "Connecting.....",Toast.LENGTH_LONG).show();

//                    String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
//                    String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;
//                    IntentFilter filter = new IntentFilter(actionStateChanged);
//                    registerReceiver(bluetoothState, filter);
//                    startActivityForResult(new Intent(actionRequestEnable), 0);

*/
/*
                    //register for discovery events
                    String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
                    String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
                    IntentFilter filter = new IntentFilter(scanModeChanged);
                    registerReceiver(bluetoothState,filter);
                    startActivityForResult(new Intent(beDiscoverable),DISCOVERY_REQUEST);
*//*


                }else{
                    Toast.makeText(getApplicationContext(), "Disconnecting",Toast.LENGTH_SHORT).show();
                    btAdapter.disable();
                    statusUpdate.setText("Bluetooth is OFF");
                }
            }
        });//Connect / Disconnect Button

    }//end setupUI
*/

/*
    private void findDevices(){
        String lastUseRemoteDevice = getLastUsedRemoteBTDevice();
        if(lastUseRemoteDevice != null){
            toastText="Checking for known paired devices, namely:"+lastUseRemoteDevice;
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            for(BluetoothDevice pairedDevice : pairedDevices){
                if(pairedDevice.getAddress().equals(lastUseRemoteDevice)){
                    toastText="Found device: "+pairedDevice.getName()+"@"+lastUseRemoteDevice;
                    Toast.makeText(getApplicationContext(),toastText,Toast.LENGTH_SHORT).show();
                    remoteDevice = pairedDevice;
                }
            }
        }
        if(remoteDevice == null){
            toastText="Starting discovery for remote deviced...";
            Toast.makeText(getApplicationContext(),toastText,Toast.LENGTH_SHORT).show();
            if(btAdapter.startDiscovery()){
                toastText="Discovery thread started...Scanning for Devices";
                Toast.makeText(getApplicationContext(),toastText,Toast.LENGTH_SHORT).show();
                registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
        }
    }//end findDevices

    // Create a BroadcastReceiver to receive device discovery
    BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDevice remoteDevice;
            remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            toastText = "Discovered: "+ remoteDeviceName;
            Toast.makeText(getApplicationContext(),toastText,Toast.LENGTH_SHORT).show();

        }
    };//end BroadcastReceiver discovery
  */
/*
    private String getLastUsedRemoteBTDevice(){
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getString("LAST_REMOTE_DEVICE_ADDRESS", null);

    }//end getLastUsedRemoteDevice
 */
/*  *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   */

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == DISCOVERY_REQUEST){
            Toast.makeText(getApplicationContext(),"Discovery in progress",Toast.LENGTH_SHORT).show();
            setupUI();
            findDevices();
        }
    }
  */// end onActivityResult
    @Override
    public void onResume() {
       super.onResume();

       Log.d(TAG, "...onResume - try connect...");

       // Set up a pointer to the remote node using it's address.
       BluetoothDevice device = btAdapter.getRemoteDevice(address);

       // Two things are needed to make a connection:
       //   A MAC address, which we got above.
       //   A Service ID or UUID.  In this case we are using the
       //     UUID for SPP.

       try {
           btSocket = createBluetoothSocket(device);
       } catch (IOException e1) {
           errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
       }

       // Discovery is resource intensive.  Make sure it isn't going on
       // when you attempt to connect and pass your message.
       btAdapter.cancelDiscovery();

       // Establish the connection.  This will block until it connects.
       Log.d(TAG, "...Connecting...");
       try {
           btSocket.connect();
           Log.d(TAG, "...Connection ok...");
       } catch (IOException e) {
           try {
               btSocket.close();
           } catch (IOException e2) {
               errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
           }
       }

       // Create a data stream so we can talk to server.
       Log.d(TAG, "...Create Socket...");

       try {
           outStream = btSocket.getOutputStream();
       } catch (IOException e) {
           errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
       }
   }
    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    private void errorExit(String title, String message){
        //Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_SHORT).show();
        finish();
    }
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Send data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
            try {
                btSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            errorExit("Fatal Error", msg);

        }
    } // Wysyłanie informacji przez bluetooth



    public void lights(View view) {
        boolean checked = ((ToggleButton)view).isChecked();
        if(checked){
            Toast.makeText(getApplicationContext(), "Lights ON", Toast.LENGTH_SHORT).show();
            part[0]=1;
            sendData(""+join_msg(part));
        }else{
            Toast.makeText(getApplicationContext(), "Lights OFF", Toast.LENGTH_SHORT).show();
            part[0]=0;
            sendData(""+join_msg(part));
        }
    }        // Włącz / Wyłącz światła

    public void speedfun(){
        //speedmeter.setText("Speed: " + speed.getProgress() + "/" + speed.getMax());
        speed.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress_value=130;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress_value = progress;
                            if(progress_value%5==0) {
                                speedmeter.setText("Speed: " + (progress-(speed.getMax()/2)) + "/" + (speed.getMax()/2));
                                if(progress>156) {
                                    part[2] = 270;
                                    part[3] = 360;
                                }
                                else if(progress<105) {
                                    part[2] = 200;
                                    part[3] = 100;
                                }
                                else{
                                    part[2] = 230;
                                    part[3] = 230;
                                }
                                progShare = progress_value; // zmienna dzielona z innymi funkcjami korzystających z wartości prędkości
                                sendData("" + join_msg(part));
                            }
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //Toast.makeText(MainActivity.this, "StartTracking!!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        progShare = progress_value;
                        speedmeter.setText("Speed: " + (progress_value-(speed.getMax()/2)) + "/" + (speed.getMax()/2));
                       //Toast.makeText(MainActivity.this, "Stop    Tracking!!!!", Toast.LENGTH_SHORT).show();
                        //sendData("%" + progress_value);
                    }
                });
    }                // Obsługa zdarzeń suwaka

    public String join_msg(int[] part){
        return "$"+part[0]+" "+part[1]+" "+part[2]+" "+part[3]+"$";
    }   // funkcja łączenia elementów tablicy bufora

    @Override
    public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            Sensor mSensor = event.sensor;
            int progress = progShare;
            int val = (int)values[0];

            if(mSensor.getType() == Sensor.TYPE_ACCELEROMETER){
                if(val>=2){ //w lewo
                    if(progress>156){           //-- zakres od 156 do 260
                        part[2]=230;
                        part[3]=360;
                    }else if(progress<105){                      //-- zakres od 0 do 105
                        part[2]=230;
                        part[3]=100;
                    }
                    lock=true;
                    if(counter==3)
                        sendData(""+join_msg(part));
                }else if(val<=-2){ // w prawo
                    if(progress>156){           //-- zakres od 156 do 260
                        part[3]=230;
                        part[2]=360;
                    }else if(progress<105){                      //-- zakres od 0 do 105
                        part[3]=230;
                        part[2]=100;
                    }
                    lock=true;
                    if(counter==3)
                        sendData(""+join_msg(part));
                }else{
                    part[2]=100+progress;
                    part[3]=100+progress;
                    if(lock){
                        sendData(""+join_msg(part));
                        lock=false;
                    }
                }
                orient.setText("Y: " + val + " | " + progress);
                lewo.setText("L: " + part[2]);
                prawo.setText("P: " + part[3]);
                if(counter==4)
                    counter=0;
                counter++;
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}// end Main
