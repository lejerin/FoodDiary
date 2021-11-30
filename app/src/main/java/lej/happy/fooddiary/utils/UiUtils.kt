package lej.happy.fooddiary.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object UiUtils {

    fun hideKeyboard(activity: Activity) {
        val inputManager: InputMethodManager = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // check if no view has focus:
        val currentFocusedView: View? = activity.currentFocus
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    var mToast: Toast? = null
    @SuppressLint("ShowToast")
    fun showCenterToast(context: Context, message: String) {
        mToast?.cancel()
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        mToast?.show()
    }

    /**Two button */
    fun showConfirmDialog(
        context: Context?,
        title : String?,
        message: String?,
        positiveButton: String?,
        positiveListener: DialogInterface.OnClickListener?,
        negativeButton: String?,
        negativeListener: DialogInterface.OnClickListener?
    ): AlertDialog.Builder? {
        context?.let {
            val alert = AlertDialog.Builder(it)
            alert.setTitle(title)
            alert.setMessage(message)
            alert.setPositiveButton(positiveButton, positiveListener)
            alert.setNegativeButton(negativeButton, negativeListener)
            return alert
        }
        return null
    }

    /** One button */
    fun showAlertDialog(
        context: Context?,
        title: String?,
        message: String?,
        positiveButton: String?,
        positiveListener: DialogInterface.OnClickListener?,
    ): AlertDialog.Builder? {
        context?.let {
            val alert = AlertDialog.Builder(it)
            alert.setTitle(title)
            alert.setMessage(message)
            alert.setPositiveButton(positiveButton, positiveListener)
            return alert
        }
        return null
    }
}