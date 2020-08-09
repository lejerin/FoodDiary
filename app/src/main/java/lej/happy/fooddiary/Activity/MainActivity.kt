package lej.happy.fooddiary.Activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.fooddiary.fragment.ReviewDetailFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import lej.happy.fooddiary.Fragment.HomeFragment
import lej.happy.fooddiary.Fragment.ReviewFragment
import lej.happy.fooddiary.Fragment.TasteFragment
import lej.happy.fooddiary.Helper.DatePickerDialog
import lej.happy.fooddiary.R
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    lateinit var homeFragment: HomeFragment
    lateinit var reviewFragment: ReviewFragment
    lateinit var tasteFragment: TasteFragment

    private val REQUEST_CODE_ADD_POST = 11

    private var barYear: Int? = null
    private var barMonth: Int? = null

    private var drawerToggle: ActionBarDrawerToggle? = null
    private var mToolBarNavigationListenerIsRegistered = false

    private var doubleBackToExitPressedOnce = false

    private var nowFragment = 0

    companion object {

        lateinit var instance : MainActivity

        fun getInstancem() : MainActivity {

            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //어댑터에서 실행된 액티비티의 종료를 프래그먼트에 넘기기 위하여
        instance = this

        //툴바, 앱바, navigation drawer
        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false);

        drawerToggle =  ActionBarDrawerToggle(this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerToggle!!.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(drawerToggle!!)
        drawerToggle!!.syncState()

        nav_view.setNavigationItemSelectedListener(this)
       //
        nav_view.getMenu().getItem(0).setChecked(true);
        homeFragment = HomeFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, homeFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()


        //헤더뷰
        //name, email
        val navHeaderView = nav_view.getHeaderView(0)
        val tvHeaderName =  navHeaderView.findViewById<TextView>(R.id.user_name_text)
        val tvHeaderEmail = navHeaderView.findViewById<TextView>(R.id.user_email_text)

//        val userEmail = intent.getStringExtra("email")
//        MyApplication.prefs.setString("email", userEmail)
//        tvHeaderName.setText(intent.getStringExtra("name"))
//        tvHeaderEmail.setText(userEmail)

        initDateToNow()
        bar_month_text.text = "All"


        //리스너
        //플로팅 버튼
        fab_new_post.setOnClickListener(this)
        select_date_layout.setOnClickListener(this)

        //정렬 버튼
        app_bar_sort_btn.setOnClickListener(this)

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

    override fun onClick(v: View) {
        when (v.id) {

            R.id.fab_new_post ->{
                homeFragment.addNewPost()
            }
            R.id.select_date_layout ->{
                //앱바의 날짜를 선택했을 때 월 선택 다이얼로그 표시
                showDateSelectDialog()
            }
            R.id.app_bar_sort_btn -> {
                showPopupMenuOrder()
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        System.out.println("초기화 액티비티")
        if(resultCode == Activity.RESULT_OK ){
            refreshHome(requestCode, resultCode, data)
        }

    }


    fun refreshHome(requestCode: Int, resultCode: Int, data: Intent?){
        when(nowFragment){
            0 -> homeFragment.onActivityResult(requestCode, resultCode, data)
            1 -> reviewFragment.onActivityResult(requestCode, resultCode, data)
            else -> tasteFragment.onActivityResult(requestCode, resultCode, data)
        }


    }

    private fun showPopupMenuOrder(){
        val popup = PopupMenu(this@MainActivity, app_bar_sort_btn)
        popup.inflate(R.menu.sort_item)
        //adding click listener
        popup.setOnMenuItemClickListener { item ->

            System.out.println("클릭")
            when (item.itemId) {
                R.id.newest -> {
                    //handle menu1 click
                    supportFragmentManager.findFragmentById(R.id.frame_layout)?.let {
                        // the fragment exists
                        when(it){
                            is HomeFragment -> {
                                homeFragment.setOrder(true)
                            }
                            is ReviewFragment -> {
                                reviewFragment.setOrder(true)
                            }
                            is ReviewDetailFragment -> {
                                (it as (ReviewDetailFragment)).setOrder(true)
                            }
                            is TasteFragment -> {
                                (it as (TasteFragment)).setOrder(true)
                            }
                        }

                    }
                    true
                }
                R.id.oldest -> {

                    supportFragmentManager.findFragmentById(R.id.frame_layout)?.let {
                        // the fragment exists
                        when(it){
                            is HomeFragment -> {
                                (it as (HomeFragment)).setOrder(false)
                            }
                            is ReviewFragment -> {
                                (it as (ReviewFragment)).setOrder(false)
                            }
                            is ReviewDetailFragment -> {
                                (it as (ReviewDetailFragment)).setOrder(false)
                            }
                            is TasteFragment -> {
                                (it as (TasteFragment)).setOrder(false)
                            }
                        }

                    }

                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun enableViews(enable: Boolean) {
        if (enable) {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            drawerToggle!!.setDrawerIndicatorEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)

            if (!mToolBarNavigationListenerIsRegistered) {
                drawerToggle!!.setToolbarNavigationClickListener(View.OnClickListener { // Doesn't have to be onBackPressed
                    onBackPressed()
                })
                mToolBarNavigationListenerIsRegistered = true
            }
        } else {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            drawerToggle!!.setDrawerIndicatorEnabled(true)
            drawerToggle!!.setToolbarNavigationClickListener(null)
            mToolBarNavigationListenerIsRegistered = false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_home -> {
                getSupportActionBar()!!.setDisplayShowTitleEnabled(false)
                fab_new_post.visibility = View.VISIBLE
                select_date_layout.visibility = View.VISIBLE
            }
            else -> {
                getSupportActionBar()!!.setDisplayShowTitleEnabled(true)
                fab_new_post.visibility = View.INVISIBLE
                select_date_layout.visibility = View.INVISIBLE
            }
        }

        when (item.itemId){
            R.id.nav_home -> {
                bar_month_text.text = "All"
                nowFragment = 0
                homeFragment = HomeFragment()
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, homeFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()

            }
            R.id.nav_review -> {
                nowFragment = 1
                setActionBarTitle("장소")
                reviewFragment = ReviewFragment()
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, reviewFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            }
            R.id.nav_taste -> {
                nowFragment = 2
                setActionBarTitle("평가")
                tasteFragment = TasteFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, tasteFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    fun showDateSelectDialog(){
        val focusDialog = DatePickerDialog(this)
        focusDialog.setDialogListener(object : DatePickerDialog.CustomDialogListener {
            override fun onPositiveClicked(isOk: Boolean, year: Int, month: Int) {
                if (isOk) {
                    println("저장")
                    barYear = year
                    barMonth = month

                    if(fab_new_post.visibility == View.VISIBLE){
                        homeFragment.setHomeDate(false, year.toString(), month.toString())
                    }
                    bar_month_text.text = "" + year + "년 " + month + "월"
                }else{
                    //전체 선택
                    if(fab_new_post.visibility == View.VISIBLE){
                        homeFragment.setHomeDate(true, year.toString(), month.toString())
                    }
                    bar_month_text.text = "All"
                }
            }
        })

        focusDialog.showDialog(barYear!!, barMonth!!)
    }

    fun initDateToNow(){
        val nowYear = SimpleDateFormat("yyyy", Locale.KOREA).format(Date())
        val nowMonth = SimpleDateFormat("M", Locale.KOREA).format(Date())
        barYear = Integer.parseInt(nowYear)
        barMonth = Integer.parseInt(nowMonth)
        bar_month_text.text = "" + nowYear + "년 " + nowMonth + "월"
    }

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
            var toast = Toast.makeText(this, "한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT)

            toast.show()

            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)

        }
    }

    fun setActionBarTitle(title: String?) {
        getSupportActionBar()!!.setTitle(title)

    }
}