package lej.happy.fooddiary

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import lej.happy.fooddiary.di.diModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@MyApplication)
            modules(diModule)
        }

        // FirebaseCrashlytics 설정
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

}