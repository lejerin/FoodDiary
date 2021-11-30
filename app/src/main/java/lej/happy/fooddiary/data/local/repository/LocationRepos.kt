package lej.happy.fooddiary.data.local.repository

import android.content.Context
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.local.db.AppDatabase

class LocationRepos(val context: Context) {

    val db = AppDatabase.getInstance(context)

    fun getLocationDESC(page: Int) =
        db.postDao().selectByLocationDesc((page-1) * BaseValue.limit, (page) * BaseValue.limit)

    fun getLocationASC(page: Int) =
        db.postDao().selectByLocationAsc((page-1) * BaseValue.limit, (page) * BaseValue.limit)

    fun getAddressDESC(adr: String, page: Int) =
        db.postDao().selectByAddressDesc(adr, (page-1) * BaseValue.limit, (page) * BaseValue.limit)

    fun getAddressASC(adr: String, page: Int) =
        db.postDao().selectByAddressAsc(adr, (page-1) * BaseValue.limit, (page) * BaseValue.limit)
}