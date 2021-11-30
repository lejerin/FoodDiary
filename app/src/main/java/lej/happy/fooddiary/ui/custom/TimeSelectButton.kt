package lej.happy.fooddiary.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.SingleLiveEvent
import lej.happy.fooddiary.databinding.TimeButtonsLayoutBinding

class TimeSelectButton @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null, defStyleAttr: Int=0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private val binding : TimeButtonsLayoutBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.time_buttons_layout,
        this,
        true
    )

    private val listBtn = listOf(R.id.radioButton1, R.id.radioButton2, R.id.radioButton3, R.id.radioButton4, R.id.radioButton5)

    var selectSingleLiveEvent= SingleLiveEvent<Int>()

    var selectedNum = 0
        set(value) {
            selectSingleLiveEvent.value = value
            field = value
        }

    fun clickButton(num: Int) {
        binding.radioGroup.check(listBtn[num - 1])
    }

    init {
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            //선택시간대 고르기 (아침, 점심, 저녁, 간식, 야식)
            Log.i("happy", "checkId $checkedId")
            when(checkedId){
                R.id.radioButton1 -> selectedNum = 1
                R.id.radioButton2 -> selectedNum = 2
                R.id.radioButton3 -> selectedNum = 3
                R.id.radioButton4 -> selectedNum = 4
                R.id.radioButton5 -> selectedNum = 5
            }
        }
    }
}