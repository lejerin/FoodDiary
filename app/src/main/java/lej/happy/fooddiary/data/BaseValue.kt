package lej.happy.fooddiary.data

object BaseValue {
    // Fragment
    val LOAD_DATE_FRAGMENT_TAG = "date"
    val LOAD_LOCATION_FRAGMENT_TAG = "location"
    val LOAD_RATE_FRAGMENT_TAG = "rate"
    val LOAD_INFO_FRAGMENT_TAG = "info"

    // Order
    val ORDER_NEWEST = "newest"
    val ORDER_OLDEST = "oldest"

    // Page Limit
    val limit = 20

    // ACTIVITY_RESULT
    val ACTIVITY_RESULT_NEW_POST = 77
    val ACTIVITY_RESULT_VIEW_POST = 88

    val REQUEST_CODE_OPEN_MAP_SEARCH = 44
    val REQUEST_CODE_MODIFY_POST = 66

    enum class DATA_TYPE {
        INIT,
        ADD
    }


}