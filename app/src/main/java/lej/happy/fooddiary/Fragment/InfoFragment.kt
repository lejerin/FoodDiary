package lej.happy.fooddiary.Fragment

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_info.*
import lej.happy.fooddiary.Helper.OpenSourceDialog
import lej.happy.fooddiary.R

class InfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        opensource_btn.setOnClickListener {
            val dlg = OpenSourceDialog(context!!)
            dlg.start()
        }

        version_text.text = "Version: " + getVersionInfo()

    }

    fun getVersionInfo(): String? {
        val info: PackageInfo = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
        return info.versionName
    }
}