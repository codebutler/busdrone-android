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

package com.busdrone.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.busdrone.android.BusdroneApp;
import com.busdrone.android.event.ConnectionErrorEvent;
import com.busdrone.android.event.ConnectionStateEvent;
import com.busdrone.android.event.VehicleRemovedEvent;
import com.busdrone.android.event.VehicleUpdatedEvent;
import com.busdrone.android.event.VehiclesEvent;
import com.busdrone.android.model.Event;
import com.busdrone.android.model.Vehicle;
import com.codebutler.android_websockets.WebSocketClient;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;

import javax.inject.Inject;
import java.net.URI;
import java.util.Map;

public class BusdroneService extends Service implements WebSocketClient.Listener {
    private static final String TAG = "Busdrone-BusdroneService";

    private WebSocketClient mSocket = new WebSocketClient(URI.create("ws://busdrone.com:28737/"), this, null);

    private final IBinder mBinder = new LocalBinder();
    private final Map<String, Vehicle> mVehicles = Maps.newTreeMap();
    private int mConnectionState = ConnectionStateEvent.DISCONNECTED;

    @Inject Bus  mBus;
    @Inject Gson mGson;

    public class LocalBinder extends Binder {
        public BusdroneService getService() {
            return BusdroneService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();
        BusdroneApp.get().inject(this);
        mBus.register(this);
        mSocket.connect();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        mBus.unregister(this);
        mSocket.disconnect();
        mVehicles.clear();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onConnect() {
        Log.d(TAG, "onConnect()");
        mConnectionState = ConnectionStateEvent.CONNECTED;
        mBus.post(produceConnectionStateEvent());
    }

    @Override
    public void onDisconnect(int i, String s) {
        Log.d(TAG, "onDisconnect()");
        mConnectionState = ConnectionStateEvent.DISCONNECTED;
        mBus.post(produceConnectionStateEvent());
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "onError()", e);
        mBus.post(new ConnectionErrorEvent(e));
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage: " + message);

        Event event = mGson.fromJson(message, Event.class);
        if (event.getType().equals(Event.TYPE_INIT)) {
            synchronized (mVehicles) {
                mVehicles.clear();
                for (Vehicle vehicle : event.getVehicles()) {
                    mVehicles.put(vehicle.uid, vehicle);
                }
            }
            mBus.post(new VehiclesEvent(mVehicles));
        } else if (event.getType().equals(Event.TYPE_UPDATE_VEHICLE)) {
            Vehicle vehicle = event.getVehicle();
            synchronized (mVehicles) {
                mVehicles.put(vehicle.uid, vehicle);
            }
            mBus.post(new VehicleUpdatedEvent(vehicle));
        } else if (event.getType().equals(Event.TYPE_REMOVE_VEHICLE)) {
            String vehicleUid = event.getVehicleUid();
            synchronized (mVehicles) {
                mVehicles.remove(vehicleUid);
            }
            mBus.post(new VehicleRemovedEvent(vehicleUid));
        }
    }

    @Override
    public void onMessage(byte[] bytes) {
    }

    @Produce
    public ConnectionStateEvent produceConnectionStateEvent() {
        return new ConnectionStateEvent(mConnectionState);
    }

    @Produce
    public VehiclesEvent produceVehiclesEvent() {
        return new VehiclesEvent(mVehicles);
    }
}
