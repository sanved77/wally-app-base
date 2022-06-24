package xyz.nagdibai.superwallpapers

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
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

    @POST("{colName}/downloadInc/{id}")
    fun downloadInc(
        @Path("colName") colName: String?,
        @Path("id") id: String?
    ): Call<ChitraItemAPI>

}