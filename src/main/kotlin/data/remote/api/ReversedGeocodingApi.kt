package data.remote.api

import data.remote.model.ReversedCountry
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface ReversedGeocodingApi {

    @GET("reverse")
    fun getCountryNameByCoordinates(
        @Query("lat") latitude: String,
        @Query("lon") longitude:String,
        @Query("format") formatData:String
    ): Deferred<ReversedCountry>
}