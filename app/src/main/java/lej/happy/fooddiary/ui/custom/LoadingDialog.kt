package lej.happy.fooddiary.Helper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import lej.happy.fooddiary.R

class LoadingDialog
constructor(context: Context) : Dialog(context){

    init {
        setCanceledOnTouchOutside(false)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setContentView(R.layout.loading_dialog)

        val img_loading_frame = this.findViewById(R.id.iv_frame_loading) as ImageView
        val frameAnimation = img_loading_frame.background as AnimationDrawable
        img_loading_frame.post(Runnable {
            kotlin.run {
                frameAnimation.start()
            }
        })

    }
}