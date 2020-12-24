package lej.happy.fooddiary.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.taste_button_layout.view.*
import lej.happy.fooddiary.R


open class CustomImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null, defStyleAttr: Int=0)
    : LinearLayout(context, attrs, defStyleAttr){

    companion object{

    }

    init{
        inflate(context, R.layout.taste_button_layout, this)
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        attrs?.run {
            context.obtainStyledAttributes(this, R.styleable.CustomImageButton)
        }?.run {

            layout_background.background = getDrawable(R.styleable.CustomImageButton_layout_background)
            img.setImageDrawable(getDrawable(R.styleable.CustomImageButton_img))
            text.text = getText(R.styleable.CustomImageButton_text)

        }
    }


   override fun setSelected(isSelected: Boolean){
       layout_background.isSelected = isSelected

       if(isSelected){
           img.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
           text.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
       }else{
           img.setColorFilter(ContextCompat.getColor(context, R.color.brightGray))
           text.setTextColor(ContextCompat.getColor(context, R.color.brightGray))
       }
   }

}