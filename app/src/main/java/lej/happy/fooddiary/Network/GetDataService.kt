package lej.happy.fooddiary.Network

import lej.happy.fooddiary.Model.LocalMapData
import retrofit2.Call
import retrofit2.http.*

interface GetDataService {

    @GET("keyword.json")
    fun getKeywordMap(
        @Query("query") query: String,
        @Header("Authorization") Authorization: String
    ): Call<LocalMapData>

}