package lej.happy.fooddiary.Helper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_time.*
import lej.happy.fooddiary.Adapter.LicenseAdapter
import lej.happy.fooddiary.Model.HomeData
import lej.happy.fooddiary.Model.LicenseItem
import lej.happy.fooddiary.R

class OpenSourceDialog(context : Context) {

    private val context = context
    private val dlg = Dialog(context)   //부모 액티비티의 context 가 들어감



    fun start() {
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.open_source_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴




        val rv_open_licence = dlg.findViewById<RecyclerView>(R.id.rv_open_licence)
        val layoutManager = LinearLayoutManager(context)
        rv_open_licence.layoutManager =  layoutManager

        val licenseList = mutableListOf<LicenseItem>()

        licenseList.add(LicenseItem("Gson", "https://github.com/google/gson",
            "Copyright 2008 Google Inc.", "Apache License 2.0"))

        licenseList.add(LicenseItem("Retrofit", "https://square.github.io/retrofit",
            "Copyright 2013 Square, Inc.", "Apache License 2.0"))

        licenseList.add(LicenseItem("TedPermission", "https://github.com/ParkSangGwon/TedPermission",
            "Copyright 2017 Ted Park", "Apache License 2.0"))


        licenseList.add(LicenseItem("AndroidSlidingUpPanel", "https://github.com/umano/AndroidSlidingUpPanel",
            "Copyright 2015 Anton Lopyrev", "Apache License 2.0"))

        licenseList.add(LicenseItem("PhotoView", "https://github.com/chrisbanes/PhotoView",
            "Copyright 2018 Chris Banes", "Apache License 2.0"))

        rv_open_licence.adapter = LicenseAdapter(licenseList)

        val btnCancel = dlg.findViewById(R.id.open_source_back_btn) as ImageButton
        btnCancel.setOnClickListener {
            dlg.dismiss()
        }

        dlg.show()
    }


}