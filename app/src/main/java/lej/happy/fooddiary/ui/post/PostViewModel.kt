package lej.happy.fooddiary.ui.post

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteBlobTooBigException
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.local.db.AppDatabase
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.data.local.db.entity.Thumb
import lej.happy.fooddiary.data.local.repository.PostRepos
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.ui.map.MapSearchActivity
import lej.happy.fooddiary.utils.*
import lej.happy.fooddiary.utils.DateUtils.toCustomDate
import org.koin.java.KoinJavaComponent.inject
import java.util.*

class PostViewModel : BaseViewModel() {

    /** Repos */
    private val mRepos by inject(PostRepos::class.java)

    private val cameraUtils: CameraUtils by inject(CameraUtils::class.java)

    val dateStr = MutableLiveData<String>()

    var post = Post()
    var thumb = Thumb()

    //선택한 현재 사진 뷰페이저 index num
    private val _message = MutableLiveData<String>()
    val message : LiveData<String>
        get() = _message

    val isLoading = MutableLiveData(false)

    private val saveMutex = Mutex()

    suspend fun getPostWithId(id: Long): Post {
        val job = CoroutineScope(Dispatchers.IO).async {
            mRepos.getPostId(id)
        }
        return job.await()
    }

    fun showSelectDateDialog(v: View){
        val c = Calendar.getInstance()
        val nowYear = c.get(Calendar.YEAR)
        val nowMonth = c.get(Calendar.MONTH)
        val nowDay = c.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(v.context, { _, year, monthOfYear, dayOfMonth ->
            val monthText = (monthOfYear + 1).toString().let {
                if (it.length == 1) {
                    "0$it"
                } else {
                    it
                }
            }
            val dayText = dayOfMonth.toString().let {
                if (it.length == 1) {
                    "0$it"
                } else {
                    it
                }
            }
            dateStr.value = "" + year + "년 " + monthText + "월 " + dayText + "일"
            post.date = dateStr.value!!.toCustomDate()
        }, nowYear, nowMonth, nowDay)
            .show()
    }

    fun activityFinish(v: View) {
        val activity = v.context.getActivity()
        activity?.finish()
    }

    suspend fun saveData(photoList: List<String>, context: Context) {
        if (saveMutex.isLocked) {
            return
        }
        saveMutex.withLock {
            try {
                setLoading(true)
                if (photoList.isNotEmpty()) {
                    setPhotoToBitmap(photoList, context)
                    if (isModify) {
                        saveModifyData(context)
                    } else {
                        saveNewData(context)
                    }
                } else {
                    sendErrorMessageFromBackground("최소 사진 한장을 추가해주세요.")
                }
            } catch (e: Exception) {
                sendErrorMessageFromBackground("저장 중 오류가 발생하였습니다.")
            }
        }
    }

    private fun setPhotoToBitmap(photoList: List<String>, context: Context) {
        if (photoList.isNotEmpty() && cameraUtils.checkUriValid(photoList[0])){
            post.photo1 = photoList[0]
            thumb.photo1_bitmap = ImageUtil.convert(cameraUtils.decodeSampledBitmapFromResource(photoList[0], 300, 300))
        }

        if (photoList.size > 1 && cameraUtils.checkUriValid(photoList[1])) {
            post.photo2 = photoList[1]
            thumb.photo2_bitmap = ImageUtil.convert(cameraUtils.decodeSampledBitmapFromResource(photoList[1], 300, 300))
        } else {
            post.photo2 = null
            thumb.photo2_bitmap = null
        }

        if (photoList.size > 2 && cameraUtils.checkUriValid(photoList[2])) {
            post.photo3 = photoList[2]
            thumb.photo3_bitmap = ImageUtil.convert(cameraUtils.decodeSampledBitmapFromResource(photoList[2], 300, 300))
        } else {
            post.photo3 = null
            thumb.photo3_bitmap = null
        }

        if (photoList.size > 3 && cameraUtils.checkUriValid(photoList[3])) {
            post.photo4 = photoList[3]
            thumb.photo4_bitmap = ImageUtil.convert(cameraUtils.decodeSampledBitmapFromResource(photoList[3], 300, 300))
        } else {
            post.photo4 = null
            thumb.photo4_bitmap = null
        }
    }

    private fun saveNewData(context: Context) {
        val getDb = AppDatabase.getInstance(context)
        try {
            post.count = getDb.postDao().getCount(post.date!!) + 1
            thumb.id = getDb.postDao().insert(post)
            getDb.thumbDao().insert(thumb)
            setLoading(false)
            context.getActivity().let {
                if (it != null) {
                    it.setResult(Activity.RESULT_OK)
                    it.finish()
                } else {
                    sendErrorMessageFromBackground("오류가 발생하였습니다.")
                }
            }
        } catch (e: Exception) {
            sendErrorMessageFromBackground("저장 중 오류가 발생하였습니다.")
        }
    }


    private fun saveModifyData(context: Context) {
        val getDb = AppDatabase.getInstance(context)
        try {
            getDb.postDao().update(post)
            thumb.id = post.id
            getDb.thumbDao().update(thumb)
            val resultIntent = Intent()
            resultIntent.putExtra("modifyPost", post)
            setLoading(false)
            context.getActivity().let {
                if (it != null) {
                    it.setResult(Activity.RESULT_OK, resultIntent)
                    it.finish()
                } else {
                    sendErrorMessageFromBackground("오류가 발생하였습니다.")
                }
            }
        } catch (e: Exception) {
            sendErrorMessageFromBackground("저장 중 오류가 발생하였습니다.")
        }
    }

    private var isModify = false
    //수정일경우 데이터 입력하기
    fun setModifyData(){
        isModify = true
        //해당 년도와 월에 대해서만 date, count 순서대로 가져옴
        setJob(
            Coroutines.ioThenMain(
                {
                    try {
                        mRepos.getThumbId(post.id!!)
                    } catch (e: SQLiteBlobTooBigException) {
                        e.printStackTrace()
                        Thumb()
                    }
                },
                {
                    if (it != null) {
                        thumb = it
                    }
                }
            ))
    }

    private fun setLoading(value: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            isLoading.value = value
        }
    }

    private fun sendErrorMessageFromBackground(msg: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _message.value = msg
        }
        setLoading(false)
    }

    fun goMapSearchActivity(v: View){
        val activity = v.context.getActivity()
        val mapSearchIntent = Intent(activity, MapSearchActivity::class.java)
        activity?.startActivityForResult(mapSearchIntent, BaseValue.REQUEST_CODE_OPEN_MAP_SEARCH)
    }

}