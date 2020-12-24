package lej.happy.fooddiary.ui

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import lej.happy.fooddiary.Activity.AddPostActivity
import lej.happy.fooddiary.Fragment.InfoFragment
import lej.happy.fooddiary.Fragment.ReviewFragment
import lej.happy.fooddiary.Helper.DatePickerDialog
import lej.happy.fooddiary.R
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.ui.taste.TasteFragment
import lej.happy.fooddiary.ui.time.TimeFragment
import lej.happy.fooddiary.util.CameraUtil
import lej.happy.fooddiary.util.getActivity
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel : BaseViewModel(){

    private val TAG = "MainViewModel"

    private var nowFragment = 0

    lateinit var homeFragment: TimeFragment
    lateinit var reviewFragment: ReviewFragment
    lateinit var tasteFragment: TasteFragment
    lateinit var infoFragment: InfoFragment

    val date = BarDate()

    class BarDate(){
        var year = 0
        var month = 0
        var isAll = false

        init {
            year = Integer.parseInt(SimpleDateFormat("yyyy", Locale.KOREA).format(Date()))
            month = Integer.parseInt(SimpleDateFormat("M", Locale.KOREA).format(Date()))
        }

        override fun toString(): String {

            if(isAll){
                return "" + year + "년 " + month + "월"
            }
            return "All"

        }
    }


    private val _barDate = MutableLiveData<BarDate>()
    val barDate : LiveData<BarDate>
        get() = _barDate


    fun getFragment(num: Int) : Fragment {

        nowFragment = num

        when(nowFragment){
            R.id.nav_home -> {
                if(!::homeFragment.isInitialized) homeFragment = TimeFragment()
                return homeFragment
            }
            R.id.nav_review -> {
                if(!::reviewFragment.isInitialized) reviewFragment = ReviewFragment()
                return reviewFragment
            }
            R.id.nav_taste -> {
                if(!::tasteFragment.isInitialized) tasteFragment = TasteFragment()
                return tasteFragment
            }
            R.id.nav_info -> {
                if(!::infoFragment.isInitialized) infoFragment = InfoFragment()
                return infoFragment
            }
        }

        return homeFragment
    }

    val REQUEST_REFRESH_POST = 77
    fun newPost(view: View){
        val activity = view.context.getActivity()
        Intent(activity, AddPostActivity::class.java).also {
            activity?.startActivityForResult(it, REQUEST_REFRESH_POST)
        }

    }

    fun refreshHome(requestCode: Int, resultCode: Int, data: Intent?){
        when(nowFragment){
            R.id.nav_home -> {
                homeFragment.onActivityResult(requestCode, resultCode, data)
            }
            R.id.nav_review -> {
                reviewFragment.onActivityResult(requestCode, resultCode, data)
            }
            R.id.nav_taste -> {
                tasteFragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }


    fun showDateSelectDialog(view: View){
        val focusDialog = DatePickerDialog(view.context)
        focusDialog.setDialogListener { isMonth, year, month ->

            date.year = year
            date.month = month
            date.isAll = isMonth

            if(nowFragment == R.id.nav_home){
                homeFragment.setHomeDate(isMonth, year.toString(), month.toString())
            }
            _barDate.value = date

        }

        focusDialog.showDialog(date.year, date.month)
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

}