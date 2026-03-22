package com.cybrixx.ecomart.network;


import com.cybrixx.ecomart.model.OrderRequestDTO;
import com.cybrixx.ecomart.model.OrderResponseDTO;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;

public interface OrderApi {
    @POST("orders")
    Call<ResponseBody> placeOrder(@Body OrderRequestDTO requestDTO); // Expecting a simple success string back

    @GET("orders/my-orders")
    Call<List<OrderResponseDTO>> getMyOrders();
}
