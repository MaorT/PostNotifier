package com.example.maor.postnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {



    Button btnStartService,btnStopService;

    // todo: do i need it  (and the intent filter) ?
    public static final String mBroadcastStringAction = "com.maorservice.string";
    public static final String mBroadcastIntegerAction = "com.maorservice.integer";
    public static final String mBroadcastArrayListAction = "com.maorservice.arraylist";
    private IntentFilter mIntentFilter;
    private Intent serviceIntent;

    private boolean serviceOnFlag = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartService = (Button)findViewById(R.id.btnStartService);
        btnStopService = (Button)findViewById(R.id.btnStopService);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastStringAction);
        mIntentFilter.addAction(mBroadcastIntegerAction);
        mIntentFilter.addAction(mBroadcastArrayListAction);

        serviceIntent = new Intent(this, BroadcastService.class);

        btnStopService.setEnabled(false);

      //  startService(serviceIntent);




        // Set buttons events :
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Toast.makeText(getBaseContext(), "Service started", Toast.LENGTH_LONG).show();
               // registerReceiver(mReceiver, mIntentFilter);
                startService(serviceIntent);
                serviceOnFlag = true;
                btnStartService.setEnabled(false);
                btnStopService.setEnabled(true);
            }
        });
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Toast.makeText(getApplicationContext(), "Service stoped", Toast.LENGTH_LONG).show();
                stopService(serviceIntent);
                unregisterReceiver(mReceiver);
                serviceOnFlag = false;
                btnStartService.setEnabled(true);
                btnStopService.setEnabled(false);
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    private  BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastStringAction)) {
//                mTextView.setText(mTextView.getText()
//                        + intent.getStringExtra("Data") + "\n\n");
                Toast.makeText(context, "Service: "+ intent.getStringExtra("Data"), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onPause() {
        //  unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Intent stopIntent = new Intent(MainActivity.this,
//                BroadcastService.class);
//        stopService(stopIntent);
    }

    @Override
    public void onBackPressed() {
        if(serviceOnFlag){
            Toast.makeText(getApplicationContext(), "The service is still running - going background", Toast.LENGTH_LONG).show();
            moveTaskToBack(true);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "The service is stopped - exiting the app", Toast.LENGTH_LONG).show();
            moveTaskToBack(false);
        }
    }


    public static Context GetContext(){
        return GetContext();
    }

    public void NotifyTest(){

        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }


}
