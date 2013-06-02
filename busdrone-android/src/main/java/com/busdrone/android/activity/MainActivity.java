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

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;
import com.busdrone.android.BusdroneApp;
import com.busdrone.android.R;
import com.busdrone.android.event.ConnectionErrorEvent;
import com.busdrone.android.event.ConnectionStateEvent;
import com.busdrone.android.event.VehicleRemovedEvent;
import com.busdrone.android.event.VehicleUpdatedEvent;
import com.busdrone.android.event.VehiclesEvent;
import com.busdrone.android.model.Vehicle;
import com.busdrone.android.ui.VehicleMarkerRenderer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.squareup.otto.Subscribe;

import java.util.Map;

public class MainActivity extends BusdroneActivity {
    private static final String TAG = "Busdrone-MainActivity";

    private GoogleMap             mMap;
    private boolean               mMapLoaded;
    private BiMap<String, Marker> mMarkers = HashBiMap.create();
    private Map<String, Vehicle> mVehicles = Maps.newHashMap();
    private VehicleMarkerRenderer mMarkerRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        mMarkerRenderer = new VehicleMarkerRenderer(this);

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        final SharedPreferences prefs = BusdroneApp.get().getPreferences();
        if (prefs.contains(BusdroneApp.PREF_MAP_LAT) && prefs.contains(BusdroneApp.PREF_MAP_LNG) && prefs.contains(BusdroneApp.PREF_MAP_ZOOM)) {
            double lat  = Double.longBitsToDouble(prefs.getLong(BusdroneApp.PREF_MAP_LAT, -1));
            double lng  = Double.longBitsToDouble(prefs.getLong(BusdroneApp.PREF_MAP_LNG, -1));
            float  zoom = prefs.getFloat(BusdroneApp.PREF_MAP_ZOOM,  -1);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
            mMapLoaded = true;
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.615, -122.330), 10));
        }

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (!mMapLoaded) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    mMapLoaded = true;
                }
            }
        });

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(BusdroneApp.PREF_MAP_LAT, Double.doubleToLongBits(cameraPosition.target.latitude));
                editor.putLong(BusdroneApp.PREF_MAP_LNG, Double.doubleToLongBits(cameraPosition.target.longitude));
                editor.putFloat(BusdroneApp.PREF_MAP_ZOOM, cameraPosition.zoom);
                editor.apply();
            }
        });

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String uid = mMarkers.inverse().get(marker);
                Vehicle vehicle = mVehicles.get(uid);
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(vehicle.toString())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            }
        });
    }

    @Override
    protected void onResume() {
        // FIXME: The map will re-populate markers but there's no way to get a reference to them!
        mMarkers.clear();
        mVehicles.clear();
        mMap.clear();

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/codebutler/busdrone-android")));
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onConnectionState(ConnectionStateEvent event) {
        Log.d(TAG, "onConnectionState: " + event.getState());
        if (event.getState() == ConnectionStateEvent.CONNECTED) {
            setProgressBarIndeterminateVisibility(false);
        } else if (event.getState() == ConnectionStateEvent.DISCONNECTED) {
            setProgressBarIndeterminateVisibility(true);
            mMap.clear();
        }
    }

    @Subscribe
    public void onConnectionError(ConnectionErrorEvent event) {
        Log.e(TAG, "onConnectionError", event.getError());
        Toast.makeText(this, event.getError().toString(), Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onVehicles(VehiclesEvent event) {
        Log.d(TAG, "onVehicles: " + event.getVehicles());

        mMap.clear();
        for (Vehicle vehicle : ImmutableList.copyOf(event.getVehicles().values())) {
            addOrUpdateMarker(vehicle);
        }
    }

    @Subscribe
    public void onVehicleUpdated(VehicleUpdatedEvent event) {
        Log.d(TAG, "onVehicleUpdated: " + event.getVehicle());
        addOrUpdateMarker(event.getVehicle());
    }

    @Subscribe
    public void onVehicleRemoved(VehicleRemovedEvent event) {
        String vehicleUid = event.getVehicleUid();
        Log.d(TAG, "onVehicleRemoved: " + vehicleUid);
        if (mMarkers.containsKey(vehicleUid)) {
            mMarkers.get(vehicleUid).remove();
            mVehicles.remove(vehicleUid);
        }
    }

    private void addOrUpdateMarker(Vehicle vehicle) {
        LatLng position = new LatLng(vehicle.lat, vehicle.lon);

        Marker marker = mMarkers.get(vehicle.uid);
        if (marker == null) {
            /*
            Matrix matrix = new Matrix();
            matrix.postRotate((float) vehicle.heading);

            Bitmap markerBitmap = null;
            if (vehicle.vehicleType != null && vehicle.vehicleType.equals(Vehicle.VEHICLE_TYPE_BUS)) {
                markerBitmap = mBusMarkerBitmap;
            } else {
                // FIXME: ...
            }
            if (markerBitmap != null) {
                Bitmap rotatedMarkerBitmap = Bitmap.createBitmap(markerBitmap, 0, 0, markerBitmap.getWidth(), markerBitmap.getHeight(), matrix, true);
                marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.fromBitmap(rotatedMarkerBitmap))
                    .title(vehicle.getDisplayTitle()));
            } else {
                marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(vehicle.getDisplayTitle()));
            }
            */

            marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromBitmap(mMarkerRenderer.get(vehicle.color, vehicle.route)))
                .title(vehicle.getDisplayTitle()));

            mMarkers.put(vehicle.uid, marker);
            mVehicles.put(vehicle.uid, vehicle);

        } else {
            LatLngBounds visibleBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            if (visibleBounds.contains(position)) {
                animateMarker(marker, position, false);
            } else {
               marker.setPosition(position);
            }
        }
    }

    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
}
