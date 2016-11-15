package com.example.maor.postnotifier;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class BroadcastService extends Service {
    private String LOG_TAG = null;
    private ArrayList<String> mList;
    private static int staticCounter = 0;

    private static MQTT mqtt = null;
    // MQTT Variable todo : move to save preference
    public static String mqtt_in_topic = "postal";
    public static String mqtt_out_topic = "postal";
    public static String mqtt_server_address = "m12.cloudmqtt.com";
    public static String mqtt_userName = "androidPostal";
    public static String mqtt_password = "123456";


    @Override
    public void onCreate() {
        super.onCreate();
        LOG_TAG = this.getClass().getSimpleName();
        Log.d("mqttService", "In onCreate");
        Toast.makeText(getApplicationContext(), "PostNotifier Service has been started", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("mqttService", "In onStartCommand");

        mqtt = new MQTT();
        String ClientId = System.getProperty("user.name") + "." + System.currentTimeMillis();
        mqtt.Connect(mqtt_server_address,16666,ClientId,mqtt_userName,mqtt_password);
        mqtt.Subscribe(mqtt_in_topic);
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Wont be called as service is not bound
        Toast.makeText(getApplicationContext(), "In onBind", Toast.LENGTH_SHORT);
        Log.d("mqttService", "In onBind");
        return null;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) // todo: check why ICE_CREAM_SANDWICH  ??
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Toast.makeText(getApplicationContext(), "In onTaskRemoved", Toast.LENGTH_LONG);
        Log.d("mqttService", "In onTaskRemoved");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "In onDestroy", Toast.LENGTH_LONG);
        if(mqtt != null && mqtt.IsConnected()){
            mqtt.UnSubscribe(mqtt_in_topic);
            mqtt.Disconnect();
        }
        Toast.makeText(getApplicationContext(), "PostNotifier Service has been stopped", Toast.LENGTH_LONG).show();
        Log.d("mqttService", "In onDestroy");
    }


    public void StopService(){
        //todo : check how to stop the service and not Unsubscribe

        if(mqtt != null && mqtt.IsConnected()){
            mqtt.UnSubscribe(mqtt_in_topic);
            mqtt.Disconnect();
        }
    }



}
