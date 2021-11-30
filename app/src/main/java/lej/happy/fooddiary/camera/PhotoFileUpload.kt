package lej.happy.fooddiary.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.ValueCallback
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.content.ContentResolver
import android.content.ContentValues
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.Exception


class PhotoFileUpload(private val context: Context) {
    private val TAG = PhotoFileUpload::class.java.simpleName

    //********************************************************************************
    //  File Chooser
    //********************************************************************************

    var mCameraPhotoPath: String? = null //사진 파일 경로
    var mCameraPhotoUri: Uri? = null  //사진 파일 경로 by Q

    fun createShowFileChooserIntent(context: Context): Intent {
        var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent?.resolveActivity(context.packageManager) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    mCameraPhotoUri = createImageFileAfterQ()
                    if (mCameraPhotoUri != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraPhotoUri)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                var photoFile: File? = null
                try {
                    photoFile = createImageFileBeforeQ()
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                } catch (ex: IOException) {
                    Log.e(TAG, "Image file creation failed$ex")
                }
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.absolutePath
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){  //누가부터 FileProvider 사용
                        val photoUri: Uri = FileProvider.getUriForFile(context, "com.etoos.etoosstudyapp.fileprovider", photoFile)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    }else{
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    }
                } else {
                    takePictureIntent = null
                }
            }
        }

        val contentSelectionIntent = Intent(Intent.ACTION_PICK)
        contentSelectionIntent.type = "image/*"
        contentSelectionIntent.type = MediaStore.Images.Media.CONTENT_TYPE
        val intentArray: Array<Intent?> = arrayOf(takePictureIntent)

        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

        return chooserIntent
    }

    fun fileChooserResult(resultCode: Int, intent: Intent?) : Uri? {
        var results: Array<Uri?>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent == null) {
                //카메라로 찍은 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //안드로이드 11 대응
                    if (mCameraPhotoUri != null) {
                        results = arrayOf(mCameraPhotoUri)
                    }
                } else {
                    if (mCameraPhotoPath != null) {
                        results = arrayOf(Uri.parse(mCameraPhotoPath))
                    }
                }
            } else {
                //앨범 픽한 경우
                intent.dataString?.let {
                    results = arrayOf(Uri.parse(it))
                }
            }
        }
        return results?.get(0)
    }

    // Q(API 29) 이상에서는 MediaStore를 사용해서 외부 저장소에 파일을 저장할 수 있다.
    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFileAfterQ(): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_$timeStamp.jpg"

        val resolver: ContentResolver = context.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    // Create an image file
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFileBeforeQ(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
}