package lej.happy.fooddiary

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import lej.happy.fooddiary.di.diModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import android.database.CursorWindow
import java.lang.Exception
import java.lang.reflect.Field


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

        // SQLiteBlobTooBigException Row too big to fit into CursorWindow requiredPos=0, totalRows=1
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) //the 100MB is the new size
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }

}