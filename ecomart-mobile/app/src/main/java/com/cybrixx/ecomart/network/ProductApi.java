package com.cybrixx.ecomart.network;


import java.util.List;
import com.cybrixx.ecomart.model.ProductDTO;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ProductApi {
    @GET("products")
    Call<List<ProductDTO>> getProducts();
}
