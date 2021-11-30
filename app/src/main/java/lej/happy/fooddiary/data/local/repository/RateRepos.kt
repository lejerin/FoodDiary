package lej.happy.fooddiary.data.local.repository

import android.content.Context
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.local.db.AppDatabase

class RateRepos (val context: Context) {

    val db = AppDatabase.getInstance(context)

    fun getRateDESC(num: Int, page: Int) = db.postDao().selectByTasteDesc(num, (page-1) * BaseValue.limit, (page) * BaseValue.limit)


    fun getRateASC(num: Int, page: Int) = db.postDao().selectByTasteAsc(num, (page-1) * BaseValue.limit, (page) * BaseValue.limit)
}