package lej.happy.fooddiary.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import lej.happy.fooddiary.Activity.AddPostActivity

fun Context.getActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.getActivity()
        else -> null
    }
