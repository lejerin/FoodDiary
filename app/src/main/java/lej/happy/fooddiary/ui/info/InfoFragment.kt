package lej.happy.fooddiary.ui.info

import android.annotation.SuppressLint
import lej.happy.fooddiary.BuildConfig
import lej.happy.fooddiary.R
import lej.happy.fooddiary.databinding.FragmentDateBinding
import lej.happy.fooddiary.databinding.FragmentInfoBinding
import lej.happy.fooddiary.ui.base.BaseFragment

class InfoFragment : BaseFragment<FragmentInfoBinding>() {

    override val layoutResourceId: Int
        get() = R.layout.fragment_info

    @SuppressLint("SetTextI18n")
    override fun initBinding() {
        binding.opensourceBtn.setOnClickListener {
            val dlg = OpenSourceDialog(requireContext())
            dlg.start()
        }
        binding.versionText.text = "Version: ${BuildConfig.VERSION_NAME}"
    }
}