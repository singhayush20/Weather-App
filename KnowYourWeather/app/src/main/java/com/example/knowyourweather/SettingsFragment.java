package com.example.knowyourweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.example.knowyourweather.data.KnowYourWeatherPreferences;
import com.example.knowyourweather.data.WeatherContract;
import com.example.knowyourweather.sync.KnowYourWeatherSync;
import com.example.knowyourweather.sync.KnowYourWeatherSyncTask;
import com.example.knowyourweather.utilities.AppExecutors;
import com.example.knowyourweather.utilities.KnowYourWeatherUtils;

/**
 * The SettingsFragment serves as the display for all of the user's settings. In KnowYourWeather, the
 * user will be able to change their preference for units of measurement from metric to imperial,
 * set their preferred weather location, and indicate whether or not they'd like to see
 * notifications.
 * <p>
 * Please note: If you are using our dummy weather services, the location returned will always be
 * Mountain View, California.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    //Override the onSharedPreferenceChanged listener to update the
    //non CheckBoxPreferences when they are changed
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Preference preference=findPreference(key);
//        if(preference!=null)
//        {
//            if(!(preference instanceof CheckBoxPreference))
//            {
//                setPreferenceSummary(preference,sharedPreferences.getString(key,""));
//            }
//        }
        Log.i(TAG+" ###","entered the onSharedPreferenceChanged method in SettingsFragment");
        Activity activity=getActivity();
        if(key.equals(getString(R.string.pref_location_key))) {
            Log.i(TAG + " ###", "entered the if condition in onSharedPreferenceChanged method in SettingsFragment");
//            if(sharedPreferences.getString(key,"").equals(""))
//            {
//                Toast.makeText(getContext(),"empty location not allowed!",Toast.LENGTH_LONG).show();
//                return;
//            }
            //If the location is changed, reset the location coordinates
            KnowYourWeatherPreferences.resetLocationCoordinates(activity);
            //Sync the weather data if the location has changed
            //KnowYourWeatherSync.startImmediateSync(activity);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG+" ###","entered run() in onSharedPreferenceChange");
                    KnowYourWeatherSyncTask.syncWeather(getContext());

                }
            });
        }
        else if (key.equals(getString(R.string.pref_units_key)))
        {
            //If units are changed, update the list of weather entries for new units
            assert activity != null;
            activity.getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI,null);
        }
        Preference preference=findPreference(key);
                if(preference!=null)
                {
                    if(!(preference instanceof CheckBoxPreference))
                        setPreferenceSummary(preference,sharedPreferences.getString(key,""));
                }
    }

    @Override
    public void onStart() {
        super.onStart();
        /*Register te preference change listener*/
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        /*Unregister the preference change listener*/
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
     * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
     * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     * @param rootKey            If non-null, this preference fragment should be rooted at the
     *                           {@link PreferenceScreen} with this key.
     */
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        /* Add 'general' preferences, defined in the XML file*/
        addPreferencesFromResource(R.xml.pref_general);
        //Set the preference summary on each preference that isn't a CheckBoxPreference
        SharedPreferences sharedPreferences= getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen=getPreferenceScreen();
        int count=prefScreen.getPreferenceCount();
        for(int i=0;i<count;i++)
        {
            Preference p=prefScreen.getPreference(i);
            if(!(p instanceof CheckBoxPreference)){
                assert sharedPreferences != null;
                String value=sharedPreferences.getString(p.getKey(),"");
                setPreferenceSummary(p,value);
            }
        }

    }
    //Create a method called setPreferenceSummary to set the
    //preference summaries.
    //It accepts a preference and Object and sets the summary of the preference
    private void setPreferenceSummary(Preference preference, String value) {
        String stringValue=value;
        Log.i(TAG+" ###","stringValue: "+stringValue);
        String key=preference.getKey();
        if(preference instanceof ListPreference) {
            //For list preference look up the correct display value in
            //the preference's entries list
            //They have separate labels and values
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
        else
            //For other preferences, set the summary to the value's simple string
        preference.setSummary(stringValue);
    }

}
