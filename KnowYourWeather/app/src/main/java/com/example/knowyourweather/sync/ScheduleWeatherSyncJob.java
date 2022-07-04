package com.example.knowyourweather.sync;

import android.app.Notification;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.knowyourweather.R;
import com.google.common.util.concurrent.ListenableFuture;

public class ScheduleWeatherSyncJob extends Worker {
    private static final String TAG=ScheduleWeatherSyncJob.class.getSimpleName();
    private static Context mContext;

    public ScheduleWeatherSyncJob(@NonNull android.content.Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext=context;
    }


    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to <b>synchronously</b> do your work and return the
     * {@link Result} from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
     * you need to do your work asynchronously on a thread of your own choice, see
     * {@link ListenableWorker}.
     * <p>
     * A Worker has a well defined
     * <a href="https://d.android.com/reference/android/app/job/JobScheduler">execution window</a>
     * to finish its execution and return a {@link Result}.  After
     * this time has expired, the Worker will be signalled to stop.
     *
     * @return The {@link Result} of the computation; note that
     * dependent work will not execute if you use
     * {@link Result#failure()} or
     * {@link Result#failure(Data)}
     */
    @NonNull
    @Override
    public Result doWork() {
        try
        {
            Log.i(TAG+" ####@","entered the try block on doWork()");
            KnowYourWeatherSyncTask.syncWeather(mContext);
        }
        catch (Exception e)
        {
            Log.e(TAG+" ###","error has occurred in doWork()");
            return Result.failure();
        }
        Log.i(TAG+" ###","returning from doWork()");
        //Toast.makeText(getApplicationContext(),"Scheduled Weather Sync Complete",Toast.LENGTH_LONG).show();
        return Result.success();
    }

    /**
     * This method is invoked when this Worker has been told to stop.  At this point, the
     * {@link ListenableFuture} returned by the instance of {@link #startWork()} is
     * also cancelled.  This could happen due to an explicit cancellation signal by the user, or
     * because the system has decided to preempt the task.  In these cases, the results of the
     * work will be ignored by WorkManager.  All processing in this method should be lightweight
     * - there are no contractual guarantees about which thread will invoke this call, so this
     * should not be a long-running or blocking operation.
     */
    @Override
    public void onStopped() {
        Log.i(TAG+" ###","entered the onStopped() method");
        WorkManager.getInstance(getApplicationContext()).cancelUniqueWork(KnowYourWeatherSync.SCHEDULE_WEATHER_SYNC);
        Log.i(TAG+" ###","SCHEDULE_WEATHER_SYNC cancelled in onStopped(), now return");
        super.onStopped();
    }
}
