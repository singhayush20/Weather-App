package com.example.knowyourweather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knowyourweather.utilities.KnowYourWeatherDateUtils;
import com.example.knowyourweather.utilities.KnowYourWeatherUtils;


//Add a class WeatherAdapter
//Extend RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherAdapterViewHolder> {
    private static final String TAG =WeatherAdapter.class.getSimpleName() ;
    //Declare a private final Context variable
    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private Cursor mCursor;

    //Declare constant IDs for the ViewType for today and for
    //a future day
    private static final int VIEW_TYPE_TODAY=0;
    private static final int VIEW_TYPE_FUTURE_DAY=1;

    //Declare a private boolean called mUseTodayLayout
    private boolean mUseTodayLayout;


    /*
    Create a final private WeatherAdapterOnClickHandler called
    mClickHandler
    An on-Click handler that we've defined to make it easy for an activity
    to inflate with our RecyclerView
     */
    private final WeatherAdapterOnClickHandler mClickHandler;

    /*
    Add an interface called WeatherAdapterOnClickHandler
    Within this, define a void method that access the string as a parameter
    Receives onClick Messages
     */
    public interface WeatherAdapterOnClickHandler {
        void onClick(long date);
    }

    /**
     * Creates a WeatherAdapter.
     * @param context Used to talk to the UI and app resources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public WeatherAdapter(Context context, WeatherAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
        mContext = context;
        mUseTodayLayout=mContext.getResources().getBoolean(R.bool.use_today_layout);
    }

    //Create a class called WeatherAdapterViewHolder
    public class WeatherAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //Create a final TextView view
        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;

        // Add an ImageView for the weather icon
        final ImageView iconView;
        /**
         * Constructor accepts a view as a parameter
         * Call super(view)
         * Using view.findViewById, get a reference to this layout's
         * TextView and save it to mWeatherTextView
         *
         * @param view: view
         */
        public WeatherAdapterViewHolder(@NonNull View view) {
            super(view);
            //Get the references to all the views
            iconView = (ImageView) view.findViewById(R.id.weather_icon);
            dateView = (TextView) view.findViewById(R.id.date);
            descriptionView = (TextView) view.findViewById(R.id.weather_description);
            highTempView = (TextView) view.findViewById(R.id.high_temperature);
            lowTempView = (TextView) view.findViewById(R.id.low_temperature);
            //Call the setOnClickListener on the View passed into the constructor
            //use 'this' as the onClickListener
            view.setOnClickListener(this);
        }

        /**
         * Override the onClick() method, pass the clicked days's
         * data to mClickhandler via its onClick() method
         * This gets called by the child views during a click.
         * We fetch the date that has been selected, and then call the onClick handler registered with this adapter, passing that
         *
         * @param view: The view that was clicked
         */

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            //pass the date from the cursor
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis=mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }
    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link RecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link RecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override { #onBindViewHolder(RecyclerView.ViewHolder, int)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.WeatherAdapterViewHolder holder, int position) {
        //Move the cursor to the appropriate position
        mCursor.moveToPosition(position);


        /*******************
         * Weather Icon *
         *******************/
        int weatherId=mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        Log.i(TAG+" ###","weatherId: "+weatherId);
        int weatherImageId;

        int viewType=getItemViewType(position);
        Log.i(TAG+" ###","viewType obtained: "+viewType+" for position: "+position);
        switch(viewType)
        {
            case VIEW_TYPE_TODAY:
                Log.i(TAG+" ###","setting viewType today: weatherId: "+weatherId);
                weatherImageId = KnowYourWeatherUtils
                        .getLargeArtResourceIdForWeatherCondition(weatherId);
                break;
            case VIEW_TYPE_FUTURE_DAY:
                Log.i(TAG+" ###","setting ViewType future day: weatherId: "+weatherId);
                weatherImageId = KnowYourWeatherUtils
                        .getSmallArtResourceIdForWeatherCondition(weatherId);
                break;
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }
        Log.i(TAG+" ###","weatherImageId: "+weatherImageId);
        holder.iconView.setImageResource(weatherImageId);

        //Read the data from the cursor
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        /* Get human readable string using our utility method */
        String dateString = KnowYourWeatherDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        holder.dateView.setText(dateString);
//        /* Use the weatherId to obtain the proper description */
//        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        /***********************
         * Weather Description *
         ***********************/
        String description = KnowYourWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = mContext.getString(R.string.a11y_forecast, description);

        /* Set the text and content description (for accessibility purposes) */
        holder.descriptionView.setText(description);
        holder.descriptionView.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = KnowYourWeatherUtils.formatTemperature(mContext, highInCelsius);
        /* Create the accessibility (a11y) String from the weather description */
        String highA11y = mContext.getString(R.string.a11y_high_temp, highString);

        /* Set the text and content description (for accessibility purposes) */
        holder.highTempView.setText(highString);
        holder.highTempView.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/

        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String lowString = KnowYourWeatherUtils.formatTemperature(mContext, lowInCelsius);
        String lowA11y = mContext.getString(R.string.a11y_low_temp, lowString);

        /* Set the text and content description (for accessibility purposes) */
        holder.lowTempView.setText(lowString);
        holder.lowTempView.setContentDescription(lowA11y);


    }

    /**
     * Called when RecyclerView needs a new {@link RecyclerView.ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * { #onBindViewHolder(RecyclerView.ViewHolder, int)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @NonNull
    @Override
    public WeatherAdapter.WeatherAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        switch(viewType)
        {
            case VIEW_TYPE_TODAY:
            {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.forecast_list_item;
                break;
            }
            default: throw new IllegalArgumentException("Invalid view type, value of "+viewType);
        }
        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        view.setFocusable(true);
        return new WeatherAdapterViewHolder(view);
    }


    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if (mCursor == null)
            return 0;
        else
            return mCursor.getCount();
    }

    /**
     * Returns an integer code related to the type of View we want the ViewHolder to be at a given
     * position. This method is useful when we want to use different layouts for different items
     * depending on their position. In Sunshine, we take advantage of this method to provide a
     * different layout for the "today" layout. The "today" layout is only shown in portrait mode
     * with the first item in the list.
     *
     * @param position index within our RecyclerView and Cursor
     * @return the view type (today or future day)
     */

    @Override
    public int getItemViewType(int position) {
        Log.i(TAG+" ###","position passed in getItemViewType: "+position);
//      Within getItemViewtype, if mUseTodayLayout is true and position is 0, return the ID for today viewType
        if (mUseTodayLayout && position == 0) {
            return VIEW_TYPE_TODAY;
//       Otherwise, return the ID for future day viewType
        } else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }
    /**
     * This method saves the weatherData to mWeatherData
     * Call notifyDataSetChanged() method
     * @param weatherData
     */
    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor The new cursor to use as WeatherAdapter's data source
     *
     */
    @SuppressLint("NotifyDataSetChanged")
    public void swapCursor(Cursor newCursor)
    {
        mCursor=newCursor;
        //After the new Cursor is set, call notifyDataSetChanged

        notifyDataSetChanged();
    }

}
