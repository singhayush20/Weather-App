package com.example.knowyourweather.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.knowyourweather.data.KnowYourWeatherPreferences;
import com.example.knowyourweather.data.WeatherContract;
import com.example.knowyourweather.utilities.NetworkUtility;
import com.example.knowyourweather.utilities.NotificationUtils;
import com.example.knowyourweather.utilities.OpenWeatherJsonUtils;

import java.io.IOException;
import java.net.URL;

//Within KnowYourWeatherSyncTask, create a synchronized public static void method called syncWeather
//Within syncWeather, fetch new weather data
//If we have valid results, delete the old data and insert the new
public class KnowYourWeatherSyncTask {//extends Worker {
    //private Context mContext;
    private static final String TAG = KnowYourWeatherSyncTask.class.getSimpleName();
//    /*public KnowYourWeatherSyncTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
//        super(context, workerParams);
//        mContext=context;
//        Log.i(TAG+"###","returning from constructor");
//    }
//
//    /**
//     * Override this method to do your actual background processing.  This method is called on a
//     * background thread - you are required to <b>synchronously</b> do your work and return the
//     * {@link Result} from this method.  Once you return from this
//     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
//     * you need to do your work asynchronously on a thread of your own choice, see
//     * {@link ListenableWorker}.
//     * <p>
//     * A Worker has a well defined
//     * <a href="https://d.android.com/reference/android/app/job/JobScheduler">execution window</a>
//     * to finish its execution and return a {@link Result}.  After
//     * this time has expired, the Worker will be signalled to stop.
//     *
//     * @return The {@link Result} of the computation; note that
//     * dependent work will not execute if you use
//     * {@link Result#failure()} or
//     * {@link Result#failure(Data)}
//     */
//    @NonNull
//    @Override
//    public Result doWork() {
//        Log.i(TAG+" ###","entered the doWork() method");
//        try {
//            syncWeather(mContext);
//        }
//        catch(Exception e)
//        {
//            Log.e(TAG+" ###","exception has occurred");
//            return Result.failure();
//        }
//        return Result.success();
//    }
    //Within KnowYourWeatherSyncTask, create a synchronized public static void method called syncWeather

    /**
     * Performs the network request for updated weather, parses the JSON from that request, and
     * inserts the new weather information into our ContentProvider. Will notify the user that new
     * weather has been loaded if the user hasn't been notified of the weather within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    private static int callCount=0;
    synchronized public static void syncWeather(Context context)
    {
        Log.i(TAG+" ####@","syncWeather() called "+(++callCount)+" times");
        Log.i(TAG+" ####","entered the syncWeather()");
        //fetch new weather data
        try
        {
            /*
             * The getUrl method will return the URL that we need to get the forecast JSON for the
             * weather. It will decide whether to create a URL based off of the latitude and
             * longitude or off of a simple location as a String.
             */
            URL weatherRequestURL= NetworkUtility.getUrl(context);
            Log.i(TAG+" ####","URL obtained from NetworkUtility: "+weatherRequestURL);
            //Use the Url to retrieve Json
            String jsonWeatherResponse=NetworkUtility.getResponseFromHttpUrl(weatherRequestURL);
            Log.i(TAG+" ####","json Response obtained in syncWeather");//+jsonWeatherResponse);
            /*Parse the JSON response into a list of weather values*/
            ContentValues[] weatherValues= OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context,jsonWeatherResponse);
            /*
             * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if(weatherValues!=null&&weatherValues.length!=0)
            {
                Log.i(TAG+" ####","entered the if(), weatherValues.length: "+weatherValues.length);
                ContentResolver contentResolver=context.getContentResolver();
                // If we have valid results, delete the old data and insert the new
                /* Delete old weather data because we don't need to keep multiple days' data */
                contentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI,null,null);
                /* Insert our new weather data into Sunshine's ContentProvider */
                contentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,weatherValues);
            }

            //Check if notifications are enabled
            boolean notificationsEnabled= KnowYourWeatherPreferences.areNotificationsEnabled(context);
            Log.i(TAG+" ####","in syncWeather(), checking if notifications are enabled: "+notificationsEnabled);
            /*
             * If the last notification was shown was more than 1 day ago, we want to send
             * another notification to the user that the weather has been updated. Remember,
             * it's important that you shouldn't spam your users with notifications.
             */
            long timeSinceLastNotification = KnowYourWeatherPreferences
                    .getEllapsedTimeSinceLastNotification(context);
            boolean oneDayPassedSinceLastNotification=false;
            Log.i(TAG+" ####@","weather one day has passed since last notification: "+oneDayPassedSinceLastNotification);
            Log.i(TAG+" ####@","time since last notification: "+timeSinceLastNotification);
            if(timeSinceLastNotification>= DateUtils.MINUTE_IN_MILLIS) {
                oneDayPassedSinceLastNotification = true;
                Log.i(TAG + " ####@", "(entered if) weather one day has passed since last notification: " + oneDayPassedSinceLastNotification);
            }
            /*
             * We only want to show the notification if the user wants them shown and we
             * haven't shown a notification in the past day.
             */
            if(notificationsEnabled && oneDayPassedSinceLastNotification)
            {
                NotificationUtils.notifyUserOfWeather(context);
                Log.i(TAG+" ####","user notified");
            }
            Log.i(TAG+" ####@","returning from syncWeather");
        }
        catch(Exception e)
        {
            //Error has occurred
            e.printStackTrace();
        }
    }
}
