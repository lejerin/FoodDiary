package lej.happy.fooddiary.ui.post

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lej.happy.fooddiary.R
import lej.happy.fooddiary.camera.PhotoFileUpload
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.databinding.ActivityPostBinding
import lej.happy.fooddiary.ui.base.BaseActivity
import lej.happy.fooddiary.ui.custom.CustomPhotoViewPager
import lej.happy.fooddiary.utils.Coroutines
import lej.happy.fooddiary.utils.DateUtils.toCustomDate
import lej.happy.fooddiary.utils.UiUtils
import lej.happy.fooddiary.utils.Util
import org.koin.java.KoinJavaComponent.inject
import java.text.SimpleDateFormat
import java.util.*

class PostActivity : BaseActivity<ActivityPostBinding>() {
    private val TAG = PostActivity::class.java.simpleName

    override val layoutResourceId: Int
        get() = R.layout.activity_post

    private val mPostViewModel: PostViewModel by viewModels()
    private val photoFileUpload: PhotoFileUpload by inject(PhotoFileUpload::class.java)

    private val mTimeSelectButton by lazy { binding.timeSelectButton }
    private val mTasteSelectButton by lazy { binding.tasteSelectButton }

    override fun initStartView() {
        binding.viewModel = mPostViewModel
        initView()
        initViewPager()
        initObserver()
        initClick()
    }

    //onActivityResult Deprecated
    private val fileResultActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult() // ◀ StartActivityForResult 처리를 담당
    ) { activityResult ->
        photoFileUpload.fileChooserResult(activityResult.resultCode, activityResult.data)?.let {
            binding.viewpager.add(it)
        }
    }

    private fun pickPhoto() {
        val intent : Intent = photoFileUpload.createShowFileChooserIntent(this@PostActivity)
        fileResultActivity.launch(intent)
    }

    private fun initView() {
        (intent.getLongExtra("postId", -1)).let { postId ->
            if (postId >= 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    val post = mPostViewModel.getPostWithId(postId)
                    //본문,사진,위치,주소,위치,시간대,평가 표시
                    binding.viewpager.setPhoto(post, true)
                    mPostViewModel.setModifyData()
                    mPostViewModel.post = post
                    CoroutineScope(Dispatchers.Main).launch {
                        post.date?.let { date -> mPostViewModel.dateStr.value = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(date) }
                        post.time?.let { mTimeSelectButton.clickButton(it) }
                        post.taste?.let { mTasteSelectButton.selectedNum = it }
                    }
                }
            } else {
                mPostViewModel.dateStr.value =
                    SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(Date()).also {
                        mPostViewModel.post.date = it.toCustomDate()
                    }
            }
        }
    }

    private fun initViewPager() {
        //뷰페이저
        binding.viewpager.apply {
            init(0, true)
            setButtonListener(object : CustomPhotoViewPager.PhotoButtonListener{
                override fun isAdd(isTrue: Boolean) {
                    if (isTrue) {
                        addPhoto(getSize())
                    } else {
                        remove()
                    }
                }
            })
        }
    }

    private fun initObserver() {
        mPostViewModel.message.observe(this, {
            UiUtils.showCenterToast(this@PostActivity, it)
        })
        mPostViewModel.dateStr.observe(this, {
            binding.addDateText.text = it
        })
        mTimeSelectButton.selectSingleLiveEvent.observe(this@PostActivity, {
            mPostViewModel.post.time = it
        })
        mTasteSelectButton.selectSingleLiveEvent.observe(this@PostActivity, {
            mPostViewModel.post.taste = it
        })
    }

    private fun addPhoto(size: Int) {
        if (size < 4) {
            requestPermission()
        } else {
           UiUtils.showCenterToast(this@PostActivity, "사진은 최대 4장까지 추가 가능합니다")
        }
    }

    private fun initClick() {
        binding.savePostBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                mPostViewModel.saveData(binding.viewpager.photoList, this@PostActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //주소 검색을 완료했을 때
        if (resultCode == Activity.RESULT_OK && requestCode == BaseValue.REQUEST_CODE_OPEN_MAP_SEARCH && data != null) {
            mPostViewModel.post.apply {
                data.getStringExtra("name")?.let {
                    address = it
                    binding.addAddressText.text = it
                    binding.addAddressText.visibility = View.VISIBLE

                }
                data.getStringExtra("roadAddress")?.let {
                    location = it
                    binding.locationTitleText.setText(it)
                }
                x = data.getStringExtra("x")?.toDouble()
                y = data.getStringExtra("y")?.toDouble()
            }
        }
    }

    override fun afterPermission() {
        pickPhoto()
    }
}