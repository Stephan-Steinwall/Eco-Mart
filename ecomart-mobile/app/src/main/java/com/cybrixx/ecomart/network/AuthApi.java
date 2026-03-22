package com.cybrixx.ecomart.network;

import com.cybrixx.ecomart.model.LoginRequestDTO;
import com.cybrixx.ecomart.model.RegisterRequestDTO;
import com.cybrixx.ecomart.model.TokenDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<TokenDTO> userLogin(@Body LoginRequestDTO requestDTO); // [cite: 1827, 1828]

    @POST("auth/register")
    Call<TokenDTO> userRegister(@Body RegisterRequestDTO requestDTO);
}
