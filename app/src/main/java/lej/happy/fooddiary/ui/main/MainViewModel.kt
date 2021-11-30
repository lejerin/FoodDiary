package lej.happy.fooddiary.ui.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import lej.happy.fooddiary.BuildConfig
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.model.BarDate
import lej.happy.fooddiary.ui.post.PostActivity
import lej.happy.fooddiary.utils.Util
import lej.happy.fooddiary.utils.getActivity
import java.lang.StringBuilder

class MainViewModel : ViewModel() {

    val dateLiveEvent = MutableLiveData(BarDate())
    val tasteLiveEvent = MutableLiveData(1)

    fun newPost(view: View){
        val activity = view.context.getActivity()
        Intent(activity, PostActivity::class.java).also {
            activity?.startActivityForResult(it, BaseValue.ACTIVITY_RESULT_NEW_POST)
        }
    }

    fun getMailIntent(context: Context): Intent {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "plain/Text"

        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("eunjanii@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "<" + context.getString(R.string.app_name) + " " + "피드백 전달" + ">")

        val text = StringBuilder()
        text.append("앱 버전 (AppVersion): ${BuildConfig.VERSION_NAME}\n")
        text.append("기기명 (Device): ${Build.MODEL}\n")
        text.append("안드로이드 OS (Android OS): ${Build.VERSION.RELEASE}\n")
        text.append("API: ${Build.VERSION.SDK_INT}\n")
        text.append("내용 (Content):\n")

        emailIntent.putExtra(Intent.EXTRA_TEXT, text.toString())
        emailIntent.type = "message/rfc822"
        return emailIntent
    }
}