package lej.happy.fooddiary.util

import androidx.databinding.BindingAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


object DatabindingUtils {

    @BindingAdapter("onNavigationItemSelected")
    fun setOnNavigationItemSelected(view: NavigationView,
                                    listener: NavigationView.OnNavigationItemSelectedListener) {

        view.setNavigationItemSelectedListener(listener)
    }


}