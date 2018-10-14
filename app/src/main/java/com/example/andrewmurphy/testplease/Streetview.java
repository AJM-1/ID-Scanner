package com.example.andrewmurphy.testplease;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

import java.io.IOException;

/**
 * Created by andymurphy on 10/13/18.
 */

public class Streetview{

    public String onCreate(String address) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyDeD0lJesOnllLjBVGM-KIEzW8vUVlHc-c")
                .build();
        GeocodingResult[] results = new GeocodingResult[0];

        try {
            results = GeocodingApi.geocode(context,
                    address).await();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        System.out.println(gson.toJson(results[0].geometry.location));

        return(gson.toJson(results[0].geometry.location));
    }
}
