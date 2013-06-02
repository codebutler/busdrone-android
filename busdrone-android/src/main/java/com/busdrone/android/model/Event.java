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

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Event {
    public static final String TYPE_INIT           = "init";
    public static final String TYPE_UPDATE_VEHICLE = "update_vehicle";
    public static final String TYPE_REMOVE_VEHICLE = "remove_vehicle";

    @SerializedName("type")        private String        mType;
    @SerializedName("vehicles")    private List<Vehicle> mVehicles;
    @SerializedName("vehicle")     private Vehicle       mVehicle;
    @SerializedName("vehicle_uid") private String        mVehicleUid;

    public String getType() {
        return mType;
    }

    public List<Vehicle> getVehicles() {
        return mVehicles;
    }

    public Vehicle getVehicle() {
        return mVehicle;
    }

    public String getVehicleUid() {
        return mVehicleUid;
    }
}
