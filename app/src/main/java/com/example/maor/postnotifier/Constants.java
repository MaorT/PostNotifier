package com.example.maor.postnotifier;

/**
 * Created by Maor on 16/11/2016.
 */

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.example.maor.postnotifier.action.main";
        public static String STARTFOREGROUND_ACTION = "com.example.maor.postnotifier.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.example.maor.postnotifier.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}