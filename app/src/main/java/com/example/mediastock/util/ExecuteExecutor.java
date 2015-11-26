package com.example.mediastock.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Static executor class that execute a future class that handles async operations on the database
 */
public class ExecuteExecutor {
    private static ExecutorService executor;
    private FutureWork futureTask;

    public ExecuteExecutor(Context context, CallableAsyncTask callableAsyncTask) {
        executor = Executors.newSingleThreadExecutor();
        futureTask = new FutureWork(callableAsyncTask, context);

        // execute the task
        executor.execute(futureTask);
    }

    private static class FutureWork extends FutureTask<String> {
        private static Handler handler;
        private static WeakReference<Context> contextRef;

        public FutureWork(Callable<String> callable, final Context context) {
            super(callable);
            contextRef = new WeakReference<>(context);

            // handler to display a message on the UI (video added/removed from favorites)
            // after the thread finished the task
            handler = new Handler(Looper.getMainLooper()) {

                @Override
                public void handleMessage(Message inputMessage) {

                    String msg = (String) inputMessage.obj;
                    Toast.makeText(contextRef.get().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                    // clear resource
                    contextRef = null;
                }
            };
        }

        @Override
        protected void done() {
            super.done();

            String result = "";
            try {

                result = get(); // get resulting string

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            // send string to the handler
            Message message = handler.obtainMessage(1, result);
            message.sendToTarget();

            // after the thread finished the task, we clear the resources
            executor.shutdown();
        }
    }

    /**
     * Abstract runnable class
     */
    public static abstract class CallableAsyncTask implements Callable<String> {
        private static WeakReference<Activity> contextRef;

        public CallableAsyncTask(Activity context) {
            contextRef = new WeakReference<>(context);
        }

        public Activity getContextRef() {
            return contextRef.get();
        }

        @Override
        public abstract String call();
    }

}
