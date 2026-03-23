package com.cybrixx.ecomart.network;

import com.cybrixx.ecomart.model.UserProfileDTO; // You'll need to create this DTO in Android matching the backend one
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UserApi {
    @GET("users/profile")
    Call<UserProfileDTO> getProfile();

    @PUT("users/profile")
    Call<ResponseBody> updateProfile(@Body UserProfileDTO dto);
}