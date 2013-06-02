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

package com.busdrone.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.busdrone.android.BusdroneApp;
import com.busdrone.android.service.BusdroneService;
import com.busdrone.android.service.DummyServiceConnection;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public abstract class BusdroneActivity extends Activity implements DummyServiceConnection.Listener {
    private static final String TAG = "Busdrone-BusdroneActivity";

    private DummyServiceConnection mServiceConnection;

    @Inject protected Bus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusdroneApp.get().inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onStart() {
        super.onStart();
        mServiceConnection = (DummyServiceConnection) getLastNonConfigurationInstance();
        if (mServiceConnection == null) {
            mServiceConnection = new DummyServiceConnection();
            mServiceConnection.setListener(this);
            BusdroneApp.get().bindService(new Intent(this, BusdroneService.class), mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object onRetainNonConfigurationInstance() {
        return mServiceConnection;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mServiceConnection != null && (!isChangingConfigurations())) {
            BusdroneApp.get().unbindService(mServiceConnection);
            mServiceConnection = null;
        }
    }

    @Override
    public void onServiceConnected(BusdroneService service) {
    }

    @Override
    public void onServiceDisconnected(BusdroneService service) {
        Log.d(TAG, "onServiceDisconnected!!!");
        mServiceConnection = null;
        finish();
    }
}
