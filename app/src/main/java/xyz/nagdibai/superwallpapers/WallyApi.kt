package xyz.nagdibai.superwallpapers

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface WallyApi {

    @GET("{colName}")
    fun getAllWallpapers(
        @Path("colName") colName: String?
    ): Call<Chitra>

    @GET("{colName}/popular")
    fun getPopularWallpapers(
        @Path("colName") colName: String?
    ): Call<Chitra>

}