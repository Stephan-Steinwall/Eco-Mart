package com.cybrixx.ecomart.network;

import com.cybrixx.ecomart.model.FcmTokenDTO;
import com.cybrixx.ecomart.model.UserProfileDTO; // You'll need to create this DTO in Android matching the backend one
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;

public interface UserApi {
    @GET("users/profile")
    Call<UserProfileDTO> getProfile();

    @PUT("users/profile")
    Call<ResponseBody> updateProfile(@Body UserProfileDTO dto);

    // Links this phone to the logged-in user so order updates can be pushed to it.
    @PUT("users/fcm-token")
    Call<ResponseBody> registerFcmToken(@Body FcmTokenDTO dto);

    // The caller passes the bearer token explicitly: logout clears the saved session right away,
    // so the interceptor would otherwise have nothing to attach by the time this request runs.
    @DELETE("users/fcm-token")
    Call<ResponseBody> unregisterFcmToken(@Header("Authorization") String bearerToken);
}