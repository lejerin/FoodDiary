package lej.happy.fooddiary.data.remote.repository

import lej.happy.fooddiary.data.model.LocalMapData
import lej.happy.fooddiary.data.remote.KakaoRetrofitClient
import retrofit2.Call

class MapRepos (private val retrofitClient: KakaoRetrofitClient) {

    fun requestKeywordList(
        query: String,
        authorization: String
    ): Call<LocalMapData> {
        return retrofitClient.getService().getKeywordMap(
            query,
            authorization)
    }
}