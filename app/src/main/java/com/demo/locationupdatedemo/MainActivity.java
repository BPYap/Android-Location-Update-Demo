package com.demo.locationupdatedemo;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import location.LocationFetcher;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private View mLayout;

    // Location related
    private LocationFetcher mLocationFetcher;

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

        LocationCallback locationCallback = new LocationCallback() {
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
        mLocationFetcher = LocationFetcher.getInstance(this, locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Main", "Resuming");
        mLocationFetcher.get_location_update(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Main", "Paused");
    }
}
