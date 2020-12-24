package lej.happy.fooddiary.ui

import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import lej.happy.fooddiary.ui.custom.ImageUtil
import lej.happy.fooddiary.ui.custom.UserNameDialog
import lej.happy.fooddiary.util.MyApplication
import lej.happy.fooddiary.R
import lej.happy.fooddiary.databinding.ActivityMainBinding
import lej.happy.fooddiary.ui.base.BaseActivity
import lej.happy.fooddiary.ui.review.ReviewDetailFragment
import lej.happy.fooddiary.ui.review.ReviewFragment
import lej.happy.fooddiary.ui.taste.TasteFragment
import lej.happy.fooddiary.ui.time.TimeFragment
import lej.happy.fooddiary.util.CameraUtil


class MainActivity : BaseActivity<ActivityMainBinding>(), NavigationView.OnNavigationItemSelectedListener {


    override val layoutResourceId: Int
        get() = R.layout.activity_main

    private val viewModel: MainViewModel by viewModels()

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var mToolBarNavigationListenerIsRegistered = false

    override fun initStartView() {

        initToolBar()
    }

    override fun initDataBinding() {
        viewDataBinding.viewModel = viewModel
    }

    override fun initAfterBinding() {

        initHeaderView()
        setNewFragment(viewModel.getFragment(R.id.nav_home))



        viewModel.barDate.observe(this, Observer {
            bar_month_text.text = it.toString()
        })

        app_bar_sort_btn.setOnClickListener {
            showPopupMenuOrder()
        }
    }

    //사진
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 10

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK){
            when(requestCode){
                REQUEST_IMAGE_CAPTURE ->{
                    //사진찍은거 파일로 저장하고 가져오기

                    val uri = CameraUtil.getInstance(this).makeBitmap(user_img)
                    CameraUtil.getInstance(this).setImageView(user_img, uri, 150, 150).also {
                        MyApplication.prefs.setString("user_img", ImageUtil.convert(it))
                    }
                }

                REQUEST_IMAGE_PICK ->{
                    if(data != null){
                        CameraUtil.getInstance(this).decodeSampledBitmapFromResource(data.data!!,
                            150, 150).also {
                            MyApplication.prefs.setString("user_img", ImageUtil.convert(it))
                            user_img.setImageBitmap(it)
                        }
                    }
                }

                else ->{
                    viewModel.refreshHome(requestCode, resultCode, data)
                }

            }
        }

    }



    private fun showPopupMenuOrder(){
        val popup = PopupMenu(this@MainActivity, app_bar_sort_btn)
        popup.inflate(R.menu.sort_item)
        //adding click listener
        popup.setOnMenuItemClickListener { item ->

            (item.itemId == R.id.newest).also { v ->
                supportFragmentManager.findFragmentById(R.id.frame_layout)?.let {
                    // the fragment exists
                    when(it){
                        is TimeFragment -> {
                            (it as (TimeFragment)).setOrder(v)
                        }
                        is ReviewFragment -> {
                            (it as (ReviewFragment)).setOrder(v)
                        }
                        is ReviewDetailFragment -> {
                            (it as (ReviewDetailFragment)).setOrder(v)
                        }
                        is TasteFragment -> {
                            (it as (TasteFragment)).setOrder(v)
                        }
                    }

                }
            }
        }
        popup.show()
    }


    private fun enableViews(enable: Boolean) {
        if (enable) {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            drawerToggle.isDrawerIndicatorEnabled = false
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)

            if (!mToolBarNavigationListenerIsRegistered) {
                drawerToggle.toolbarNavigationClickListener = View.OnClickListener { // Doesn't have to be onBackPressed
                    onBackPressed()
                }
                mToolBarNavigationListenerIsRegistered = true
            }
        } else {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            drawerToggle.isDrawerIndicatorEnabled = true
            drawerToggle.toolbarNavigationClickListener = null
            mToolBarNavigationListenerIsRegistered = false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_review, R.id.nav_taste, R.id.nav_info -> {
                supportActionBar!!.setDisplayShowTitleEnabled(true)
                fab_new_post.visibility = View.INVISIBLE
                select_date_layout.visibility = View.INVISIBLE
            }
            R.id.nav_mail -> {

            }
            else -> {
                supportActionBar!!.setDisplayShowTitleEnabled(false)
                fab_new_post.visibility = View.VISIBLE
                select_date_layout.visibility = View.VISIBLE
            }
        }

        when (item.itemId){
            R.id.nav_home -> {
                bar_month_text.text = "All"
                setNewFragment(viewModel.getFragment(item.itemId))
            }
            R.id.nav_review -> {
                setActionBarTitle("장소")
                setNewFragment(viewModel.getFragment(item.itemId))
            }
            R.id.nav_taste -> {
                setActionBarTitle("평가")
                setNewFragment(viewModel.getFragment(item.itemId))
            }

            R.id.nav_info -> {
                setActionBarTitle("정보")
                setNewFragment(viewModel.getFragment(item.itemId))
            }
            R.id.nav_mail -> {
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.type = "plain/Text"

                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("eunjanii@gmail.com"))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "<" + getString(R.string.app_name)
                        + " " + "피드백 전달" + ">")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "앱 버전 (AppVersion):" + getVersionInfo() + "\n기기명 (Device):\n안드로이드 OS (Android OS):\n내용 (Content):\n")
                emailIntent.type = "message/rfc822"
                startActivity(emailIntent)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun getVersionInfo(): String? {
        val info: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
        return info.versionName
    }

    private fun setNewFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }

    private fun initToolBar(){
        //툴바, 앱바, navigation drawer
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false);

        drawerToggle =  ActionBarDrawerToggle(this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerToggle.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        nav_view.menu.getItem(0).isChecked = true

    }

    private fun initHeaderView(){
        //헤더뷰
        //name, email
        val navHeaderView = nav_view.getHeaderView(0)
        val tvHeaderImg = navHeaderView.findViewById<ImageView>(R.id.user_img)
        tvHeaderImg.clipToOutline = true
        MyApplication.prefs.getString("user_img", "null").also {
            if(it != "null"){
                tvHeaderImg.setImageBitmap(ImageUtil.convert(it))
            }
        }

        val tvHeaderName =  navHeaderView.findViewById<TextView>(R.id.user_name_text).also {
            it.text = MyApplication.prefs.getString("user_name", "Name")
        }

        tvHeaderImg.setOnClickListener {
            viewModel.setPermission(it)
        }

        tvHeaderName.setOnClickListener {
            val dlg = UserNameDialog(this)
            dlg.setDialogListener(object : UserNameDialog.UserNameDialogListener{
                override fun onPositiveClicked(str: String) {
                    MyApplication.prefs.setString("user_name", str)
                    tvHeaderName.text = str
                }
            })
            dlg.start(tvHeaderName.text.toString())
        }

        bar_month_text.text = "All"

        //앱바의 뒤로가기 햄버거 버튼 표시하기
        supportFragmentManager.addOnBackStackChangedListener {

            if (supportFragmentManager.backStackEntryCount == 0) {
                enableViews(false)
                setActionBarTitle("장소")
            } else {
                enableViews(true)
            }
        }
    }


    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        val manager: FragmentManager = supportFragmentManager

        if(manager.backStackEntryCount > 0){
            manager.popBackStack()
        }else{
            //0인경우
            if(drawer_layout.isDrawerOpen(GravityCompat.START)){
                drawer_layout.closeDrawer(GravityCompat.START)

                return
            }
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true

            Toast.makeText(this, "한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT).show()
            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    fun setActionBarTitle(title: String?) {
        supportActionBar!!.title = title
    }
}