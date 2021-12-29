package lej.happy.fooddiary.ui.main

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.UpdateEvent
import lej.happy.fooddiary.databinding.ActivityMainBinding
import lej.happy.fooddiary.ui.base.BaseActivity
import lej.happy.fooddiary.ui.date.DateFragment
import lej.happy.fooddiary.ui.info.InfoFragment
import lej.happy.fooddiary.ui.location.LocationFragment
import lej.happy.fooddiary.ui.rate.RateFragment
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import lej.happy.fooddiary.R
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import lej.happy.fooddiary.camera.PhotoFileUpload
import lej.happy.fooddiary.data.local.prefs.UserPrefs
import lej.happy.fooddiary.ui.custom.UserNameDialog
import lej.happy.fooddiary.utils.CameraUtils
import lej.happy.fooddiary.utils.ImageUtils
import lej.happy.fooddiary.utils.UiUtils
import org.koin.java.KoinJavaComponent.inject
import kotlin.math.abs


class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val TAG = MainActivity::class.java.simpleName

    override val layoutResourceId: Int
        get() = R.layout.activity_main

    private val mMainViewModel: MainViewModel by viewModel()

    private val mUserPref by inject(UserPrefs::class.java)
    private val photoFileUpload: PhotoFileUpload by inject(PhotoFileUpload::class.java)
    private val cameraUtils: CameraUtils by inject(CameraUtils::class.java)

    private var isExpanded = false

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val fabNewPost by lazy { binding.mainView.fabNewPost }
    private val dateChoiceButton by lazy { binding.mainView.ibDownArrow }
    private val drawerLayout by lazy { binding.dlMain }
    private val tvDate by lazy { binding.mainView.tvTitle }

    override fun initStartView() {
        binding.activity = this@MainActivity
        binding.viewModel = mMainViewModel
        initAppBar()
        initDrawerLayout()
        initHeaderView()
        initDateChoice()
        initObserver()

        // 날짜 갱신
        mMainViewModel.dateLiveEvent.value?.let { binding.mainView.customDateChoiceLayout.initDate(it) }
        replaceFragment(BaseValue.LOAD_DATE_FRAGMENT_TAG)
    }

    private fun initObserver() {
        binding.mainView.customDateChoiceLayout.initChoiceListener {
            mMainViewModel.dateLiveEvent.value = it
            openOrCloseDateAppBar(false)
        }
        binding.mainView.customRateChoiceLayout.selectSingleLiveEvent.observe(this@MainActivity) {
            it?.let { taste -> mMainViewModel.tasteLiveEvent.value = taste }
            when (it) {
                1 -> { setAppBarTitle(getString(R.string.taste_type_1)) }
                2 -> { setAppBarTitle(getString(R.string.taste_type_2)) }
                3 -> { setAppBarTitle(getString(R.string.taste_type_3)) }
            }
            openOrCloseDateAppBar(false)
        }
    }

    fun setAppBarTitle(title: String) {
        tvDate.text = title
    }

    private fun initAppBar() {
        //툴바, 앱바, navigation drawer
        setSupportActionBar(binding.mainView.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerToggle = ActionBarDrawerToggle(
            this, binding.dlMain, binding.mainView.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                enableViews(false)
                tvDate.text = getString(R.string.location)
            } else {
                enableViews(true)
            }
        }
    }

    private fun initHeaderView() {
        val navHeaderView = binding.nvMain.getHeaderView(0)
        val tvHeaderImg = navHeaderView.findViewById<ImageView>(R.id.user_img).apply {
            clipToOutline = true
            setOnClickListener {
                requestPermission()
            }
        }
        mUserPref.userImg?.let {
            tvHeaderImg.setImageBitmap(ImageUtils.convert(it))
        }

        navHeaderView.findViewById<TextView>(R.id.user_name_text).apply {
            text =  mUserPref.userName
            setOnClickListener {
                val dlg = UserNameDialog(this@MainActivity)
                dlg.setDialogListener(object : UserNameDialog.UserNameDialogListener{
                    override fun onPositiveClicked(str: String) {
                        mUserPref.userName = str
                        text = str

                    }
                })
                dlg.start(text.toString())
            }
        }
    }

    private fun initDrawerLayout() {
        binding.nvMain.apply {
            setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_review, R.id.nav_info -> {
                        openOrCloseDateAppBar(false)
                        fabNewPost.visibility = View.INVISIBLE
                        dateChoiceButton.visibility = View.INVISIBLE
                    }
                    R.id.nav_taste -> {
                        openOrCloseDateAppBar(false)
                        binding.mainView.customRateChoiceLayout.visibility = View.VISIBLE
                        binding.mainView.customDateChoiceLayout.visibility = View.GONE
                        binding.mainView.customRateChoiceLayout.selectedNum = 1
                        fabNewPost.visibility = View.INVISIBLE
                        dateChoiceButton.visibility = View.VISIBLE
                    }
                    R.id.nav_mail -> {  }
                    R.id.nav_home -> {
                        openOrCloseDateAppBar(false)
                        binding.mainView.customRateChoiceLayout.visibility = View.GONE
                        binding.mainView.customDateChoiceLayout.visibility = View.VISIBLE
                        fabNewPost.visibility = View.VISIBLE
                        dateChoiceButton.visibility = View.VISIBLE
                    }
                }
                when (item.itemId) {
                    R.id.nav_home -> {
                        setAppBarTitle(getString(R.string.all))
                        binding.mainView.selectDateLayout.isEnabled = true
                        replaceFragment(BaseValue.LOAD_DATE_FRAGMENT_TAG)
                    }
                    R.id.nav_review -> {
                        setAppBarTitle(getString(R.string.location))
                        binding.mainView.selectDateLayout.isEnabled = false
                        replaceFragment(BaseValue.LOAD_LOCATION_FRAGMENT_TAG)
                    }
                    R.id.nav_taste -> {
                        setAppBarTitle(getString(R.string.taste_type_1))
                        binding.mainView.selectDateLayout.isEnabled = true
                        replaceFragment(BaseValue.LOAD_RATE_FRAGMENT_TAG)
                    }
                    R.id.nav_info -> {
                        binding.mainView.selectDateLayout.isEnabled = false
                        setAppBarTitle(getString(R.string.information))
                        replaceFragment(BaseValue.LOAD_INFO_FRAGMENT_TAG)
                    }
                    R.id.nav_mail -> {
                        sendMail()
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            menu.getItem(0).isChecked = true
        }
    }

    fun onClickOfSetOrder() {
        val popup = PopupMenu(this@MainActivity, binding.mainView.appBarSortBtn)
        popup.inflate(R.menu.sort_item)
        popup.setOnMenuItemClickListener { item ->
            UpdateEvent.mOrderUpdateDataObs.onNext(
                when (item.itemId) {
                    R.id.newest -> BaseValue.ORDER_NEWEST
                    R.id.oldest -> BaseValue.ORDER_OLDEST
                    else -> BaseValue.ORDER_NEWEST
                }
            )
            true
        }
        popup.show()
    }

    private fun getFragmentTag(tag: String): Fragment = when (tag) {
        BaseValue.LOAD_DATE_FRAGMENT_TAG -> DateFragment()
        BaseValue.LOAD_LOCATION_FRAGMENT_TAG -> LocationFragment()
        BaseValue.LOAD_RATE_FRAGMENT_TAG -> RateFragment()
        BaseValue.LOAD_INFO_FRAGMENT_TAG -> InfoFragment()
        else -> DateFragment()
    }

    private fun replaceFragment(tag: String) {
        Log.i(TAG, "replaceFragment : $tag")
        val fragment = supportFragmentManager.findFragmentByTag(tag) ?: getFragmentTag(tag)
        if (fragment.isVisible) {
            Log.i(TAG, " Fragment Already visible : ${fragment.javaClass.simpleName}")
            return
        } else {
            supportFragmentManager.commit(true) {
                replace(R.id.fcv_main, fragment, tag)
                setReorderingAllowed(true)
            }
        }
    }

    private fun enableViews(enable: Boolean) {
        drawerLayout.setDrawerLockMode(if (enable) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED)

        if (enable) {
            drawerToggle.isDrawerIndicatorEnabled = false
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            drawerToggle.isDrawerIndicatorEnabled = true
        }

        drawerToggle.toolbarNavigationClickListener =
            if (enable) {
                View.OnClickListener { onBackPressed() }
            } else {
                null
            }
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {

        when {
            supportFragmentManager.backStackEntryCount > 0 -> {
                supportFragmentManager.popBackStack()
            }
            isExpanded -> {
                openOrCloseDateAppBar(false)
            }
            else -> {
                //0인경우
                if (binding.dlMain.isDrawerOpen(GravityCompat.START)) {
                    binding.dlMain.closeDrawer(GravityCompat.START)
                    return
                }
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed()
                    return
                }
                this.doubleBackToExitPressedOnce = true

                Toast.makeText(this, "한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BaseValue.ACTIVITY_RESULT_NEW_POST -> {
                    UpdateEvent.mRefreshUpdateDataObs.onNext(true)
                }
                BaseValue.ACTIVITY_RESULT_VIEW_POST -> {
                    UpdateEvent.mRefreshUpdateDataObs.onNext(true)
                }
            }
        }
    }

    private fun initDateChoice() {
        binding.mainView.selectDateLayout.setOnClickListener {
            openOrCloseDateAppBar(null)
        }
        binding.mainView.appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                //  Collapse
                if (isExpanded) {
                    Log.i(TAG, "Collapse")
                    binding.mainView.dateDialog.visibility = View.GONE
                    isExpanded = false
                }
            }
        })
    }

    private fun sendMail() {
        try {
            startActivity(mMainViewModel.getMailIntent(this@MainActivity))
        } catch (nf: ActivityNotFoundException) {
            nf.printStackTrace()
            UiUtils.showCenterToast(this@MainActivity, getString(R.string.toast_email_activity_not_found))
        }
        catch (e: Exception) {
            e.printStackTrace()
            UiUtils.showCenterToast(this@MainActivity, getString(R.string.toast_email_error))
        }
    }

    private fun closeSlideAnimation() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_out).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    binding.mainView.dateDialog.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {}
            })
        }
        binding.mainView.dateDialog.startAnimation(anim)
    }

    private fun openOrCloseDateAppBar(enable: Boolean?) {
        when {
            (enable == null && isExpanded) || (enable == false) -> {
                // 접기
                closeSlideAnimation()
                isExpanded = false
            }
            else -> {
                binding.mainView.dateDialog.visibility = View.VISIBLE
                binding.mainView.appBarLayout.setExpanded(true, true)
                isExpanded = true
            }
        }
    }

    override fun afterPermission() {
        pickPhoto()
    }

    //onActivityResult Deprecated
    private val fileResultActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult() // ◀ StartActivityForResult 처리를 담당
    ) { activityResult ->
        photoFileUpload.fileChooserResult(activityResult.resultCode, activityResult.data)?.let {
            val bitmap = cameraUtils.decodeSampledBitmapFromResource(it.toString(), 300, 300)
            mUserPref.userImg = ImageUtils.convert(bitmap)
            binding.nvMain.getHeaderView(0).findViewById<ImageView>(R.id.user_img).setImageBitmap(bitmap)
        }
    }

    private fun pickPhoto() {
        val intent : Intent = photoFileUpload.createShowFileChooserIntent(this@MainActivity)
        fileResultActivity.launch(intent)
    }
}
