package lej.happy.fooddiary.di

import lej.happy.fooddiary.camera.PhotoFileUpload
import lej.happy.fooddiary.data.local.prefs.UserPrefs
import lej.happy.fooddiary.data.local.repository.DateRepos
import lej.happy.fooddiary.data.local.repository.LocationRepos
import lej.happy.fooddiary.data.local.repository.RateRepos
import lej.happy.fooddiary.data.local.repository.PostRepos
import lej.happy.fooddiary.data.remote.MapKeywordService
import lej.happy.fooddiary.data.remote.repository.MapRepos
import lej.happy.fooddiary.ui.date.DateViewModel
import lej.happy.fooddiary.ui.main.MainViewModel
import lej.happy.fooddiary.ui.post.PostViewModel
import lej.happy.fooddiary.utils.CameraUtils
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import lej.happy.fooddiary.data.remote.KakaoRetrofitClient
import lej.happy.fooddiary.ui.location.LocationViewModel
import lej.happy.fooddiary.ui.location.detail.LocationDetailViewModel
import lej.happy.fooddiary.ui.rate.RateViewModel
import lej.happy.fooddiary.ui.view.ViewViewModel

val prefModule = module {
    single { UserPrefs(androidContext()) }
}

val retrofitClientModule = module {
    single { KakaoRetrofitClient() }
    single<MapKeywordService> {
        Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/v2/local/search/") // 도메인 주소
            .addConverterFactory(GsonConverterFactory.create()) // GSON을 사용하기 위해 ConverterFactory에 GSON 지정
            .build()
            .create(MapKeywordService::class.java)
    }
}

val viewModelModule = module {
    viewModel { MainViewModel() }
    viewModel { DateViewModel() }
    viewModel { LocationViewModel() }
    viewModel { LocationDetailViewModel() }
    viewModel { RateViewModel() }
    viewModel { PostViewModel() }
    viewModel { ViewViewModel() }
}

val repositoryModule = module {
    factory { DateRepos(androidContext()) }
    factory { LocationRepos(androidContext()) }
    factory { RateRepos(androidContext()) }
    factory { PostRepos(androidContext()) }
    factory { MapRepos(get()) }
}

val helperModule = module {
    single { PhotoFileUpload(androidContext()) }
    single { CameraUtils(androidContext()) }
}

var diModule =
    listOf(
        prefModule,
        retrofitClientModule,
        viewModelModule,
        repositoryModule,
        helperModule
    )