package com.example.maor.postnotifier;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStartService,btnStopService,btnSettings;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartService = (Button)findViewById(R.id.btnStartService);
        btnStopService = (Button)findViewById(R.id.btnStopService);
        btnSettings = (Button)findViewById(R.id.btnSettings);

        btnStopService.setEnabled(false);
        serviceIntent = new Intent(MainActivity.this, MQTT.class);

        if(MQTT.GetStatus()){
                btnStartService.setEnabled(false);
                btnStopService.setEnabled(true);
        }
        else
        {
            btnStartService.setEnabled(true);
            btnStopService.setEnabled(false);
        }

        CheckAutoStart();

        btnStartService.setOnClickListener(this);
        btnStopService.setOnClickListener(this);
        btnSettings.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /** Set buttons events **/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartService:
                StartService();
                break;
            case R.id.btnStopService:
                StopService();
                break;
            case R.id.btnSettings:
                Intent i = new Intent(MainActivity.this, Preferences.class);
                startActivity(i);
                break;
        }

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
        if(!MQTT.GetStatus() && GetAutoStartService())
            StartService();

    }

}
