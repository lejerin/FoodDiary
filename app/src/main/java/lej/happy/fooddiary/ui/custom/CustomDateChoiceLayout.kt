package lej.happy.fooddiary.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.model.BarDate
import lej.happy.fooddiary.databinding.DateChoiceLayoutBinding
import lej.happy.fooddiary.databinding.ItemMonthDateBinding

class CustomDateChoiceLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null, defStyleAttr: Int=0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private var beforeClickPos: Int? = null
    private var dateResult = BarDate()
    private var choiceListener: (BarDate) -> Unit = { }

    private val binding: DateChoiceLayoutBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.date_choice_layout,
        this,
        true
    )

    private val monthAdapter = ViewPagerDateAdapter {
        binding.vpMonth.currentItem = it - 2
    }

    init {
        initMonthAdapter()
        initClickListener()
    }

    fun initChoiceListener(action : (BarDate) -> Unit) {
        choiceListener = action
    }

    fun initDate(date: BarDate) {
        dateResult = date
        binding.year = date.year.toString()
        val pos = date.month - 1
        if (date.isAll) {
            monthAdapter.selectedBtn(null)
        } else {
            monthAdapter.selectedBtn(pos)
        }
        binding.vpMonth.currentItem = pos
    }


    private fun initMonthAdapter() {
        binding.vpMonth.apply {
            adapter = monthAdapter
        }
    }

    private fun initClickListener() {
        binding.prevYearBtn.setOnClickListener {
            dateResult.year = dateResult.year.minus(1)
            binding.pickerYearText.text = dateResult.year.toString()
            monthAdapter.selectedBtn(-1)
        }
        binding.nextYearBtn.setOnClickListener {
            dateResult.year = dateResult.year.plus(1)
            binding.pickerYearText.text = dateResult.year.toString()
            monthAdapter.selectedBtn(-1)
        }
        binding.btnAll.setOnClickListener {
            initDate(BarDate())
            sendResult()
        }
    }

    private fun sendResult() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            CoroutineScope(Dispatchers.Main).launch {
                choiceListener.invoke(dateResult)
            }
        }
    }

    inner class ViewPagerDateAdapter(private val action : (Int) -> Unit): PagerAdapter() {

        private val monthList = mutableListOf(
            Pair("1월", false),
            Pair("2월", false),
            Pair("3월", false),
            Pair("4월", false),
            Pair("5월", false),
            Pair("6월", false),
            Pair("7월", false),
            Pair("8월", false),
            Pair("9월", false),
            Pair("10월", false),
            Pair("11월", false),
            Pair("12월", false)
        )

        override fun getCount(): Int = monthList.size

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val binding: ItemMonthDateBinding =
                DataBindingUtil.inflate(LayoutInflater.from(container.context), R.layout.item_month_date, container, false)

            binding.value = monthList[position].first
            binding.isSelected = monthList[position].second
            binding.root.setOnClickListener {
                selectedBtn(position)
                action.invoke(position)
                dateResult.isAll = false
                dateResult.month = position + 1
                sendResult()
            }

            container.addView(binding.root)
            return binding.root
        }

        fun selectedBtn(pos: Int?) {
            beforeClickPos.let {
                if (it == null) {
                    binding.allBackground.isSelected = false
                } else {
                    monthList[it] = Pair(monthList[it].first, false)
                }
            }

            beforeClickPos = pos

            when (pos) {
                null -> { binding.allBackground.isSelected = true }
                -1 -> { beforeClickPos = null }
                else -> { monthList[pos] = Pair(monthList[pos].first, true) }
            }

            notifyDataSetChanged()
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View?)
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE;
        }

        override fun getPageWidth(position: Int): Float {
            return 0.2f
        }
    }
}