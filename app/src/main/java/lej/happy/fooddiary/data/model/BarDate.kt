package lej.happy.fooddiary.data.model

import lej.happy.fooddiary.utils.DateUtils.getCustomMonth
import lej.happy.fooddiary.utils.DateUtils.getCustomYear
import java.util.*

class BarDate {
    var year = 0
    var month = 0
    var isAll = true

    init {
        setDate(Date())
    }

    fun setDate(date: Date) {
        year = date.getCustomYear()
        month = date.getCustomMonth()
    }

    override fun toString(): String {

        if(!isAll){
            return "" + year + "년 " + month + "월"
        }
        return "All"

    }
}