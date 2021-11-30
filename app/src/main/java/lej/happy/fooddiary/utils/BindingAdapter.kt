package lej.happy.fooddiary.utils

import android.view.View

object BindingAdapter {

    @androidx.databinding.BindingAdapter("bind:roadAddressVisibility")
    @JvmStatic
    fun setVisibilityRoadAddressLayout(view: View, address: String?){
        view.visibility = if (address.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    @androidx.databinding.BindingAdapter("bind:selection")
    @JvmStatic
    fun setSelect(view: View, enable: Boolean){
        view.isSelected = enable
    }
}