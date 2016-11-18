package com.example.maor.postnotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MQTT extends Service implements MqttCallback {

    private static Context context = null;

    // MQTT Variable todo : move some of them to the save preference
    private  MqttClient client = null;
    public static String mqtt_in_topic = "demoTopic";
    public static String mqtt_out_topic = "demoTopic";
    public static String mqtt_server_address = "demo.mqtt.com";
    public static String mqtt_userName = "demo username";
    public static String mqtt_password = "demo password";
    public static int mqtt_port = 1883; // for demo, now using 16666
    // Other settigns flags
    private static boolean notification_vibration = true;
    private static boolean notification_sound = true;

    String ClientId = System.getProperty("user.name") + "." + System.currentTimeMillis(); // Generate a unique user id

    private static final String LOG_TAG = "PostNotifier";

    private static boolean serviceOnFlag = false; //helps to know the service status (run/stop)

    Notification notification = null;

    private int numMessages = 0; // count the notification number



    private IntentFilter mIntentFilter;
    public static final String mBroadcastStringAction = "com.maorservice.string";
    public static final String mBroadcastIntegerAction = "com.maorservice.integer";
    public static final String mBroadcastArrayListAction = "com.maorservice.arraylist";



    private  BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastStringAction)) {
                String topic = intent.getStringExtra("Topic");
                String data = intent.getStringExtra("Data");
                boolean retained = intent.getExtras().getBoolean("Retained");

                if(data.length() == 0) // Ignore empty messages
                    return;

                // When receiving a system notifications and not a regular data
                if(topic.equals("system")){
                    if(data.equals("newSettings")){
                        LoadPreferences(); // refresh new settings
//                        if(!IsConnected())
//                            Connect(mqtt_server_address,mqtt_port,ClientId,mqtt_userName,mqtt_password);
//                        if(IsConnected())
//                            Subscribe(mqtt_in_topic);

                    }
                    Toast.makeText(context, data, Toast.LENGTH_LONG).show();
                    return;
                }

                NotifyOnNewMessage(data);
                // Clear retained message - prevent from getting notification on every re-connection
                // It's a king of a 'ACK' to tell the server that the massage was arrived successfully
                ClearReatinedMessage();

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
        mIntentFilter.addAction(mBroadcastStringAction);
        mIntentFilter.addAction(mBroadcastStringAction);
        mIntentFilter.addAction(mBroadcastIntegerAction);
        mIntentFilter.addAction(mBroadcastArrayListAction);
        registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            context = this.getBaseContext();
            LoadPreferences();
            Connect(mqtt_server_address,mqtt_port,ClientId,mqtt_userName,mqtt_password);
            Subscribe(mqtt_in_topic);
            serviceOnFlag = true;
            notification = BuildForegroundNotification(getString(R.string.app_name),"Connecting");
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(MainActivity.class);
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,notification);

            if(IsConnected())
                Update_Foreground_Notification_Text("Connected"); // todo : Do something when unsuccessful connection
        }
         else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "PostNotifier Service has been stopped");
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
        return null;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //Log.d("mqttService","new message:"+ message.toString());
        NotifyBroadcast(topic,message.toString(),message.isRetained());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
        Log.d("mqttService", "mqtt deliveryComplete");

    }

    public static boolean GetStatus(){
        return serviceOnFlag;
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
       // Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_LONG);
        NotifyBroadcast("system","PostNotifier -connectionLost",false);
        Update_Foreground_Notification_Text("Disconnected!");
        // Try to reconnect in a loop when the connection was lost, until service stopped or connected successfully
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!IsConnected() && serviceOnFlag)
                {
                    NotifyBroadcast("system","Trying to reconnect..",false);
                    Connect(mqtt_server_address,mqtt_port,ClientId,mqtt_userName,mqtt_password);
                    try { Thread.sleep(10000);}
                    catch (Exception ex) {}
                }

                NotifyBroadcast("system","Connected successfully",false);
                Update_Foreground_Notification_Text("Connected");
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


    public boolean Publish(String topic, String payload,int qos,boolean retained) {
        try {
            client.publish(topic, payload.getBytes(),qos,retained);
            return true;
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void NotifyBroadcast(String topic,String message,boolean retained){

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(mBroadcastStringAction);
        broadcastIntent.putExtra("Data",message); // Add data that is sent to service
        broadcastIntent.putExtra("Topic",topic); // Add data that is sent to service
        broadcastIntent.putExtra("Retained",retained); // Add data that is sent to service

        sendBroadcast(broadcastIntent);

        Log.d("mqttService","Notifying broadcast receiver..");
    }


    private void NotifyOnNewMessage(String message){



      //  Vibrate(1000);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.ic_post1);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_post1);
        mBuilder.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false));
        mBuilder.setContentTitle("תיבת דואר");
        mBuilder.setContentText(message);
        mBuilder.setNumber(++numMessages);
        if(notification_sound)
            mBuilder.setSound(alarmSound);

        if(notification_vibration){
            long[] pattern = {500,1000,500,1000,500}; // todo : learn how it's work
            mBuilder.setVibrate(pattern);
        }

        mBuilder.setLights(Color.GREEN, 300, 300);
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

    private void Vibrate(int timeMs){
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(timeMs);
    }


    private Notification BuildForegroundNotification(String title,String text)
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // todo : add large icon
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_post_icon);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.ic_post_icon);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    private void Update_Foreground_Notification_Text(String text){

        Notification note = BuildForegroundNotification(getString(R.string.app_name),text);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,note);
    }

    public static void LoadPreferences(){

        if(context == null)
            return;

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);

        mqtt_userName = SP.getString("mqttUserName", "NA");
        mqtt_password = SP.getString("mqttUserPassword", "undefined");
        mqtt_server_address = SP.getString("mqttServerUrl", "undefined.mqtt.com");
        mqtt_in_topic = SP.getString("mqttInTopic", "undefined");
        mqtt_out_topic = SP.getString("mqttOutTopic", "undefined");
        mqtt_port = Integer.parseInt(SP.getString("mqttServerPort", "1883"));
        notification_vibration = SP.getBoolean("notification_vibration", false);
        notification_sound = SP.getBoolean("notification_sound", false);

        Log.d(LOG_TAG,"The new setting was loaded");
    }


    public void ClearReatinedMessage(){
        if(client == null || !client.isConnected())
            return;
        Publish(mqtt_in_topic,"",1,true);
    }

}
