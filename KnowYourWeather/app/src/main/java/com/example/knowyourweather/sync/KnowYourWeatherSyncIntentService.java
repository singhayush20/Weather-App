package com.example.knowyourweather.sync;

import android.app.Application;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.knowyourweather.utilities.KnowYourWeatherUtils;
//Create a new class called SunshineSyncIntentService that extends IntentService

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class KnowYourWeatherSyncIntentService {
    private static final String TAG= KnowYourWeatherSyncIntentService.class.getSimpleName();
    //extends IntentService {

//    /**
//     * @deprecated
//     */
//    public KnowYourWeatherSyncIntentService() {
//        super("KnowYourWeatherSyncIntentService");
//    }
//
//    /**
//     * @param intent
//     * @deprecated
//     */
//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        KnowYourWeatherSyncTask.syncWeather(this);
//    }

//    public KnowYourWeatherSyncIntentService(@NonNull Application application)
//    {
//        mWorkManager=WorkManager.getInstance(application.getApplicationContext());
//    }
//    public static  void loadWeather(Context context)
//    {
//        Log.i(TAG+" ###","Entered the loadWeather method()");
//        WorkManager mWorkManager;
//        mWorkManager=WorkManager.getInstance(context);
//        OneTimeWorkRequest oneTimeWorkRequest=new OneTimeWorkRequest.Builder(KnowYourWeatherSyncTask.class)
//                .build();
//        mWorkManager.enqueue(oneTimeWorkRequest);
//
//    }
}
