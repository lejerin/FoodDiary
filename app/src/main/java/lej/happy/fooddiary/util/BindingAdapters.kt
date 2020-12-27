package lej.happy.fooddiary.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.navigation.NavigationView


object DatabindingUtils {

    @BindingAdapter("android:src")
    fun setImageViewResource(imageView: ImageView, resource: Int) {
        imageView.setImageResource(resource)
    }


}