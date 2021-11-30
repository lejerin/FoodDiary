package lej.happy.fooddiary.data.local.repository

import android.content.Context
import android.util.Log
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.local.db.AppDatabase
import java.util.*

class DateRepos(val context: Context) {

    val db = AppDatabase.getInstance(context)

    fun getDataAllDESC(page: Int) =
        db.postDao().selectByPageDesc((page-1) * BaseValue.limit, (page) * BaseValue.limit)

    fun getDataAllASC(page: Int) =
        db.postDao().selectByPageAsc((page-1) * BaseValue.limit, (page) * BaseValue.limit)

    fun getDataMonthDESC(startDate : Date, endDate : Date, page: Int) =
        db.postDao().selectByDateDESC(startDate, endDate, (page-1) * BaseValue.limit, (page) * BaseValue.limit)

    fun getDataMonthASC(startDate : Date, endDate : Date, page: Int) =
        db.postDao().selectByDateASC(startDate, endDate, (page-1) * BaseValue.limit, (page) * BaseValue.limit)
}