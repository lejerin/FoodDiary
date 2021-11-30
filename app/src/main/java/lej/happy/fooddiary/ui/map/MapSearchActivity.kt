package lej.happy.fooddiary.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.model.LocalMapData
import lej.happy.fooddiary.data.remote.model.Document
import lej.happy.fooddiary.data.remote.repository.MapRepos
import lej.happy.fooddiary.databinding.ActivityMapSearchBinding
import lej.happy.fooddiary.ui.adapter.MapListAdapter
import lej.happy.fooddiary.ui.base.BaseActivity
import lej.happy.fooddiary.utils.UiUtils
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapSearchActivity : BaseActivity<ActivityMapSearchBinding>() {

    override val layoutResourceId: Int
        get() = R.layout.activity_map_search

    val mapList = mutableListOf<Document>()
    lateinit var mapView: MapView
    var selectLocation: Document? = null

    private val slidingBar : SlidingUpPanelLayout by lazy { binding.slidingBar }
    private val listView : RecyclerView by lazy { binding.listView }

    /** Repos */
    private val mMapRepos by inject(MapRepos::class.java)

    override fun initStartView() {
        initMapView()
        initSlidingBar()
        initKeywordView()
        initClickListener()
        initRecyclerView()
    }

    private fun initMapView() {
        mapView = MapView(this@MapSearchActivity)
        val mapViewContainer = findViewById<ViewGroup>(R.id.map_view)
        mapView.setZoomLevel(0,false)
        mapViewContainer.addView(mapView)
    }

    private fun initSlidingBar() {
        slidingBar.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
    }

    private fun initKeywordView() {
        binding.kewordSearchText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    startMapSearch()
                }
                else -> return@OnEditorActionListener false
            }
            true
        })
    }

    private fun initRecyclerView() {
        val mapListAdapter = MapListAdapter(mapList).apply {
            setItemClickListener(object : MapListAdapter.OnItemClickListener{
                override fun onClick(v: View, position: Int) {
                    val item = mapList[position]
                    selectLocation = item
                    binding.selectTitleText.text = item.placeName
                    binding.selectLocationBtn.visibility = View.VISIBLE
                    mapViewChange(item.x.toDouble(),item.y.toDouble(),item.placeName)

                }
            })
        }
        listView.apply {
            adapter = mapListAdapter
            layoutManager = LinearLayoutManager(this@MapSearchActivity)
            addItemDecoration(DividerItemDecoration(this@MapSearchActivity, LinearLayoutManager.VERTICAL).apply {
                ContextCompat.getDrawable(this@MapSearchActivity, R.drawable.custom_divider)?.let {
                    setDrawable(it)
                }
            })
        }
    }

    private fun initClickListener() {
        binding.mapSearchBackBtn.setOnClickListener {
            finish()
        }
        binding.removeBtnMapSearch.setOnClickListener {
            binding.kewordSearchText.setText("")
        }
        binding.mapSearchBtn.setOnClickListener {
            startMapSearch()
        }
        binding.selectLocationBtn.setOnClickListener {
            selectLocation()
        }
    }

    private fun startMapSearch() {
        UiUtils.hideKeyboard(this@MapSearchActivity)
        getMapSearchKeyword()
    }

    private fun selectLocation() {
        selectLocation.let { location ->
            if (location != null) {
                val newIntent = Intent()
                newIntent.putExtra("name", location.roadAddressName)
                newIntent.putExtra("x", location.x)
                newIntent.putExtra("y", location.y)
                newIntent.putExtra("roadAddress", location.placeName)
                setResult(RESULT_OK, newIntent)
                finish()
            } else {
                Toast.makeText(this,"장소를 검색하여 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mapViewChange(x: Double, y: Double, name: String){
        mapView.removeAllPOIItems()
        slidingBar.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED

        val newMapPoint = MapPoint.mapPointWithGeoCoord(y,x)
        mapView.setMapCenterPoint(newMapPoint,true)
        val marker = MapPOIItem().apply {
            itemName = name
            tag = 0
            mapPoint = newMapPoint
            markerType = MapPOIItem.MarkerType.RedPin // 기본으로 제공하는 BluePin 마커 모양.
            selectedMarkerType = MapPOIItem.MarkerType.RedPin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        }
        mapView.addPOIItem(marker)
    }

    private fun getMapSearchKeyword(){
        mMapRepos.requestKeywordList(binding.kewordSearchText.text.toString(),
            "KakaoAK 7114008c849d19203b186846030bd6ad").enqueue(object : Callback<LocalMapData> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<LocalMapData>, response: Response<LocalMapData>) {
                if (response.isSuccessful) {
                    response.body()?.documents.let {
                        if (it.isNullOrEmpty()) {
                            UiUtils.showCenterToast(this@MapSearchActivity, "검색 결과가 없습니다.")
                        } else {
                            mapList.clear()
                            mapList.addAll(it)
                            listView.adapter?.notifyDataSetChanged()
                            slidingBar.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                        }
                    }
                } else {
                    UiUtils.showCenterToast(this@MapSearchActivity, "서버 오류입니다.\n${response.code()}\n" +
                            "${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<LocalMapData>, t: Throwable) {
              UiUtils.showCenterToast(this@MapSearchActivity, "서버 오류입니다.\n${t.message}")
            }
        })
    }

    override fun onBackPressed() {
        if(slidingBar.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingBar.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun afterPermission() {

    }
}