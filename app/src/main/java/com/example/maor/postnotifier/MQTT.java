package com.example.maor.postnotifier;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
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

    private static final String LOG_TAG = "ForegroundService";
    private boolean serviceOnFlag = false;

    Notification notification = null;

    private int numMessages = 0;
    private IntentFilter mIntentFilter;



    private  BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.mBroadcastStringAction)) {
//                mTextView.setText(mTextView.getText()
//                        + intent.getStringExtra("Data") + "\n\n");

                String topic = intent.getStringExtra("Topic");
                String data = intent.getStringExtra("Data");

                //Toast.makeText(context, "MQTT Received: "+ intent.getStringExtra("Data"), Toast.LENGTH_LONG).show();

                // When receiving a system notifications and not a regular data
//                if(topic.equals("system")){
//                    Vibrate(500);
//                    Toast.makeText(context, data, Toast.LENGTH_LONG).show();
//                    return;
//                }

                Toast.makeText(getApplicationContext(), "MQTT Received: "+ intent.getStringExtra("Data"), Toast.LENGTH_SHORT).show();
                NotifyOnNewMessage(); //todo:return\implement this action
                Log.d("mqttService","Service broadcast was notified");

            }
        }
    };










    //region LifeCycle and Events Methods

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mqttService", "mqtt onCreate");
        Toast.makeText(getApplicationContext(), "PostNotifier Service has been started", Toast.LENGTH_LONG).show();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(MainActivity.mBroadcastStringAction);
        mIntentFilter.addAction(MainActivity.mBroadcastStringAction);
        mIntentFilter.addAction(MainActivity.mBroadcastIntegerAction);
        mIntentFilter.addAction(MainActivity.mBroadcastArrayListAction);
        registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("mqttService", "mqtt onStartCommand");
//        Connect(mqtt_server_address,mqtt_port,ClientId,mqtt_userName,mqtt_password);
//        Subscribe(mqtt_in_topic);
//        serviceOnFlag = true;
//       // StartForegroung_test();
//        return START_REDELIVER_INTENT;
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");

            // todo : does it needed ?
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Connect(mqtt_server_address,mqtt_port,ClientId,mqtt_userName,mqtt_password);
            Subscribe(mqtt_in_topic);

            serviceOnFlag = true;

//            Intent previousIntent = new Intent(this, MQTT.class);
//            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
//            PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
//                    previousIntent, 0);

            Intent playIntent = new Intent(this, MQTT.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0);

            Intent nextIntent = new Intent(this, MQTT.class);
            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
            PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                    nextIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_stat_name);

            notification = new NotificationCompat.Builder(this)
                    .setContentTitle("ContentTitle")
                    .setTicker("setTicker")
                    .setContentText("ContentText")
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
//                    .addAction(android.R.drawable.ic_media_previous,
//                            "Previous", ppreviousIntent)
                    .addAction(android.R.drawable.ic_media_play, "Play",
                            pplayIntent)
                    .addAction(android.R.drawable.ic_media_next, "Stop",
                    pnextIntent).build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Log.i(LOG_TAG, "Clicked Previous");
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
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

        Log.d("mqttService","Notifying broadcast receiver..");
    }



    private void StartForegroung_test(){

            //Log.w(getClass().getName(), "Got to play()!");


            Notification note=new Notification(R.drawable.ic_stat_name,
                    "MQTT listener running..",
                    System.currentTimeMillis());

//
            Intent i=new Intent(this, MainActivity.class);

            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pi=PendingIntent.getActivity(this, 0,
                    i, 0);

//            note.setLatestEventInfo(this, "Fake Player",
//                    "Now Playing: \"Ummmm, Nothing\"",
//                    pi);
            note.flags|=Notification.FLAG_NO_CLEAR;

            startForeground(1337, note);
    }


    private void NotifyOnNewMessage(){

        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(200);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.ic_stat_name);
        mBuilder.setContentTitle("תיבת דואר");
        mBuilder.setContentText("דואר חדש הגיע");
        mBuilder.setNumber(++numMessages);
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        mNotificationManager.notify(numMessages, mBuilder.build()); // todo : choose notification number in a smart way


    }

}
