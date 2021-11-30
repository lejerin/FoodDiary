package lej.happy.fooddiary.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class KakaoRetrofitClient {
    // 위에서 만든 RetrofitService를 연결해줍니다.
    fun getService(): MapKeywordService = retrofit.create(MapKeywordService::class.java)

    private val retrofit =
        Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/v2/local/search/") // 도메인 주소
            .addConverterFactory(GsonConverterFactory.create()) // GSON을 사용하기 위해 ConverterFactory에 GSON 지정
            .build()
}