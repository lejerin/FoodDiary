package lej.happy.fooddiary.ui.post

import android.app.Activity
import android.app.DatePickerDialog
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
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.coroutines.*
import lej.happy.fooddiary.ui.map.MapSearchActivity
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.data.db.AppDatabase
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.data.db.entity.Thumb
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.ui.custom.CustomImageButton
import lej.happy.fooddiary.ui.custom.CustomPhotoViewPager
import lej.happy.fooddiary.util.CameraUtil
import lej.happy.fooddiary.util.Coroutines
import lej.happy.fooddiary.util.ImageUtil
import lej.happy.fooddiary.util.getActivity
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class AddPostViewModel(
) : BaseViewModel() {

    lateinit var repository: Repository

    var post = Post()
    var thumb = Thumb()

    private var selectedBtn: CustomImageButton? = null
    val dateStr = MutableLiveData<String>()

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

    private val _isSaving = MutableLiveData<Boolean>().also {
        it.value = false
    }
    val isSaving : LiveData<Boolean>
        get() = _isSaving


    fun saveData(photoList: List<Uri>, context: Context){

        //사진 저장
        if(photoList.isNotEmpty() && !_isSaving.value!!){

            _isSaving.value = true

            //db에 저장
            val getDb = AppDatabase.getInstance(context)
            val cameraUtil = CameraUtil.getInstance(context)

            if(checkUriValid(photoList[0], context)){
                post.photo = ImageUtil.convert(
                    cameraUtil.decodeSampledBitmapFromResource(photoList[0], 100, 100))

                post.photo1 = photoList[0].toString()
                thumb.photo1_bitmap = ImageUtil.convert(cameraUtil.decodeSampledBitmapFromResource(photoList[0], 300, 300))
            }

            if(photoList.size > 1){
                if(checkUriValid(photoList[1], context)){
                    post.photo2 = photoList[1].toString()
                    thumb.photo2_bitmap = ImageUtil.convert(cameraUtil.decodeSampledBitmapFromResource(photoList[1], 300, 300))
                }
            }else{
                post.photo2 = null
                thumb.photo2_bitmap = null
            }

            if(photoList.size > 2){
                if(checkUriValid(photoList[2], context)){
                    post.photo3 = photoList[2].toString()
                    thumb.photo3_bitmap = ImageUtil.convert(cameraUtil.decodeSampledBitmapFromResource(photoList[2], 300, 300))
                }
            }else{
                post.photo3 = null
                thumb.photo3_bitmap = null
            }

            if(photoList.size > 3){
                if(checkUriValid(photoList[3], context)){
                    post.photo4 = photoList[3].toString()
                    thumb.photo4_bitmap = ImageUtil.convert(cameraUtil.decodeSampledBitmapFromResource(photoList[3], 300, 300))
                }
            }else{
                post.photo4 = null
                thumb.photo4_bitmap = null
            }



            if(isModify){
                //수정
                val addRunnable = Runnable {
                    try {
                        getDb.postDao().update(post)

                        thumb.id = post.id
                        getDb.thumbDao().update(thumb)

                        val resultIntent = Intent()
                        resultIntent.putExtra("modifyPost", post)
                        context.getActivity().also {
                            it?.setResult(Activity.RESULT_OK, resultIntent)
                            it?.finish()
                        }

                    } catch (e: Exception){
                        //저장 실패

                    }
                }
                val addThread = Thread(addRunnable)
                addThread.start()

            }else{
                //신규추가
                //count 센 뒤 저장
                val r = Runnable {
                    try {
                        post.count  = getDb.postDao().getCount(post.date!!) + 1
                        val addRunnable = Runnable {
                            try {
                                thumb.id = getDb.postDao().insert(post)
                                getDb.thumbDao().insert(thumb)

                                context.getActivity().also {
                                    it?.setResult(Activity.RESULT_OK)
                                    it?.finish()
                                }
                            } catch (e: Exception){
                                //저장 실패

                            }
                        }
                        val addThread = Thread(addRunnable)
                        addThread.start()
                    } catch (e: Exception) {

                    }
                }

                val thread = Thread(r)
                thread.start()
            }

        }
    }

    private fun checkUriValid(uri: Uri, context: Context) : Boolean{
        var ls : InputStream? = null
        try {
            ls = context.contentResolver.openInputStream(uri)
        }catch (e: Exception){

        }

        if(ls == null)
            return false

        return true
    }


    fun showSelectDateDialog(v: View){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(v.context, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            // Display Selected date in textbox
            var monthtext = (monthOfYear + 1).toString()
            if(monthtext.length == 1){
                monthtext = "0" + monthtext
            }
            var daytext = dayOfMonth.toString()
            if(daytext.length == 1){
                daytext = "0" + daytext
            }
            dateStr.value = "" + year + "년 " + (monthOfYear + 1)  + "월 " + dayOfMonth + "일"
            post.date = StringtoDate(dateStr.value!!)

        }, year, month, day)

        dpd.show()
    }

    fun selectedBtn(v: View){

        selectedBtn?.isSelected = false
        selectedBtn = v as CustomImageButton
        selectedBtn?.isSelected = true

        when(v.id){
            R.id.best_emotion_layout -> post.taste = 1
            R.id.good_emotion_layout -> post.taste = 2
            R.id.bad_emotion_layout -> post.taste = 3
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



    private var isModify = false
    //수정일경우 데이터 입력하기
    fun setModifyData(){
        isModify = true

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



    private fun setPermission(v: View) {
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

    private val REQUEST_CODE_OPEN_MAP_SEARCH = 44
    fun goMapSearchActivity(v: View){
        val activity = v.context.getActivity()
        val mapSearchIntent = Intent(activity, MapSearchActivity::class.java)
        activity?.startActivityForResult(mapSearchIntent, REQUEST_CODE_OPEN_MAP_SEARCH)
    }

    fun activityFinish(v: View) {
        val activity = v.context.getActivity()
        activity?.finish()
    }

    private fun StringtoDate(str: String, dateFormat: String = "yyyy년 M월 d일", timeZone: TimeZone = TimeZone.getDefault()): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(str)
    }
}