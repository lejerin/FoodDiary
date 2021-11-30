package lej.happy.fooddiary.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

class CameraUtils(val context: Context) {

    fun checkUriValid(uri: String) : Boolean{
        var ls : InputStream? = null
        try {
            ls = context.contentResolver.openInputStream(Uri.parse(uri))
        }catch (e: Exception){

        }
        if(ls == null)
            return false

        return true
    }

    fun decodeSampledBitmapFromResource(
        uri: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        val mUri = Uri.parse(uri)
        val bitmap = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, mUri))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                MediaStore.Images.Media.getBitmap(context.contentResolver, mUri)
            }
            else -> {
                BitmapFactory.Options().run {
                    // Calculate inSampleSize
                    inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                    // Decode bitmap with inSampleSize set
                    inJustDecodeBounds = false
                    BitmapFactory.decodeStream(
                        context.contentResolver.openInputStream(mUri), null, this)!!
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return imgRotate(mUri, bitmap)
        }
        return bitmap
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun imgRotate(uri: Uri, bitmap: Bitmap) : Bitmap {
        val ins = context.contentResolver.openInputStream(uri)
        val exif = ins?.let { ExifInterface(it) }
        ins?.close()

        Log.i("imgRotate", "imgRotate")

        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when(orientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}