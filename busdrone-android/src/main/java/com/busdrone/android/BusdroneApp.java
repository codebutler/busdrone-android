/*
 * Copyright (C) 2013 Eric Butler
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

package com.busdrone.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Bus;
import dagger.ObjectGraph;

import javax.inject.Inject;

public class BusdroneApp extends Application {
    public static final String PREF_MAP_LAT  = "main_map_lat";
    public static final String PREF_MAP_LNG  = "main_map_lng";
    public static final String PREF_MAP_ZOOM = "main_map_zoom";

    private static BusdroneApp sInstance;

    private ObjectGraph       mObjectGraph;
    private SharedPreferences mPreferences;

    @Inject Bus mBus;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        Crashlytics.start(this);

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        }

        mObjectGraph = ObjectGraph.create(new BusdroneModule(this));
        mObjectGraph.inject(this);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mBus.register(this);
    }

    public static BusdroneApp get() {
        return sInstance;
    }

    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    public void inject(Object object) {
        mObjectGraph.inject(object);
    }
}
