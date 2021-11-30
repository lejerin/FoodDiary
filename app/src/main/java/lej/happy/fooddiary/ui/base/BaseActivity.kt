package lej.happy.fooddiary.ui.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.databinding.DataBindingUtil
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import lej.happy.fooddiary.R
import lej.happy.fooddiary.utils.UiUtils

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity() {

    lateinit var binding: T

    abstract val layoutResourceId: Int

    abstract fun initStartView()
    abstract fun afterPermission()

    val mDialogList = mutableListOf<AlertDialog>()
    val mDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutResourceId)
        binding.lifecycleOwner = this@BaseActivity

        initStartView()
    }

    override fun onDestroy() {
        super.onDestroy()
        for (dialog in mDialogList) {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        mDisposable.dispose()
    }

    //********************************************************************************
    //  권한 요청
    //********************************************************************************

    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_WRITE_STORAGE_PERMISSION = 102

    // 권한 관련
    fun requestPermission() {
        if (!checkPermission()) {
            // 권한 없음
            val permissionReadPhoneState = checkSelfPermission(Manifest.permission.CAMERA)
            val permissionWriteExternalStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            when {
                permissionReadPhoneState != PackageManager.PERMISSION_GRANTED -> {  // 통화 권한 요청
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
                }
                permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED -> {  // 저장장치 Write 요청 : android 11 부터는 필요 없음
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE_PERMISSION)
                }
            }
        } else {
            afterPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                checkPermission(grantResults, Manifest.permission.CAMERA)
            }
            REQUEST_WRITE_STORAGE_PERMISSION -> {
                checkPermission(grantResults, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun checkPermission(grantResults: IntArray, permission: String) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted :)
            requestPermission()
        } else {
            // permission was not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@BaseActivity, permission)) {
                showDialogPermissionRationale(permission)
            } else {
                showDialogPermissionDenied(permission)
            }
        }
    }

    // 사용자가 거부를 했지만 권한 요청이 가능할 때 권한이 필요한 이유를 설명
    private fun showDialogPermissionRationale(permission: String) {
        val dialog = UiUtils.showAlertDialog(this,
            getString(R.string.request_permission_title),
            getString(R.string.request_permission_message_photo),
            getString(R.string.dialog_positive_text)
        ) { dialog, _ ->
            dialog.dismiss()
            requestPermission()
        }?.setCancelable(false)
            ?.show()
        dialog?.let {
            mDialogList.add(it)
        }
    }

    // 사용자가 완전히 거부하여 더 이상 권한 요청이 불가능 할 때
    private fun showDialogPermissionDenied(permission: String) {
        val dialog = UiUtils.showConfirmDialog(this,
            when (permission) {
                Manifest.permission.CAMERA -> getString(R.string.request_permission_denied_title_camera_state)
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> getString(R.string.request_permission_denied_title_storage_access)
                else -> ""
            },
            when (permission) {
                Manifest.permission.READ_PHONE_STATE -> getString(R.string.request_permission_denied_message_camera_state)
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> getString(R.string.request_permission_denied_message_storage_access)
                else -> ""
            },
            getString(R.string.dialog_positive_text),
            {dialog,_ ->
                dialog.dismiss()
                startPermissionSettingPage()
            },
            getString(R.string.dialog_negative_text),
            {dialog,_ ->
                dialog.dismiss()
                finish()
            }
        )?.setCancelable(false)
            ?.show()
        dialog?.let {
            mDialogList.add(it)
        }
    }

    private var isOnResumeFromPermissionSetting = false
    private fun startPermissionSettingPage() {
        val permissionsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        permissionsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        isOnResumeFromPermissionSetting = true
        startActivity(permissionsIntent)
    }

    override fun onResume() {
        super.onResume()
        if (isOnResumeFromPermissionSetting) {
            requestPermission()
            isOnResumeFromPermissionSetting = false
        }
    }

    private fun checkPermission() : Boolean {
        return (ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.CAMERA ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }
}