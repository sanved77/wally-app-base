package xyz.nagdibai.superwallpapers

import retrofit2.Call
import retrofit2.http.GET

interface WallyApi {

    // TODO: 9/11/2021 Add dynamic string for collection name 
    
    @GET("testy")
    fun getAllWallpapers(): Call<Chitra>

}