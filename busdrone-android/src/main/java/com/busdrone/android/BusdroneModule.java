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

import android.content.Context;
import com.busdrone.android.activity.MainActivity;
import com.busdrone.android.service.BusdroneService;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(
    injects = {
        BusdroneApp.class,
        BusdroneService.class,
        MainActivity.class
    }
)
public class BusdroneModule {
    private final Context mAppContext;

    public BusdroneModule(Context appContext) {
        mAppContext = appContext;
    }

    @Provides @Singleton
    public Bus provideBus() {
        return new AndroidBus();
    }

    @Provides @Singleton
    public Gson provideGson() {
        return new Gson();
    }
}
