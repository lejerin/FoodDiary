package lej.happy.fooddiary.data

import android.content.Context
import lej.happy.fooddiary.DB.AppDatabase
import java.util.*

class Repository (val context: Context){

    val db = AppDatabase.getInstance(context)

    fun getDataAllDESC(page: Int, limit: Int) =
        db.postDao().selectByPageDesc((page-1)*limit, (page)*limit)

    fun getDataAllASC(page: Int, limit: Int) =
        db.postDao().selectByPageAsc((page-1)*limit, (page)*limit)

    fun getDataMonthDESC(startDate : Date, endDate : Date) =
        db.postDao().selectByDateDESC(startDate, endDate)

    fun getDataMonthASC(startDate : Date, endDate : Date) =
        db.postDao().selectByDateASC(startDate, endDate)

}