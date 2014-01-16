/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.dakko.glass.scouter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import us.dakko.glass.scouter.R;

/**
 * View used to display draw a running Chronometer.
 *
 * This code is greatly inspired by the Android's Chronometer widget.
 */
public class ScouterView extends FrameLayout {

    /**
     * Interface to listen for changes on the view layout.
     */
    public interface ChangeListener {
        /** Notified of a change in the view. */
        public void onChange();
    }

    // About 24 FPS.
    private static final long DELAY_MILLIS = 41;

    private final TextView mView;
    private boolean mStarted;
    private boolean mForceStart;
    private boolean mVisible;
    private boolean mRunning;

    private long mBaseMillis;

    private ChangeListener mChangeListener;

    public ScouterView(Context context) {
        this(context, null, 0);
    }

    public ScouterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScouterView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.card_chronometer, this);

        mView = (TextView) findViewById(R.id.mainview);
        setBaseMillis(SystemClock.elapsedRealtime());
    }

    /**
     * Set the base value of the chronometer in milliseconds.
     */
    public void setBaseMillis(long baseMillis) {
        mBaseMillis = baseMillis;
        updateText();
    }

    /**
     * Get the base value of the chronometer in milliseconds.
     */
    public long getBaseMillis() {
        return mBaseMillis;
    }

    /**
     * Set a {@link ChangeListener}.
     */
    public void setListener(ChangeListener listener) {
        mChangeListener = listener;
    }

    /**
     * Set whether or not to force the start of the chronometer when a window has not been attached
     * to the view.
     */
    public void setForceStart(boolean forceStart) {
        mForceStart = forceStart;
        updateRunning();
    }

    /**
     * Start the chronometer.
     */
    public void start() {
        mStarted = true;
        updateRunning();
    }

    /**
     * Stop the chronometer.
     */
    public void stop() {
        mStarted = false;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = (visibility == VISIBLE);
        updateRunning();
    }


    private final Handler mHandler = new Handler();

    private final Runnable mUpdateTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRunning) {
                updateText();
                mHandler.postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
            }
        }
    };

    /**
     * Update the running state of the chronometer.
     */
    private void updateRunning() {
        boolean running = (mVisible || mForceStart) && mStarted;
        if (running != mRunning) {
            if (running) {
                mHandler.post(mUpdateTextRunnable);
            } else {
                mHandler.removeCallbacks(mUpdateTextRunnable);
            }
            mRunning = running;
        }
    }

    /**
     * Update the value of the chronometer.
     */
    private void updateText() {
    	long millis = SystemClock.elapsedRealtime() - mBaseMillis;
        millis %= TimeUnit.HOURS.toMillis(1);
        millis %= TimeUnit.MINUTES.toMillis(1);
        mView.setText(Long.toString(9001));
        mView.setTextSize(110.0f);
        mView.setTextColor(Color.RED);
        if (TimeUnit.MILLISECONDS.toSeconds(millis) > 2) {
        	Service context = (Service)getContext();
        	context.stopSelf();
        }
        if (mChangeListener != null) {
            mChangeListener.onChange();
        }

    }
}
