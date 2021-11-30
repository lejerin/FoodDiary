package lej.happy.fooddiary.ui.rate

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.UpdateEvent
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.data.model.BarDate
import lej.happy.fooddiary.data.model.ReviewRank
import lej.happy.fooddiary.databinding.FragmentDateBinding
import lej.happy.fooddiary.databinding.FragmentLocationBinding
import lej.happy.fooddiary.databinding.FragmentRateBinding
import lej.happy.fooddiary.ui.adapter.LocationAdapter
import lej.happy.fooddiary.ui.adapter.PhotoGridAdapter
import lej.happy.fooddiary.ui.base.BaseFragment
import lej.happy.fooddiary.ui.base.BaseRVFragment
import lej.happy.fooddiary.ui.date.DateFragment
import lej.happy.fooddiary.ui.location.LocationViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class RateFragment : BaseRVFragment<FragmentRateBinding, Post>() {
    private val TAG = RateFragment::class.java.simpleName

    override val layoutResourceId: Int
        get() = R.layout.fragment_rate

    private val dataList: MutableList<Post> = mutableListOf()
    override val mAdapter by lazy { PhotoGridAdapter(dataList) }
    override val mRecyclerView by lazy { binding.tsReview }

    override val mEmptyView by lazy { binding.tasteNoDataInRecyclerview }

    private val mRateViewModel: RateViewModel by viewModel()

    val clicksListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mRateViewModel.mutex.isLocked || mRateViewModel.isEnd) {
                return
            }

            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)?.findLastCompletelyVisibleItemPosition()
            val itemTotalCount = recyclerView.adapter?.itemCount

            if (lastVisibleItemPosition != null && itemTotalCount != null) {
                if (lastVisibleItemPosition >= itemTotalCount - (BaseValue.limit / 2)) { // 스크롤이 끝에 도달했는지 확인
                    mRateViewModel.getDateData(BaseValue.DATA_TYPE.ADD)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainViewModel.tasteLiveEvent.value = 1
    }

    override fun initBinding() {
        //super.initBinding()
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        mRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = mAdapter
        }

        binding.fragment = this@RateFragment
        initObserver()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        mRateViewModel.newDataList.observe(viewLifecycleOwner, {
            Log.i(TAG, "newDataList ${it?.second?.size}")
            it?.let { pair ->
                CoroutineScope(Dispatchers.Main).launch {
                    mAdapter.updateList(pair)
                    checkEmptyDataUi()
                    if (pair.first == BaseValue.DATA_TYPE.INIT) {
                        mRecyclerView.scrollToPosition(0)
                    }
                }
            }
        })
        mMainViewModel.tasteLiveEvent.observe(viewLifecycleOwner, {
            Log.i(TAG, "tasteLiveEvent $it")
            initRecyclerView()
            mRateViewModel.taste = it
            mRateViewModel.getDateData(BaseValue.DATA_TYPE.INIT)
        })
        mDisposable.add(
            UpdateEvent.mOrderUpdateDataObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    initRecyclerView()
                    mRateViewModel.isDESC = (it == BaseValue.ORDER_NEWEST)
                    mRateViewModel.getDateData(BaseValue.DATA_TYPE.INIT)
                })
        mDisposable.add(
            UpdateEvent.mRefreshUpdateDataObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    initRecyclerView()
                    mRateViewModel.getDateData(BaseValue.DATA_TYPE.INIT)
                }
        )
    }

    private fun initRecyclerView() {
        Log.i("getData", "initRecyclerView")
        mRateViewModel.isDESC = true
        mRateViewModel.isEnd = false
        mRateViewModel.page = 0
    }
}