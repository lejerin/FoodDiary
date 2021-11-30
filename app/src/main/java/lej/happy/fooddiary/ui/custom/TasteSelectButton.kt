package lej.happy.fooddiary.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.SingleLiveEvent
import lej.happy.fooddiary.data.model.BarDate
import lej.happy.fooddiary.databinding.TasteButtonsLayoutBinding
import lej.happy.fooddiary.databinding.TimeButtonsLayoutBinding

class TasteSelectButton @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null, defStyleAttr: Int=0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private val binding : TasteButtonsLayoutBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.taste_buttons_layout,
        this,
        true
    )

    private val listBtn = listOf(binding.bestEmotionLayout, binding.goodEmotionLayout, binding.badEmotionLayout)

    var selectSingleLiveEvent = SingleLiveEvent<Int>()

    var selectedNum = 0
        set(value) {
            when(value){
                1 -> click(1)
                2 -> click(2)
                3 -> click(3)
            }
            selectSingleLiveEvent.value = value
            field = value
        }

    init {
        binding.bestEmotionLayout.setOnClickListener {
            selectedNum = 1
        }

        binding.goodEmotionLayout.setOnClickListener {
            selectedNum = 2
        }

        binding.badEmotionLayout.setOnClickListener {
            selectedNum = 3
        }
    }

    private fun click(pos: Int) {
        if (pos in 1..3) {
            for (i in listBtn.indices) {
                listBtn[i].isSelected = (i == pos - 1)
            }
        }
    }
}