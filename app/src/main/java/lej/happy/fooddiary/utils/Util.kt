package lej.happy.fooddiary.utils

import android.content.Context
import android.content.pm.PackageInfo
import java.text.SimpleDateFormat
import java.util.*

object Util {

    fun getVersionInfo(context: Context): String? {
        val info: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return info.versionName
    }
}