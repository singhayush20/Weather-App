package com.example.knowyourweather.sync;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.knowyourweather.data.WeatherContract;
import com.example.knowyourweather.utilities.AppExecutors;

import java.util.concurrent.TimeUnit;

public class KnowYourWeatherSync {
    /*
        It’s best practice to not initialize things more
        than once, so for that, we will make sure that
        startImmediateSync will only get called once when
        the app starts and only if the database was empty.
         */
    private static boolean sInitialized;

    private static final String TAG = KnowYourWeatherSync.class.getSimpleName();

    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     *
     * @param context
     */
    public static void startImmediateSync(@NonNull final Context context) {

        Log.i(TAG + " ###", "entered the startImmediateSync() method");
        //Start the KnowYourWeatherIntentService
//        Intent intentToSyncImmediately=new Intent(context,KnowYourWeatherSyncIntentService.class);
//        context.startService(intentToSyncImmediately);
        //KnowYourWeatherSyncIntentService.loadWeather(context);
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     *
     * @param context Context that will be passed to other methods and used to access the
     *                ContentResolver
     */
    // Create a synchronized public static void method called initialize
    synchronized public static void initialize(Context context) {
        Log.i(TAG + " ###", "entered the initialize method: sInitialized: " + sInitialized);
        // Only execute this method body if sInitialized is false
        /*
         * Only perform initialization once per app lifetime. If initialization has already been
         * performed, we have nothing to do in this method.
         */
        if (sInitialized)
            return;
        sInitialized = true;
//        //Check to see if our weather ContentProvider is empty
//        /*
//         * We need to check to see if our ContentProvider has data to display in our forecast
//         * list. However, performing a query on the main thread is a bad idea as this may
//         * cause our UI to lag. Therefore, we create a thread in which we will run the query
//         * to check the contents of our ContentProvider.
//         */
//        new AsyncTask<Void,Void,Void>()
//        {
//            /**
//             * @param voids
//             * @deprecated
//             */
//            @Override
//            protected Void doInBackground(Void... voids) {
//                Log.i(TAG+" ###","entered the doInBackground() method");
//                /*URI for every row of weather data in our table
//                 */
//                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
//                Log.i(TAG+" ###","forecastQueryUri: "+forecastQueryUri);
//                /*
//                 * Since this query is going to be used only as a check to see if we have any
//                 * data (rather than to display data), we just need to PROJECT the ID of each
//                 * row. In our queries where we display data, we need to PROJECT more columns
//                 * to determine what weather details need to be displayed.
//                 */
//                String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
//                String selectionStatement = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();
//                /* Here, we perform the query to check to see if we have any weather data */
//                Cursor cursor = context.getContentResolver().query(forecastQueryUri, projectionColumns, selectionStatement, null, null);
//                /*
//                 * A Cursor object can be null for various different reasons. A few are
//                 * listed below.
//                 *
//                 *   1) Invalid URI
//                 *   2) A certain ContentProvider's query method returns null
//                 *   3) A RemoteException was thrown.
//                 *
//                 * Bottom line, it is generally a good idea to check if a Cursor returned
//                 * from a ContentResolver is null.
//                 *
//                 * If the Cursor was null OR if it was empty, we need to sync immediately to
//                 * be able to display data to the user.
//                 */
//                Log.i(TAG+" ###","in loadInBackground() cursor.getCount(): "+cursor.getCount());
//                if (cursor == null || cursor.getCount() == 0)
//                    startImmediateSync(context);
//                //Make sure to close the cursor.
//                assert cursor != null;
//                cursor.close();
//                return null;
//            }
//        }.execute();
        //Call the method created to schedule a periodic sync of weather data
        /*
         * This method call triggers KnowYourWeather to create its task to synchronize weather data
         * periodically.
         */
        schedulePeriodicWeatherSync(context);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG + " ###", "entered the run() method");
                /*URI for every row of weather data in our table
                 */
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                Log.i(TAG + " ###", "forecastQueryUri: " + forecastQueryUri);
                /*
                 * Since this query is going to be used only as a check to see if we have any
                 * data (rather than to display data), we just need to PROJECT the ID of each
                 * row. In our queries where we display data, we need to PROJECT more columns
                 * to determine what weather details need to be displayed.
                 */
                String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                String selectionStatement = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();
                /* Here, we perform the query to check to see if we have any weather data */
                Cursor cursor = context.getContentResolver().query(forecastQueryUri, projectionColumns, selectionStatement, null, null);
                /*
                 * A Cursor object can be null for various different reasons. A few are
                 * listed below.
                 *
                 *   1) Invalid URI
                 *   2) A certain ContentProvider's query method returns null
                 *   3) A RemoteException was thrown.
                 *
                 * Bottom line, it is generally a good idea to check if a Cursor returned
                 * from a ContentResolver is null.
                 *
                 * If the Cursor was null OR if it was empty, we need to sync immediately to
                 * be able to display data to the user.
                 */
                Log.i(TAG + " ###", "in run() cursor.getCount(): " + cursor.getCount());
                if (cursor == null || cursor.getCount() == 0)
                    KnowYourWeatherSyncTask.syncWeather(context);
                //Make sure to close the cursor.
                assert cursor != null;
                cursor.close();

            }
        });
    }

    //Add constant values to sync Sunshine every 3 - 4 hours
    /*
     * Interval at which to sync with the weather. Use TimeUnit for convenience, rather than
     * writing out a bunch of multiplication ourselves and risk making a silly mistake.
     */
    private static final int SYNC_INTERVAL_HOURS = 3;
    static final String SCHEDULE_WEATHER_SYNC = "schedule-weather-sync";
    static boolean issInitialized = false;

    //Create a method to schedule periodic weather sync
    private static void schedulePeriodicWeatherSync(@NonNull Context context) {
        Log.i(TAG + " ###@", "entered the schedulePeriodicWeatherSync method\nissInitialised: " + issInitialized);
        if (issInitialized) {
            Log.i(TAG+" ###@","return from issInitialized()");
            return;
        }

        Constraints constraint = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)//any network but connected
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ScheduleWeatherSyncJob.class,
                15, TimeUnit.MINUTES)
                .setConstraints(constraint)
                .build();
        WorkManager mWorkManager = WorkManager.getInstance(context.getApplicationContext());
        mWorkManager.enqueueUniquePeriodicWork(SCHEDULE_WEATHER_SYNC, ExistingPeriodicWorkPolicy.KEEP, request);
        Log.i(TAG + " ###", "returning from the schedulePeriodicWeatherSync()");
        issInitialized = true;
        Log.i(TAG + " ###", "issInitialized set to " + issInitialized + " now return from schedulePeriodicWorkRequest");
    }
}
