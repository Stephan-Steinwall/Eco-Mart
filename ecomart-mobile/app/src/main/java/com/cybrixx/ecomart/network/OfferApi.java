package com.cybrixx.ecomart.network;

import com.cybrixx.ecomart.model.OfferDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface OfferApi {
    @GET("offers/active")
    Call<List<OfferDTO>> getActiveOffers();
}
