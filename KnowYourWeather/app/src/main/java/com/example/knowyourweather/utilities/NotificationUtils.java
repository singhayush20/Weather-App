package com.example.knowyourweather.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.example.knowyourweather.DetailsActivity;
import com.example.knowyourweather.R;
import com.example.knowyourweather.data.KnowYourWeatherPreferences;
import com.example.knowyourweather.data.WeatherContract;

public class NotificationUtils {
    /*
     * The columns of data that we are interested in displaying within our notification to let
     * the user know there is new weather data available.
     */
    public static final String[] WEATHER_NOTIFICATION_PROJECTION =
            {
                    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
            };
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;
    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 3004 is in no way significant.
     */
    private static final int WEATHER_NOTIFICATION_ID = 3004;
    private static final String TAG = NotificationUtils.class.getSimpleName();
    private static final String WEATHER_NOTIFICATION_CHANNEL_ID = "weather-notification-channel-id";

    /**
     * Constructs and displays a notification for the
     * newly updated weather for today.
     *
     * @param context: Context used to query our ContentProvider and use various Utility methods
     */
    public static void notifyUserOfWeather(Context context) {
        Log.i(TAG + " ###", "entered the notifyUserOfWeather");
        /*
        Build the URI for today's weather in order to show up to date
        in notification */
        Uri todaysWeatherUri = WeatherContract.WeatherEntry.buildWeatherUriWithDate(KnowYourWeatherDateUtils.normalizeDate(System.currentTimeMillis()));
         /*
         The MAIN_FORECAST_PROJECTION array passed in as the second parameter is defined in our WeatherContract
         * class and is used to limit the columns returned in our cursor.
         */
        Log.i(TAG + " ###", "querying the database for today's data," +
                "with URI: " + todaysWeatherUri + "and " + WEATHER_NOTIFICATION_PROJECTION);
        Cursor todayWeatherCursor = context.getContentResolver().query(
                todaysWeatherUri,
                WEATHER_NOTIFICATION_PROJECTION,
                null,
                null,
                null
        );
        /*
         * If todayWeatherCursor is empty, moveToFirst will return false. If our cursor is not
         * empty, we want to show the notification.
         */
        if (todayWeatherCursor.moveToFirst()) {
            int weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
            double high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP);
            double low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP);
            Log.i(TAG + " ###", "obtained values: weatherId: " + weatherId + " low:" + low + " high:" + high);
            Resources resources = context.getResources();
            int largeArtResourceId = KnowYourWeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId);
            Bitmap largeIcon = BitmapFactory.decodeResource(
                    resources,
                    largeArtResourceId);
            String notificationTitle = context.getString(R.string.app_name);
            String notificationText = getNotificationText(context, weatherId, high, low);
            /* getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID */
            int smallArtResourceId = KnowYourWeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId);

            /*
             * NotificationCompat Builder is a very convenient way to build backward-compatible
             * notifications. In order to use it, we provide a context and specify a color for the
             * notification, a couple of different icons, the title for the notification, and
             * finally the text of the notification, which in our case in a summary of today's
             * forecast.
             */
            //Use NotificationCompat.Builder to begin building the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WEATHER_NOTIFICATION_CHANNEL_ID)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true);
            Log.i(TAG + " ###", "notification created, now creating intent");
            //Create an Intent with the proper URI to start the DetailActivity
            /*
             * This Intent will be triggered when the user clicks the notification. In our case,
             * we want to open KnowYourWeather to the DetailActivity to display the newly updated weather.
             */
            Intent detailIntentForToday = new Intent(context, DetailsActivity.class);
            detailIntentForToday.setData(todaysWeatherUri);
            Log.i(TAG + " ###", "data set for detailIntentToday: " + todaysWeatherUri);
            Log.i(TAG + " ###", "creating TaskStackBuilder for building pending intent");
            //Use TaskStackBuilder to create the proper PendingIntent
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
            PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            // Set the content Intent of the NotificationBuilder
            builder.setContentIntent(resultPendingIntent);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //Set a notification channel for Android O devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                NotificationChannel mChannel = new NotificationChannel(
                        WEATHER_NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.main_notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(mChannel);
            }
            //If the build version is greater than or equal to JELLY_BEAN and less than OREO,
            // set the notification's priority to PRIORITY_HIGH.-->BACKWARD COMPATIBILITY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }
            //Notify the user with the ID WEATHER_NOTIFICATION_ID
            /* WEATHER_NOTIFICATION_ID allows you to update or cancel the notification later on */
            notificationManager.notify(WEATHER_NOTIFICATION_ID, builder.build());
            Log.i(TAG + " ###", "notificationManager.notify() called for id: " + WEATHER_NOTIFICATION_ID);
            //Save the time at which the notification occurred using KnowYourWeatherPreferences
            /*
             * Since we just showed a notification, save the current time. That way, we can check
             * next time the weather is refreshed if we should show another notification.
             */
            KnowYourWeatherPreferences.saveLastNotificationTime(context, System.currentTimeMillis());
        }
        /* Always close your cursor when you're done with it to avoid wasting resources. */
        todayWeatherCursor.close();
    }
    /**
     * Constructs and returns the summary of a particular day's forecast using various utility
     * methods and resources for formatting. This method is only used to create the text for the
     * notification that appears when the weather is refreshed.
     * <p>
     * The String returned from this method will look something like this:
     * <p>
     * Forecast: Sunny - High: 14°C Low 7°C
     *
     * @param context   Used to access utility methods and resources
     * @param weatherId ID as determined by Open Weather Map
     * @param high      High temperature (either celsius or fahrenheit depending on preferences)
     * @param low       Low temperature (either celsius or fahrenheit depending on preferences)
     * @return Summary of a particular day's forecast
     */
    /*
     * Short description of the weather, as provided by the API.
     * e.g "clear" vs "sky is clear".
     */
    private static String getNotificationText(Context context, int weatherId, double high, double low) {
        String shortDescription = KnowYourWeatherUtils
            .getStringForWeatherCondition(context, weatherId);

    String notificationFormat = context.getString(R.string.format_notification);

    /* Using String's format method, we create the forecast summary */
    String notificationText = String.format(notificationFormat,
            shortDescription,
            KnowYourWeatherUtils.formatTemperature(context, high),
            KnowYourWeatherUtils.formatTemperature(context, low));
    Log.i(TAG+" ###","returning Notification String from getNotificationText(): "+notificationText);
        return notificationText;
}
}

