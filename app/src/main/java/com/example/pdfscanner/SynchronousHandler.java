package com.example.pdfscanner;
import android.os.Handler;
import android.os.Looper;

/**
 * A helper class that provides more ways to post a runnable than {@link android.os.Handler}.
 *
 * Created by Petros Douvantzis on 19/6/2015.
 */
public class SynchronousHandler {

    private static class NotifyRunnable implements Runnable {
        private final Runnable mRunnable;
        private final Handler mHandler;
        private boolean mFinished = false;

        public NotifyRunnable(final Handler handler, final Runnable r) {
            mRunnable = r;
            mHandler = handler;
        }

        public boolean isFinished() {
            return mFinished;
        }

        @Override
        public void run() {
            synchronized (mHandler) {
                mRunnable.run();
                mFinished = true;
                mHandler.notifyAll();
            }
        }
    }

    public static void postAndWait(final Handler handler, final Runnable r) {

        if (handler.getLooper() == Looper.myLooper()) {
            r.run();
        } else {
            synchronized (handler) {
                NotifyRunnable runnable = new NotifyRunnable(handler, r);
                handler.post(runnable);
                while (!runnable.isFinished()) {
                    try {
                        handler.wait();
                    } catch (InterruptedException is) {
                        // ignore
                    }
                }
            }
        }
    }
}
