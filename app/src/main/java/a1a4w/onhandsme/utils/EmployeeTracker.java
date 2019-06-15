package a1a4w.onhandsme.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import a1a4w.onhandsme.model.MapModel;

import static a1a4w.onhandsme.utils.Constants.refDatabase;


public class EmployeeTracker extends Service {
    private static final String TAG = "GoldSmileGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 300*1000;
    private static final float LOCATION_DISTANCE = 0;
    private FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
    private String emailLogin,saleEmail;

    public EmployeeTracker() {
    }

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            Toast.makeText(getApplicationContext(),"Tọa độ của bạn đang được cập nhật",Toast.LENGTH_SHORT).show();


            //GeoFire geoFire = new GeoFire(refSupport.child(agent).child("GeoFire"));
           // geoFire.setLocation(deliveryEmail.replace(".",",")
          //          ,new GeoLocation(location.getLatitude(),location.getLongitude()));


            MapModel updateMap = new MapModel(location.getLatitude()+"",location.getLongitude()+"");
            refDatabase.child(emailLogin).child("GeoFire").child(saleEmail.replace(".",",")).setValue(updateMap);


        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //agent=(String) Objects.requireNonNull(intent.getExtras()).get("Agent");
        saleEmail = (String) Objects.requireNonNull(intent.getExtras()).get("SaleEmail");
        emailLogin = (String) Objects.requireNonNull(intent.getExtras()).get("EmailLogin");

        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}