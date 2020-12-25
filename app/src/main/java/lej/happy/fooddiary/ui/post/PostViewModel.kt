package lej.happy.fooddiary.ui.post

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.coroutines.*
import lej.happy.fooddiary.Adapter.ViewPagerAdapter
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.data.db.entity.Thumb
import lej.happy.fooddiary.ui.MainViewModel
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.ui.custom.CustomImageButton
import lej.happy.fooddiary.ui.custom.CustomPhotoViewPager
import lej.happy.fooddiary.util.CameraUtil
import lej.happy.fooddiary.util.Coroutines
import lej.happy.fooddiary.util.getActivity

class PostViewModel(
) : BaseViewModel() {

    lateinit var repository: Repository

    var post = Post()
    var thumb = Thumb()

    private var selectedBtn: CustomImageButton? = null

    //선택한 현재 사진 뷰페이저 index num
    private val _message = MutableLiveData<String>()
    val message : LiveData<String>
        get() = _message

    fun addPhoto(v: CustomPhotoViewPager){
        if(v.getSize() < 4){
            setPermission(v)

        }else{
            _message.value = "사진은 최대 4장까지 추가 가능합니다"
        }
    }

    fun removePhoto(v: CustomPhotoViewPager){

        v.remove()

    }



    fun selectedBtn(v: View){

        selectedBtn?.isSelected = false
        selectedBtn = v as CustomImageButton
        selectedBtn?.isSelected = true

        when(v.id){
            R.id.best_taste_btn -> post.taste = 1
            R.id.good_taste_btn -> post.taste = 2
            R.id.bad_taste_btn -> post.taste = 3
        }
    }

    //선택시간대 고르기 (아침, 점심, 저녁, 간식, 야식)
    val radioListner = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        when(checkedId){
            R.id.radioButton1 -> post.time = 1
            R.id.radioButton2 -> post.time = 2
            R.id.radioButton3 -> post.time = 3
            R.id.radioButton4 -> post.time = 4
            R.id.radioButton5 -> post.time = 5
        }
    }



    //수정일경우 데이터 입력하기
    fun setModifyData(){

        //해당 년도와 월에 대해서만 date, count 순서대로 가져옴
        newJob(
            Coroutines.ioThenMain(
                { repository.getThumbId(post.id!!)},
                {
                    if (it != null) {
                        thumb = it
                    }
                }
            ))

    }



    fun setPermission(v: View) {
        val permission = object : PermissionListener {
            override fun onPermissionGranted() {//설정해 놓은 위험권한(카메라 접근 등)이 허용된 경우 이곳을 실행
                showChoicePhotoDialog(v.context)

            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {//설정해 놓은 위험권한이 거부된 경우 이곳을 실행
                Toast.makeText(v.context,"요청하신 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.with(v.context)
            .setPermissionListener(permission)
            .setRationaleMessage("카메라 앱을 사용하시려면 권한을 허용해주세요.")
            .setDeniedMessage("권한을 거부하셨습니다.앱을 사용하시려면 [앱 설정]-[권한] 항목에서 권한을 허용해주세요.")
            .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
            .check()
    }

    private fun showChoicePhotoDialog(context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setMessage("사진을 선택해주세요")
            .setCancelable(false)
            .setPositiveButton("카메라") { _, _ ->
                CameraUtil.getInstance(context).dispatchTakePictureIntent()
            }
            .setNegativeButton("앨범") { _, _ ->
                // Dismiss the dialog
                openGallery(context)
            }
        val alert = builder.create()
        alert.show()
    }

    val REQUEST_IMAGE_PICK = 10

    private fun openGallery(context: Context) {
        val activity = context.getActivity()

        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = MediaStore.Images.Media.CONTENT_TYPE
        activity?.startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK)
    }

    fun activityFinish(v: View) {
        val activity = v.context.getActivity()
        activity?.finish()
    }

}