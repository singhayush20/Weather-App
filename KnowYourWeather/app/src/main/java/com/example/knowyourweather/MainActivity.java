package com.example.knowyourweather;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knowyourweather.data.KnowYourWeatherPreferences;
import com.example.knowyourweather.data.WeatherContract;
import com.example.knowyourweather.sync.KnowYourWeatherSync;
import com.example.knowyourweather.utilities.FakeDataUtils;

public class MainActivity extends AppCompatActivity implements
        WeatherAdapter.WeatherAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MainActivity.class.getSimpleName();
    //Add a private static boolean flag for
    // preference updates and initialize it to false
    private RecyclerView mRecyclerView;
    private WeatherAdapter mWeatherAdapter;
    private ProgressBar mLoadingIndicatorView;
    private int mPosition = RecyclerView.NO_POSITION;

    /*Create a ID Constant for the Loader*/
    private static final int Weather_Loader_ID = 11;


    /*
    Create a string array containing the names of the desired date columns from
    our ContentProvider
    The columns of data that we are interested in displaying within our
    MainActivity's list of weather data.
     */
    public static final String[] MAIN_FORECAST_PROJECTION =
            {WeatherContract.WeatherEntry.COLUMN_DATE,
                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID};
    // Create constant int values representing each column name's position above
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    //Get a handle on the menu inflater using the
//AppCompatActivity's getMenuInflater() method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Get a handle on the menu inflater using the
        //AppCompatActivity's getMenuInflater() method
        MenuInflater inflater = getMenuInflater();
        //Use the inflater's inflate() method to inflate the
        //menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        //Return true so that the menu is displayed in the toolbar
        return true;
    }

    //To handle the Refresh event
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            //Clear and load new data
            //mWeatherTextView.setText("");
            //instead use
            //Set the adapter to null before refreshing
            mWeatherAdapter.swapCursor(null);
            invaliddateData();
            LoaderManager loaderManager = LoaderManager.getInstance(this);
            loaderManager.restartLoader(Weather_Loader_ID, null, this);
            return true;
        } else if (id == R.id.action_map) {
            openLocationInMap();
            return true;
        } else if (id == R.id.action_settings) {
            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        getSupportActionBar().setElevation(0f);
        //Load fake data
        //This will be removed once we sync with live data
        //FakeDataUtils.insertFakeData(this);
        /*
        Using findViewById, get the reference of the RecyclerView from xml.
        This will allow us to set the adapter and the toggle the visibility.
         */
        mRecyclerView = findViewById(R.id.recyclerview_forecast);
        //TextView to display error if no data, disappears otherwise
        /*
        Create a LayoutManager (LinearLayoutManager with vertical orientation
        and shouldReverseLayout==false
        LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
        parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
        languages.
         */
        LinearLayoutManager layoutmanager = new LinearLayoutManager
                (this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutmanager);
        /*
        Use setHasFixedSize(true)on mRecyclerView to designate that all items in the list will have the same size
        Use this setting to improve performance if you know that changes in content do not
        change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);
        //Set the WeatherAdapter which is responsible for linking
        //our weather data with the Views that will end up displaying our weather data.
        mWeatherAdapter = new WeatherAdapter(this, this);
        mRecyclerView.setAdapter(mWeatherAdapter);
        mRecyclerView.setVisibility(View.VISIBLE);
        //TextView to display loading icon, when loading data
        //Hidden when no data loads
        mLoadingIndicatorView = findViewById(R.id.progress_icon_view);
        showLoading();
        /*
         * This ID will uniquely identify the Loader. We can use it, for example, to get a handle
         * on our Loader at a later point in time through the support LoaderManager.
         */
        int loaderID = Weather_Loader_ID;
        /*
         *From the MainActivity, we have implemented the LoaderCallbacks interface with the type of
         * String array. (implements LoaderCallbacks<String[]>) The variable callback is passed
         * to the call to initLoader below. This means that whenever the loaderManager has
         * something to notify us of, it will do so through this callback.
         */
        LoaderManager.LoaderCallbacks<Cursor> callback = MainActivity.this;
        /*
         * The second parameter of the initLoader method below is a Bundle. Optionally, you can
         * pass a Bundle to initLoader that you can then access from within the onCreateLoader
         * callback. In our case, we don't actually use the Bundle, but it's here in case we wanted
         * to.
         */
        Bundle bundleForLoader = null;
        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        LoaderManager loaderManager = LoaderManager.getInstance(this);
        loaderManager.initLoader(loaderID, bundleForLoader, callback);

        //Call KnowYourWeather's startImmediateSync method to sync the data
        Log.i(TAG+" ###","call KnowYourWeatherSync.initialize()");
        //KnowYourWeatherSync.startImmediateSync(this);
        //Instead call initialized() to ensure that sync is not done everytime
        //the app is opened.
        KnowYourWeatherSync.initialize(this);

    }
    // Create a method that will get the user's preferred location and execute your new AsyncTask and call it loadWeatherData
    private void showWeatherDataView() {
        Log.i(TAG+" ###","entered the showWeatherDataView");
        //Make sure, the error is invisible
        mLoadingIndicatorView.setVisibility(View.INVISIBLE);
        //Make sure, the weather data is visible
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    //Create a method called showLoading that shows
    //the loading indicator and hides the data
    private void showLoading() {
        mRecyclerView.setVisibility((View.INVISIBLE));
        mLoadingIndicatorView.setVisibility(View.VISIBLE);
    }

    /**
     * Override the onClick method of the WeatherAdapterOnClickHandler interface
     * Show a toast when an item is clicked, displaying item's weather data
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param date: The weather for the day we click on.
     */

    @Override
    public void onClick(long date) {
        Intent weatherDetailIntent = new Intent(MainActivity.this, DetailsActivity.class);
//      COMPLETED (39) Refactor onClick to pass the URI for the clicked date with the Intent
        Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
        weatherDetailIntent.setData(uriForDateClicked);
        startActivity(weatherDetailIntent);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * <p>This will always be called from the process's main thread.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.i("###","entered the onCreateLoader() method in MainActivity");
        //Remove the onStartLoading method, loadInBackground and deliver results.
        switch (id) {
            //If the loader requested is our forecast loader,
            // return the appropriate CursorLoader
            case Weather_Loader_ID:
                /*
                URI for all rows of weather data in our weather table*/
                /*Sort order: Ascending by date*/
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all weather data from today onwards that is stored in our weather table.
                 * We created a handy method to do that in our WeatherEntry class.
                 */
                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();
                return new androidx.loader.content.CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection, null, sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented " + id);

        }

    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     *
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     *
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * <p>This will always be called from the process's main thread.
     *  @param loader The Loader that has finished.
     *
     * @param data The data generated by the Loader.
     */
    /* Called when a Loader has finished loading its data.
     *
     * NOTE: There is one small bug in this code. If no data is present in the cursor do to an
     * initial load being performed with no access to internet, the loading indicator will show
     * indefinitely, until data is present from the ContentProvider. This will be fixed in a
     * future version of the course.*/
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        //Call mForecastAdapter's swapCursor method and pass in the new Cursor
        mWeatherAdapter.swapCursor(data);
        //If mPosition equals RecyclerView.NO_POSITION, set it to 0
        if (mPosition == RecyclerView.NO_POSITION)
            mPosition = 0;
        //Smooth scroll the RecyclerView to mPosition
        mRecyclerView.smoothScrollToPosition(mPosition);
        Log.i("###","data.getCount() in onLoadFinished(): "+data.getCount());
        if (data.getCount() != 0)
            showWeatherDataView();
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * <p>This will always be called from the process's main thread.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        /*
         * We aren't using this method in our example application, but we are required to Override
         * it to implement the LoaderCallbacks<String> interface
         */
    }

    /**
     * This method is used when we are resetting data, so that at one point in time during a
     * refresh of our data, you can see that there is no data showing.
     */
    private void invaliddateData() {
        //Call mForecastAdapter's swapCursor method and pass in null
        mWeatherAdapter.swapCursor(null);

    }


    /**
     * This method uses the URI scheme for showing a location found on a
     * map. This super-handy intent is detailed in the "Common Intents"
     * page of Android's developer site:
     *
     * @see <a"http://developer.android.com/guide/components/intents-common.html#Maps">
     * <p>
     * Hint: Hold Command on Mac or Control on Windows and click that link
     * to automagically open the Common Intents page
     */
    private void openLocationInMap() {
        //String addressString = "Maharshi Puram Colony, Sikandra, Agra";
        String addressString = KnowYourWeatherPreferences.getPreferredWeatherLocation(this);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("geo").path("0,0").appendQueryParameter("q", addressString);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri addressUri = builder.build();
        Log.e("### ", addressUri.toString());
        intent.setData(addressUri);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
        else
            Log.e("### ", "Couldn't call " + addressUri + " no apps installed!");
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG+" ####@","onDestroy() called");
        super.onDestroy();
    }
}