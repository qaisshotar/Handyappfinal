package com.example.handymanfinal.Remote;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IgoogleAPI {


    @GET("maps/api/directions/json")
    Observable <String> getDirections(
            @Query("mode") String mode,
            @Query("transit_routing_preference") String transit_routeing,
            @Query("origin") String from,
            @Query("destination") String to,
            @Query("key") String key
    );
}
