import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public static final class LocationFetcher {

    // request code mappings
    private static final int LOCATION_PERMISSION_REQUEST = 0;
    private static final int LOCATION_SETTING_REQUEST = 1;

    private View mLayout;

    // Location settings
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest.Builder location_setting_request;
    private SettingsClient settings_client;

    // Location provider and callback handler
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    // TextView widgets
    private TextView mLastUpdateTimeTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mDebugTextView;

    // Labels for each TextView
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;

    private String mLastUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get main layout
        mLayout = findViewById(R.id.main_layout);

        // Locate the TextView widgets
        mLatitudeTextView = findViewById(R.id.latitude_text);
        mLongitudeTextView = findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = findViewById(R.id.last_update_time_text);
        mDebugTextView = findViewById(R.id.debug_text);

        // Set labels.
        mLatitudeLabel = "Latitude";
        mLongitudeLabel = "Longitude";
        mLastUpdateTimeLabel = "Last update";

        // Initialize LocationRequest object
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Initialize settings request
        location_setting_request = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        settings_client = LocationServices.getSettingsClient(this);

        // Setup FusedLocationClient and define locationCallback
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Snackbar.make(mLayout, "No location info received",
                            Snackbar.LENGTH_INDEFINITE).show();
                    return;
                }

                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                Location location = locationResult.getLastLocation();
                mLatitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel,
                        location.getLatitude()));
                mLongitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel,
                        location.getLongitude()));
                mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
                        mLastUpdateTimeLabel, mLastUpdateTime));
            }
        };

        get_location_update();
    }

    @Override
    protected void onResume() {
        super.onResume();
        get_location_update();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    get_location_update();
                } else {
                    request_location_permission();
                }
        }
    }

    private void request_location_permission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(mLayout, "Need permission to get location info",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Ok", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION_REQUEST);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_SETTING_REQUEST);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCATION_SETTING_REQUEST:
                if (resultCode == RESULT_OK) {
                    mDebugTextView.setText("High Accuracy GPS mode ON");
                    get_location_update();
                }
        }
    }

    private void check_location_settings() {
        Task<LocationSettingsResponse> task = settings_client.checkLocationSettings(location_setting_request.build());

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        mDebugTextView.setText("Unmatched location settings detected");
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                LOCATION_SETTING_REQUEST);
                    }
                    catch (IntentSender.SendIntentException sendEx) {
                        mDebugTextView.setText("Can't resolve exception in check location setting task");
                    }
                }
            }
        });
    }

    private void get_location_update() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            check_location_settings();
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, null);
        }
        else {
            request_location_permission();
        }
    }
}
