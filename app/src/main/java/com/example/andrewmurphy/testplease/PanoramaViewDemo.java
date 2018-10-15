package com.example.andrewmurphy.testplease;

/**
 * Created by andymurphy on 10/14/18.
 */

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup.LayoutParams;

import org.apache.commons.lang3.StringUtils;

/**
 * This shows how to create a simple activity with streetview
 */
public class PanoramaViewDemo extends AppCompatActivity {


    private LatLng location = new LatLng( 42.3554329, -88.0592862);

    private StreetViewPanoramaView mStreetViewPanoramaView;

    private static final String STREETVIEW_BUNDLE_KEY = "StreetViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //sets location
        setCameraLatLng(savedInstanceState);

        StreetViewPanoramaOptions options = new StreetViewPanoramaOptions();
        if (savedInstanceState == null) {
            options.position(location);
        }

        mStreetViewPanoramaView = new StreetViewPanoramaView(this, options);
        addContentView(mStreetViewPanoramaView,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // *** IMPORTANT ***
        // StreetViewPanoramaView requires that the Bundle you pass contain _ONLY_
        // StreetViewPanoramaView SDK objects or sub-Bundles.
        Bundle mStreetViewBundle = null;
        if (savedInstanceState != null) {
            mStreetViewBundle = savedInstanceState.getBundle(STREETVIEW_BUNDLE_KEY);
        }
        mStreetViewPanoramaView.onCreate(mStreetViewBundle);
    }

    protected void setCameraLatLng(Bundle savedInstanceState){
        String latln;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                latln= null;
            } else {
                latln = extras.getString("STRING_I_NEED");
            }
        } else {
            latln = (String) savedInstanceState.getSerializable("STRING_I_NEED");
        }

        String lat = StringUtils.substringBetween(latln, "\"lat\": ", ",");
        String lng = StringUtils.substringBetween(latln, "\"lng\": ", "\n");

        location = new LatLng( Double.valueOf(lat), Double.valueOf(lng));
    }

    @Override
    protected void onResume() {
        mStreetViewPanoramaView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mStreetViewPanoramaView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mStreetViewPanoramaView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mStreetViewBundle = outState.getBundle(STREETVIEW_BUNDLE_KEY);
        if (mStreetViewBundle == null) {
            mStreetViewBundle = new Bundle();
            outState.putBundle(STREETVIEW_BUNDLE_KEY, mStreetViewBundle);
        }

        mStreetViewPanoramaView.onSaveInstanceState(mStreetViewBundle);
    }
}
