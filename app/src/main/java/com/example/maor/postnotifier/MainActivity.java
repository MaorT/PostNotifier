package com.example.maor.postnotifier;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    Button btnStartService,btnStopService;



    private Intent serviceIntent;

    private boolean serviceOnFlag = false;
    int numMessages = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("mqttService","MainActivity onCreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartService = (Button)findViewById(R.id.btnStartService);
        btnStopService = (Button)findViewById(R.id.btnStopService);
        btnStopService.setEnabled(false);
        serviceIntent = new Intent(MainActivity.this, MQTT.class);


        if(MQTT.GetStatus() == true){
                btnStartService.setEnabled(false);
                btnStopService.setEnabled(true);
        }
        else
        {
            btnStartService.setEnabled(true);
            btnStopService.setEnabled(false);
        }

      //  startService(serviceIntent);

        // Set buttons events :
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Toast.makeText(getBaseContext(), "Service started", Toast.LENGTH_LONG).show();
//                registerReceiver(mReceiver, mIntentFilter);
//                startService(serviceIntent);
//                serviceOnFlag = true;
                btnStartService.setEnabled(false);
                btnStopService.setEnabled(true);

              //  registerReceiver(mReceiver, mIntentFilter);
                serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                startService(serviceIntent);
            }
        });
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Toast.makeText(getApplicationContext(), "Service stoped", Toast.LENGTH_LONG).show();
//                unregisterReceiver(mReceiver);
//                stopService(serviceIntent);
//                serviceOnFlag = false;
                btnStartService.setEnabled(true);
                btnStopService.setEnabled(false);

                serviceIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                startService(serviceIntent);
            }
        });


    }

    @Override
    public void onResume() {
        Log.d("mqttService","MainActivity onResume ");
        super.onResume();
       // registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        Log.d("mqttService","MainActivity onPause ");
        //  unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("mqttService","MainActivity onDestroy ");
        super.onDestroy();
//        Intent stopIntent = new Intent(MainActivity.this,
//                BroadcastService.class);
//        stopService(stopIntent);
    }

//    @Override
//    public void onBackPressed() {
//
//        Log.d("mqttService","MainActivity onBackPressed ");
//        if(serviceOnFlag){
//            Toast.makeText(getApplicationContext(), "The service is still running - going background", Toast.LENGTH_LONG).show();
//            moveTaskToBack(true);
//        }
//        else
//        {
//            Toast.makeText(getApplicationContext(), "The service is stopped - exiting the app", Toast.LENGTH_LONG).show();
//            moveTaskToBack(false);
//        }
//    }

}
