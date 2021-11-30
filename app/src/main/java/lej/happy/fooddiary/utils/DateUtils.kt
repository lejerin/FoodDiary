package lej.happy.fooddiary.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import java.text.SimpleDateFormat
import java.util.*


object DateUtils {

    fun String.toCustomDate(dateFormat: String = "yyyy년 M월 d일", timeZone: TimeZone = TimeZone.getDefault()): Date? {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(this)
    }

    fun Date.getCustomYear() = Integer.parseInt(SimpleDateFormat("yyyy", Locale.KOREA).format(this))
    fun Date.getCustomMonth() = Integer.parseInt(SimpleDateFormat("M", Locale.KOREA).format(this))
}