package com.example.maor.postnotifier;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.ArrayList;



public class MQTT extends Service implements MqttCallback {

    // MQTT Variable todo : move some of them to the save preference
    private MqttClient client = null;
    public static String mqtt_in_topic = "postal";
    public static String mqtt_out_topic = "postal";
    public static String mqtt_server_address = "m12.cloudmqtt.com";
    public static String mqtt_userName = "androidPostal";
    public static String mqtt_password = "123456";
    public static int mqtt_port = 16666;
    String ClientId = System.getProperty("user.name") + "." + System.currentTimeMillis(); // Generate a unique user id

    private boolean serviceOnFlag = false;


    //region LifeCycle and Events Methods

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mqttService", "mqtt onCreate");
        Toast.makeText(getApplicationContext(), "PostNotifier Service has been started", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("mqttService", "mqtt onStartCommand");
        Connect(mqtt_server_address,mqtt_port,ClientId,mqtt_userName,mqtt_password);
        Subscribe(mqtt_in_topic);
        serviceOnFlag = true;
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("mqttService", "mqtt onDestroy");
        serviceOnFlag = false;
        if(client != null && IsConnected()){
            UnSubscribe(mqtt_in_topic);
            Disconnect();
        }
        Toast.makeText(getApplicationContext(), "PostNotifier Service has been stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Wont be called as service is not bound
        Log.d("mqttService", "mqtt onBind");
        Toast.makeText(getApplicationContext(), "In onBind", Toast.LENGTH_SHORT);
        return null;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //Log.d("mqttService","new message:"+ message.toString());
        NotifyBroadcast(topic,message.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
        Log.d("mqttService", "mqtt deliveryComplete");

    }

    //endregion


    //region Connection methods

    public void Connect(String url,int port,String clientID) {
        try {
            MemoryPersistence persistance = new MemoryPersistence();
            client = new MqttClient("tcp://" + url + ":"+port, clientID, persistance);
            client.connect();
            client.setCallback(this);

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Connect(String url, int port, String clientID, MqttConnectOptions options) {
        try {
            MemoryPersistence persistance = new MemoryPersistence();
            client = new MqttClient("tcp://" + url + ":"+port, clientID, persistance);
            client.connect(options);
            client.setCallback(this);

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Connect(String url, int port, String clientID, String userName,String passWord) {
        try {
            MemoryPersistence persistance = new MemoryPersistence();
            String brokerUrl = "tcp://" + url + ":"+port;
            client = new MqttClient(brokerUrl,clientID, persistance);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(userName);
            options.setPassword(passWord.toCharArray());
            options.setCleanSession(true);
            client.connect(options);
            client.setCallback(this);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean IsConnected()
    {
        return client.isConnected();
    }

    public void Disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void connectionLost(Throwable cause) {
        //  Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_LONG);
        NotifyBroadcast("system","connectionLost");

        // Try to reconnect in a loop when the connection was lost, until service stopped or connected successfully
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!IsConnected() && serviceOnFlag)
                {
                    NotifyBroadcast("system","Trying to reconnect..");
                    Connect(mqtt_server_address,mqtt_port,ClientId,mqtt_userName,mqtt_password);
                    try { Thread.sleep(2000);}
                    catch (Exception ex) {}
                }
                Subscribe(mqtt_in_topic);
            }
        });
        thread.run();
    }



    //endregion




    public void Subscribe(String topic) {
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void UnSubscribe(String topic) {
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public  boolean Publish(String topic, String payload) {
        MqttMessage message = new MqttMessage(payload.getBytes());
        try {
            client.publish(topic, message);
            return true;
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return false;
    }



    private void NotifyBroadcast(String topic,String message){

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.mBroadcastStringAction);
        broadcastIntent.putExtra("Data",message); // Add data that is sent to service
        broadcastIntent.putExtra("Topic",topic); // Add data that is sent to service
        sendBroadcast(broadcastIntent);
    }



//    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) // todo: check why ICE_CREAM_SANDWICH  ??
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
//        Toast.makeText(getApplicationContext(), "In onTaskRemoved", Toast.LENGTH_LONG);
//        Log.d("mqttService", "In onTaskRemoved");
//    }



}
