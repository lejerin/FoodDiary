package lej.happy.fooddiary.ui.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.databinding.ActivityViewBinding
import lej.happy.fooddiary.ui.base.BaseActivity
import lej.happy.fooddiary.ui.post.PostActivity
import lej.happy.fooddiary.utils.UiUtils
import java.text.SimpleDateFormat
import java.util.*

class ViewActivity : BaseActivity<ActivityViewBinding>() {
    private val TAG = PostActivity::class.java.simpleName

    private val mViewViewModel: ViewViewModel by viewModels()

    override val layoutResourceId: Int
        get() = R.layout.activity_view

    private var postId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initObserver()
        try {
            postId = intent.getLongExtra("postId", -1)
            mViewViewModel.getPostWithId(postId)
        } catch (e : Exception) {
            e.printStackTrace()
            UiUtils.showCenterToast(this@ViewActivity, "정보를 가져오는 실패하였습니다.")
            finishActivity()
        }
    }

    override fun initStartView() {
        binding.viewDetailBackBtn.setOnClickListener {
            finishActivity()
        }
    }

    private fun initObserver() {
        mViewViewModel.finish.observe(this, {
            finishActivity()
        })
        mViewViewModel.post.observe(this, {
            initView()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == BaseValue.REQUEST_CODE_MODIFY_POST && data != null) {
            mViewViewModel.isModified = true
            mViewViewModel.getPostWithId(postId)
        }
    }

    private fun initView(){
        binding.viewpager.clearData()
        binding.viewModel = mViewViewModel

        mViewViewModel.post.value?.let {
            binding.addDateText.text = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(it.date ?: Date())

            binding.viewpager.init(it.id!!, false)
            binding.viewpager.setPhoto(it, false)

            it.taste?.let { taste ->
                binding.detailPostTasteImg.setImageResource(mViewViewModel.getTasteImg(taste))
            }

            if (it.address != null) {
                binding.detailPostLocationText.setTextColor(
                    ContextCompat.getColor(this,
                        R.color.blue))
                binding.locationTag.setTextColor(
                    ContextCompat.getColor(this,
                        R.color.blue))
            }
        }
    }

    override fun onBackPressed() {
        if (mViewViewModel.isModified) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    private fun finishActivity(){
        if (mViewViewModel.isModified) {
            setResult(Activity.RESULT_OK)
        }
        supportFinishAfterTransition()
    }

    override fun afterPermission() {

    }
}