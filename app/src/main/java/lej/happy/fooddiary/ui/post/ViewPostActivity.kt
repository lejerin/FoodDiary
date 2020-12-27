package lej.happy.fooddiary.ui.post

import android.app.Activity
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_detail_post.*
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.databinding.ActivityDetailPostBinding
import lej.happy.fooddiary.ui.base.BaseActivity

class ViewPostActivity : BaseActivity<ActivityDetailPostBinding>() {


    override val layoutResourceId: Int
        get() = R.layout.activity_detail_post

    private val viewModel: ViewPostViewModel by viewModels()

    private val REQUEST_CODE_MODIFY_POST = 66


    override fun initStartView() {
        viewModel.repository =  Repository(this@ViewPostActivity)
        viewModel.post = intent.getSerializableExtra("post") as Post


    }

    override fun initDataBinding() {
        initView()
        viewModel.finish.observe(this, Observer {
            finishActivity()
        })
    }

    override fun initAfterBinding() {
        viewDataBinding.viewDetailBackBtn.setOnClickListener {
            finishActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_MODIFY_POST && data != null){
            viewModel.isModified = true
            viewModel.post = data.getSerializableExtra("modifyPost") as Post
            initView()

        }

    }

    private fun initView(){
        viewDataBinding.viewpager.clearData()
        viewDataBinding.viewModel = viewModel
        viewDataBinding.viewpager.init(viewModel.post.id!!)
        viewDataBinding.viewpager.hideButton()
        viewDataBinding.viewpager.setPhoto(viewModel.post, false)

        viewDataBinding.detailPostTasteImg.setImageResource(viewModel.getTasteImg())

        if(viewModel.post.address != null){
            detail_post_location_text.setTextColor(
                ContextCompat.getColor(this,
                    R.color.blue))
            location_tag.setTextColor(
                ContextCompat.getColor(this,
                    R.color.blue))
        }

    }

    override fun onBackPressed() {
        if(viewModel.isModified){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    private fun finishActivity(){
        if(viewModel.isModified){
            setResult(Activity.RESULT_OK)
        }
        supportFinishAfterTransition()
    }

}