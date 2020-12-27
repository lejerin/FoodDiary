package lej.happy.fooddiary.ui.taste

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import lej.happy.fooddiary.ui.time.PhotoGridAdapter
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.ui.custom.CustomImageButton
import lej.happy.fooddiary.util.Coroutines
import java.util.ArrayList

class TasteViewModel(
    private val repository: Repository
) : BaseViewModel() {

    lateinit var tasteAdapter : PhotoGridAdapter

    private var selectedBtn: CustomImageButton? = null
    lateinit var context: Context

    private var isDESC = true
    private var taste = 1
    var isLoading = false

    val tasteList = MutableLiveData<MutableList<Post>>()
    init {
        tasteList.value = ArrayList()
        tasteAdapter =
            PhotoGridAdapter(tasteList.value!!)
    }

    fun selectedBtn(v: View){
        selectedBtn?.isSelected = false
        selectedBtn = v as CustomImageButton
        selectedBtn?.isSelected = true

        when(v.id){
            R.id.best_taste_btn -> taste = 1
            R.id.good_taste_btn -> taste = 2
            R.id.bad_taste_btn -> taste = 3
        }

        getTasteData()
    }

    fun setOrder(order: Boolean){
        isDESC = order
        getTasteData()
    }

    private fun getQuery(): List<Post> {

        return if(isDESC) repository.getTasteDESC(taste)
        else repository.getTasteASC(taste)
    }


    private fun getTasteData(){

        if(!isLoading){

            isLoading = true

            tasteList.value?.clear()

            //해당 년도와 월에 대해서만 date, count 순서대로 가져옴
            newJob(
                Coroutines.ioThenMain(
                    { getQuery()},
                    {
                        println("taste 개수 " + it?.size + "now taste" + taste)
                        isLoading = false
                        tasteList.value?.addAll(it!!)
                        tasteList.value = tasteList.value
                    }
                ))
        }
    }

}
