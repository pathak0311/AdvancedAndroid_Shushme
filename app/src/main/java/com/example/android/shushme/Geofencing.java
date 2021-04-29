package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback {

    private static final String TAG = Geofencing.class.getSimpleName();
    private static final long GEOFENCE_TIMEOUT = 24*60*60*1000;
    private static final float GEOFENCE_RADIUS = 50;
    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public Geofencing(GoogleApiClient mGoogleApiClient, Context mContext) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.mContext = mContext;
        this.mGeofenceList = new ArrayList<>();
        this.mGeofencePendingIntent = null;
    }

    public void registerAllGeofences(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected() || mGeofenceList == null || mGeofenceList.size() == 0){
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getmGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException e){
            Log.e(TAG, e.getMessage());
        }
    }

    public void unRegisterAllGeofences(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getmGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException e){
            Log.e(TAG, e.getMessage());
        }
    }

    public void updateGeofencesList(PlaceBuffer places){
        mGeofenceList = new ArrayList<>();
        if(places == null || places.getCount() == 0) return;
        for(Place place : places){
            String placeId = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeId)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getmGeofencePendingIntent(){
        if(mGeofencePendingIntent != null){
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Error adding/removing geofence: %s", result.getStatus().toString()));
    }
}
