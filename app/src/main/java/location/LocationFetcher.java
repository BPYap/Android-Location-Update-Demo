package location;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.demo.locationupdatedemo.MainActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

public final class LocationFetcher {
    private static LocationFetcher single_instance = null;

    // request code mappings
    public static final int LOCATION_PERMISSION_REQUEST = 0;
    public static final int LOCATION_SETTING_REQUEST = 1;

    // Location settings
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest.Builder location_setting_request;
    private SettingsClient settings_client;
    private LocationCallback mLocationCallback;

    // Location provider and callback handler
    private FusedLocationProviderClient mFusedLocationClient;

    private LocationFetcher(){}

    private LocationFetcher(MainActivity mainActivity, LocationCallback locationCallback) {
        // Initialize LocationRequest object
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Initialize settings request
        location_setting_request = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        settings_client = LocationServices.getSettingsClient(mainActivity);

        // Setup FusedLocationClient and define locationCallback
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);

        mLocationCallback = locationCallback;
    }

    public static LocationFetcher getInstance(MainActivity mainActivity, LocationCallback locationCallback)
    {
        if (single_instance == null)
            single_instance = new LocationFetcher(mainActivity, locationCallback);

        return single_instance;
    }

    private void request_location_permission(MainActivity mainActivity) {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(mainActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
        }
    }

    private void check_location_settings(final MainActivity mainActivity) {
        Task<LocationSettingsResponse> task = settings_client.checkLocationSettings(location_setting_request.build());

        task.addOnFailureListener(mainActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(mainActivity,
                                LOCATION_SETTING_REQUEST);
                    }
                    catch (IntentSender.SendIntentException sendEx) {

                    }
                }
            }
        });
    }

    public void get_location_update(MainActivity mainActivity) {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            check_location_settings(mainActivity);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, null);
        }
        else {
            request_location_permission(mainActivity);
        }
    }
}
