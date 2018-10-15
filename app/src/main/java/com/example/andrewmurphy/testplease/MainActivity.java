/*
 * Copyright (C) The Android Open Source Project
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

package com.example.andrewmurphy.testplease;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.vision.barcode.Barcode;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue, male, female, real, fake;
    private ProgressBar sex, fakeId;
    int scanCount = 0, totalAge = 0, mCount = 0, sexPercent, fCount =0, fakeIdPercent;
    String address;
    Button openStreetView, openImage, stats;

    boolean notFake = false;


    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        statusMessage = findViewById(R.id.status_message);
        barcodeValue = findViewById(R.id.barcode_value);
        sex = findViewById(R.id.sex);
        male = findViewById(R.id.male);
        male.setVisibility(View.INVISIBLE);
        female = findViewById(R.id.female);
        female.setVisibility(View.INVISIBLE);

        fakeId = findViewById(R.id.fakeId);
        real = findViewById(R.id.real);
        real.setVisibility(View.INVISIBLE);
        fake = findViewById(R.id.fake);
        fake.setVisibility(View.INVISIBLE);

        openStreetView = findViewById(R.id.openStreetView);
        openStreetView.setVisibility(View.INVISIBLE);
        openImage = findViewById(R.id.openImage);
        openImage.setVisibility(View.INVISIBLE);
        stats = findViewById(R.id.stats);


        /**Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4194");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }*/


        autoFocus = findViewById(R.id.auto_focus);
        useFlash = findViewById(R.id.use_flash);

        findViewById(R.id.read_barcode).setOnClickListener(this);
        stats.setOnClickListener(this);
        openImage.setOnClickListener(this);
        openStreetView.setOnClickListener(this);

        autoFocus.setVisibility(View.INVISIBLE);

        //Open to Camera
        openReader();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            //set first stat to invis
            sex.setVisibility(View.INVISIBLE);
            male.setVisibility(View.INVISIBLE);
            female.setVisibility(View.INVISIBLE);

            //set second stat to invis
            real.setVisibility(View.INVISIBLE);
            fake.setVisibility(View.INVISIBLE);
            fakeId.setVisibility(View.INVISIBLE);

            barcodeValue.setText("");
            notFake = false;
            //open up dat reader
            openReader();
        }
        if (v.getId() == R.id.stats) {
            //sex stat
            sexPercent = (int)((double)mCount/((double)scanCount)*100);
            sex.setProgress(sexPercent);
            male.setVisibility(View.VISIBLE);
            female.setVisibility(View.VISIBLE);
            male.setText("Male: "+sexPercent+"%");
            female.setText("Female: "+(100-sexPercent)+"%");
            sex.setVisibility(View.VISIBLE);

            //streetview buttons
            openStreetView.setVisibility(View.INVISIBLE);
            openImage.setVisibility(View.INVISIBLE);

            //fakeid stat
            fakeIdPercent = (int)((double)scanCount/((double)(scanCount+fCount))*100);
            fakeId.setProgress(fakeIdPercent);
            real.setVisibility(View.VISIBLE);
            fake.setVisibility(View.VISIBLE);
            fakeId.setVisibility(View.VISIBLE);
            real.setText("Valid: "+fakeIdPercent+"%");
            fake.setText("Invalid: "+(100-fakeIdPercent)+"%");

            stats.setVisibility(View.INVISIBLE);

            barcodeValue.setText(("Statistics \nAverage Age: "+ String.format("%.2f", ((double)totalAge/(double)scanCount)) + "\nTotal Scans: \t"+ scanCount));

        }
        if (v.getId() == R.id.openStreetView) {
            Streetview getLatLn = new Streetview();
            String latln = getLatLn.onCreate(address);

            System.out.println(latln);

            Intent intent1 = new Intent(this, PanoramaViewDemo.class);
            intent1.putExtra("latln", latln);
            this.startActivity(intent1);
        }
        if (v.getId() == R.id.openImage) {
            String url = "http://maps.googleapis.com/maps/api/streetview?size=640x480&location="
                    +address.replaceAll(" ", "").replaceAll("\n","")
                    +"&fov=140&pitch=10&sensor=false&key=AIzaSyDeD0lJesOnllLjBVGM-KIEzW8vUVlHc-c";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }

    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(parseString(barcode.rawValue));
                    Log.d(TAG, "Barcode read: " + barcode.rawValue);
                    if(notFake==true) {
                        openStreetView.setVisibility(View.VISIBLE);
                        openImage.setVisibility(View.VISIBLE);
                    }
                    stats.setVisibility(View.VISIBLE);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void openReader(){
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }


    String parseString(String id){
        try {
            if (id.charAt(0) != '@') {
                fCount++;
                return "This ID is fake.";
            }

            String fName = StringUtils.substringBetween(id, "DAC", "\n");
            String mName = StringUtils.substringBetween(id, "DAD", "\n");
            String lName = StringUtils.substringBetween(id, "DCS", "\n");

            String dob = StringUtils.substringBetween(id, "DBB", "\n");
            dob = dob.substring(0, 2) + "/" + dob.substring(2, 4) + "/" + dob.substring(4);

            String eyes = StringUtils.substringBetween(id, "DAY", "\n");

            String sex = StringUtils.substringBetween(id, "DBC", "\n");

            address = StringUtils.substringBetween(id, "DAG", "\n") + "\n" + StringUtils.substringBetween(id, "DAI", "\n") + ", "
                    + StringUtils.substringBetween(id, "DAJ", "\n") + " " + StringUtils.substringBetween(id, "DAK", "\n").subSequence(0, 5) + "\n"; //+ StringUtils.substringBetween(id, "DCG", "\n");


            if (fName == null || lName == null) {
                fCount++;
                return "This ID is fake.";
            }

            //add age
            SimpleDateFormat df = new SimpleDateFormat("dd/mm/yyyy");
            int yearDifference = 0;
            try {
                Date birthdate = df.parse(dob);
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthdate);
                Calendar today = Calendar.getInstance();

                yearDifference = today.get(Calendar.YEAR)
                        - birth.get(Calendar.YEAR);

                if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH)) {
                    yearDifference--;
                } else {
                    if (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH)
                            && today.get(Calendar.DAY_OF_MONTH) < birth
                            .get(Calendar.DAY_OF_MONTH)) {
                        yearDifference--;
                    }

                }
                totalAge += yearDifference;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (sex.charAt(0) == '1') {
                sex = "M";
                mCount++;
            } else
                sex = "F";


            System.out.println(fName);

            scanCount++;

            String parsed = "Name: " + fName + " " + mName + " " + lName + "\n" + "Date of birth: " + dob + "\nSex: " + sex + "      Age: " + yearDifference + "\n" + "Eye Color: " + eyes + "\n" + address;

            notFake = true;

            return (parsed);
        }
        catch (Exception e){
            return ("This ID is fake");
        }


    }

}