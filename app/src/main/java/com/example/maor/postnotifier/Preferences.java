package com.example.maor.postnotifier;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {

   // private static boolean settingsChanged_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    /****************************/
    /**   Preference Fragment   **/
    /****************************/

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,Preference.OnPreferenceChangeListener
    {
        @Override

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String preferenceName = preference.getKey();

            /** General validators - No empty setting at all **/
            if(newValue.toString().length() == 0)
            {
                Toast.makeText(getActivity(),"Empty value not allowed! Try again",Toast.LENGTH_LONG).show();
                return false;
            }

            /** Set validators **/
            switch (preferenceName){
                case "mqttUserName":
                    if(newValue.toString().length() ==0){
                        Toast.makeText(getActivity(),"Username can't be empty! try again ",Toast.LENGTH_LONG).show();
                        return false;
                    }
                    break;
                case "mqttUserPassword":
                    if(newValue.toString().length() ==0){
                        Toast.makeText(getActivity(),"Password can't be empty! try again ",Toast.LENGTH_LONG).show();
                        return false;
                    }
                    break;
                case "mqttServerUrl":
                    if(newValue.toString().length() ==0){
                        Toast.makeText(getActivity(),"Server Url can't be empty! try again ",Toast.LENGTH_LONG).show();
                        return false;
                    }
                    break;
            }

            if(preferenceName.startsWith("mqtt")){
                Toast.makeText(getActivity(),"New MQTT settings - please restart the service",Toast.LENGTH_LONG).show();
                return true;
            }

            Toast.makeText(getActivity(),"The new " + preferenceName+" has been saved",Toast.LENGTH_LONG).show();
            return true;
        }

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            /** Get each preference and set it's PreferenceChangeListener **/
            SetPreferenceListenerByKey("mqttUserName");
            SetPreferenceListenerByKey("mqttUserPassword");
            SetPreferenceListenerByKey("mqttServerUrl");
            SetPreferenceListenerByKey("mqttServerPort");
            SetPreferenceListenerByKey("notification_sound");
            SetPreferenceListenerByKey("notification_vibration");
            SetPreferenceListenerByKey("notification_ringtone");
        }

        private void SetPreferenceListenerByKey(String keyName){

            final Preference pref = getPreferenceScreen().findPreference(keyName);
            pref.setOnPreferenceChangeListener(this);

        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //if (key.equals("MQTT_ServerIP")) {
          //  Preference pref = findPreference(key);
            MQTT.LoadPreferences();
//            pref.setSummary(sharedPreferences.getString(key, ""));
            // }
        }


        // Register and Unregister the OnSharedPreferenceChangeListener
        // When any preference is changed, it runs "onSharedPreferenceChanged"
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }/// End of the internal class


} /// End of the Preferences
