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
    private final FutureWork futureTask;

    public ExecuteExecutor(Context context, int type, CallableAsyncTask callableAsyncTask) {
        executor = Executors.newSingleThreadExecutor();
        futureTask = new FutureWork(this, callableAsyncTask, context, type);

        // execute the task
        executor.execute(futureTask);
    }

    /**
     * Class to compute an asynchronous computation and to control the operation. It wraps a callable object.
     */
    private static class FutureWork extends FutureTask<String> {
        private static Handler handler;
        private final WeakReference<ExecuteExecutor> ref;
        private final int type;

        public FutureWork(ExecuteExecutor executeExecutor, Callable<String> callable, final Context context, int type) {
            super(callable);
            this.ref = new WeakReference<>(executeExecutor);
            this.type = type;

            // handler to display a message on the UI (video/music added/removed from favorites)
            // after the thread finished the task
            if (type == 1)
                handler = new MyHandler(Looper.getMainLooper(), context);
        }

        @Override
        protected void done() {
            super.done();

            if (type == 1) {

                String result = "";
                try {

                    result += get(); // get resulting message

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                // send message to the handler
                Message message = handler.obtainMessage(1, result);
                message.sendToTarget();
            }

            // after the thread finished the task, we close the eexecutor
            executor.shutdown();
        }
    }

    /**
     * Abstract callable class that computes a background operation.
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

    /**
     * Handler class to get the message from the background thread and to display a result message on the the main thread
     */
    private static class MyHandler extends Handler {
        private static WeakReference<Context> ref;

        public MyHandler(Looper looper, Context context) {
            super(looper);

            ref = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message inputMsg) {
            super.handleMessage(inputMsg);

            String msg = (String) inputMsg.obj;
            Toast.makeText(ref.get().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            // clear resource
            ref = null;
        }
    }

}
