package lej.happy.fooddiary.data.remote

import lej.happy.fooddiary.data.model.LocalMapData
import retrofit2.Call
import retrofit2.http.*

interface MapKeywordService {

    @GET("keyword.json")
    fun getKeywordMap(
        @Query("query") query: String,
        @Header("Authorization") Authorization: String
    ): Call<LocalMapData>

}