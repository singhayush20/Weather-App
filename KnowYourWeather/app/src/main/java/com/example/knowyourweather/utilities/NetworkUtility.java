package com.example.knowyourweather.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.knowyourweather.data.KnowYourWeatherPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * This utility class will be used to communicate with the
 * weather servers.
 */
public final class NetworkUtility {
    private static final String TAG = NetworkUtility.class.getName();
    private static final String DYNAMIC_WEATHER_URL = "https://andfun-weather.udacity.com/weather";
    private static final String STATIC_WEATHER_URL = "https://andfun-weather.udacity.com/staticweather";
    private static final String BASE_WEATHER_URI_LAT_LON="https:api.openweathermap.org/data/2.5/onecall";
    private static final String BASE_WEATHER_URI_LOC="http://api.openweathermap.org/data/2.5/weather";
    private static final String apiID="REPLACE THIS TEXT WITH THE API KEY;
    private static final String FORECAST_BASE_URL = STATIC_WEATHER_URL;

    /*
     * NOTE: These values only effect responses from OpenWeatherMap, NOT from the fake weather
     * server. They are simply here to allow us to teach you how to build a URL if you were to use
     * a real API.If you want to connect your app to OpenWeatherMap's API, feel free to! However,
     * we are not going to show you how to do so in this course.
     */
    /* The format we want our API to return */
    private static final String format = "json";
    /* The units we want our API to return */
    private static final String units = "metric";
    /* The number of days we want our API to return */
    private static final int numDays = 14;
    final static String QUERY_PARAM = "q";
    final static String LAT_PARAM = "lat";
    final static String LON_PARAM = "lon";
    final static String FORMAT_PARAM = "mode";
    final static String UNITS_PARAM = "units";
    final static String DAYS_PARAM = "cnt";
    private static String API_ID_param="appid";

    public static URL getUrl(Context context)
    {
        if(KnowYourWeatherPreferences.isLocationLatLonAvailable(context)) {
            double[] preferredCoordinates = KnowYourWeatherPreferences.getLocationCoordinates(context);
            double latitude = preferredCoordinates[0];
            double longitude=preferredCoordinates[1];//earlier 0 was set
            Log.i(TAG+" ###","Location cordinates are available, latitude:  "+latitude+" longitude: "+longitude);

            return buildUrlWithLatitudeLongitude(latitude,longitude);
        }
        else
        {
            Log.i(TAG+" ###","Location cordinates not available: ");
            String locationQuery = KnowYourWeatherPreferences.getPreferredWeatherLocation(context);
            Log.i(TAG+" ###","Location coordinates not available: location query is: "+locationQuery);

            return buildUrlWithLocationQuery(locationQuery);

        }
    }
/*---------------------------------------------------------------------------------------*/
    /**
     * Build the URL for fetching the weather data
     *
     * @param query : The keyword that will be queried
     * @return : returns the URL built from the string
     */
    public static URL buildUrlWithLocationQuery(String query) {
        Log.i(TAG+" ### ","query: in buildUrlWithLocationQuery is "+query);
        Uri buildUrl = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, query)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .build();
//        Uri buildUrl=Uri.parse(BASE_WEATHER_URI_LOC).buildUpon()
//                .appendQueryParameter(QUERY_PARAM,query)
//                .appendQueryParameter(FORMAT_PARAM,format)
//                .appendQueryParameter(UNITS_PARAM,units)
//                .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
//                .appendQueryParameter(API_ID_param,apiID)
//                .build();
        URL url = null;
        try {
            url = new URL(buildUrl.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.i("###","Url built: "+url);
        Log.i(TAG+" ###","returning url: "+url);
        return url;
    }
    /**
     * Builds the URL need to talk to the weather server using latitude
     * and longitude of a location
     */
    public static URL buildUrlWithLatitudeLongitude(Double latitude, Double longitude)
    {
        Uri weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(LAT_PARAM, String.valueOf(latitude))
                .appendQueryParameter(LON_PARAM, String.valueOf(longitude))
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .build();
//        Uri weatherQueryUri=Uri.parse(BASE_WEATHER_URI_LAT_LON).buildUpon()
//                .appendQueryParameter(LAT_PARAM, String.valueOf(latitude))
//                .appendQueryParameter(LON_PARAM, String.valueOf(longitude))
//                .appendQueryParameter(FORMAT_PARAM, format)
//                .appendQueryParameter(UNITS_PARAM, units)
//                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                .appendQueryParameter(API_ID_param,apiID)
//                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "URL: " + weatherQueryUrl);
            Log.i(TAG+" ###","returning url: "+weatherQueryUrl);

            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
/*------------------------------------------------------------------------------------------------------*/
    /**
     * This method returns the entire result from the HTTP response
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        Log.i("###","entered the getResponseFromHttpUrl method and connection object obtained");
        try {
            InputStream input = urlConnection.getInputStream();
            Scanner sc = new Scanner(input);
            sc.useDelimiter("\\A");
            boolean hasInput = sc.hasNext();
            Log.i(TAG+" ###","hasInput: "+hasInput);
            if (hasInput) {
                return sc.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
            Log.i("###","returning from the connection function");

        }
    }

}
