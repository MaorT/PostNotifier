package com.example.maor.postnotifier;

/**
 * Created by Maor on 16/11/2016.
 */

public class Constants {
    public interface ACTION {
         String MAIN_ACTION = "com.example.maor.postnotifier.action.main";
         String STARTFOREGROUND_ACTION = "com.example.maor.postnotifier.action.startforeground";
         String STOPFOREGROUND_ACTION = "com.example.maor.postnotifier.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}