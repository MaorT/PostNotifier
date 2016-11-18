package com.example.maor.postnotifier;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    Button btnStartService,btnStopService,btnSettings;



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
        btnSettings = (Button)findViewById(R.id.btnSettings);

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

        CheckAutoStart();


        // Set buttons events :
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartService();

            }
        });
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopService();

            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Preferences.class);
                startActivity(i);
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
    }


    private void StartService(){
        btnStartService.setEnabled(false);
        btnStopService.setEnabled(true);
        serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(serviceIntent);
    }

    private void StopService(){
        btnStartService.setEnabled(true);
        btnStopService.setEnabled(false);
        serviceIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        startService(serviceIntent);
    }

    private boolean GetAutoStartService(){

       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean("autoStartService", false);
    }

    private void CheckAutoStart(){
        if(MQTT.GetStatus() == false && GetAutoStartService() == true )
            StartService();

    }

}
