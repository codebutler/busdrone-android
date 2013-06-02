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

package com.busdrone.android.model;

import android.text.TextUtils;

public class Vehicle {
    public static final String VEHICLE_TYPE_BUS       = "bus";
    public static final String VEHICLE_TYPE_FERRY     = "ferry";
    public static final String VEHICLE_TYPE_STREETCAR = "streetcar";
    public static final String VEHICLE_TYPE_TRAIN     = "train";
    public static final String VEHICLE_TYPE_LRV       = "lrv";

    public String uid;
    public String provider;
    public String vehicleType;
    public String vehicleId;
    public String prevStop;
    public String nextStop;
    public String coach;
    public String name;
    public String routeId;
    public String route;
    public String tripId;
    public String destination;
    public String color;
    public int speed;
    public int speedKmh;
    public double lat;
    public double lon;
    public double heading;
    public boolean inService; // XXX
    public long timestamp;
    public long age;

    public String getDisplayTitle() {
        if (!TextUtils.isEmpty(destination)) {
            return String.format("%s - %s", route, destination);
        } else {
            return route;
        }
    }

    @Override
    public String toString() {
        return "Vehicle{" +
            "uid='" + uid + '\'' +
            ", provider='" + provider + '\'' +
            ", vehicleType='" + vehicleType + '\'' +
            ", vehicleId='" + vehicleId + '\'' +
            ", prevStop='" + prevStop + '\'' +
            ", nextStop='" + nextStop + '\'' +
            ", coach='" + coach + '\'' +
            ", name='" + name + '\'' +
            ", routeId='" + routeId + '\'' +
            ", route='" + route + '\'' +
            ", tripId='" + tripId + '\'' +
            ", destination='" + destination + '\'' +
            ", color='" + color + '\'' +
            ", speed=" + speed +
            ", speedKmh=" + speedKmh +
            ", lat=" + lat +
            ", lon=" + lon +
            ", heading=" + heading +
            ", inService=" + inService +
            ", timestamp=" + timestamp +
            ", age=" + age +
            '}';
    }
}
