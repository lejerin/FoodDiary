package lej.happy.fooddiary.Activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.fooddiary.fragment.ReviewDetailFragment
import com.google.android.material.navigation.NavigationView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import lej.happy.fooddiary.Fragment.HomeFragment
import lej.happy.fooddiary.Fragment.ReviewFragment
import lej.happy.fooddiary.Fragment.TasteFragment
import lej.happy.fooddiary.Helper.DatePickerDialog
import lej.happy.fooddiary.Helper.ImageUtil
import lej.happy.fooddiary.Helper.OpenSourceDialog
import lej.happy.fooddiary.Helper.UserNameDialog
import lej.happy.fooddiary.MyApplication
import lej.happy.fooddiary.R
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    lateinit var currentPhotoPath : String //문자열 형태의 사진 경로값 (초기값을 null로 시작하고 싶을 때 - lateinti var)


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
        val tvHeaderImg = navHeaderView.findViewById<ImageView>(R.id.user_img)
        tvHeaderImg.setClipToOutline(true)
        val user_bitmap = MyApplication.prefs.getString("user_img", "null")
        if(!user_bitmap.equals("null")){

            tvHeaderImg.setImageBitmap(ImageUtil.convert(user_bitmap))
        }

        val tvHeaderName =  navHeaderView.findViewById<TextView>(R.id.user_name_text)
        tvHeaderName.setText(MyApplication.prefs.getString("user_name", "Name"))


        tvHeaderImg.setOnClickListener {
            setPermission()
        }
        tvHeaderName.setOnClickListener {
            val dlg = UserNameDialog(this)
            dlg.setDialogListener(object : UserNameDialog.UserNameDialogListener{
                override fun onPositiveClicked(str: String) {
                    MyApplication.prefs.setString("user_name", str)
                    tvHeaderName.setText(str)
                }
            })
            dlg.start(tvHeaderName.text.toString())
        }

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


    val REQUEST_IMAGE_PICK = 10

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(resultCode == Activity.RESULT_OK){

            when(requestCode){

                REQUEST_IMAGE_PICK -> {
                    val bitmap = decodeSampledBitmapFromResource(data!!.data!!,150,150)
                    MyApplication.prefs.setString("user_img", ImageUtil.convert(bitmap))
                    user_img.setImageBitmap(bitmap)
                }

                else -> {
                    refreshHome(requestCode, resultCode, data)
                }

            }



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
            R.id.nav_review, R.id.nav_taste -> {
                getSupportActionBar()!!.setDisplayShowTitleEnabled(true)
                fab_new_post.visibility = View.INVISIBLE
                select_date_layout.visibility = View.INVISIBLE
            }
            else -> {
                getSupportActionBar()!!.setDisplayShowTitleEnabled(false)
                fab_new_post.visibility = View.VISIBLE
                select_date_layout.visibility = View.VISIBLE
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

            R.id.nav_license -> {
                val dlg = OpenSourceDialog(this)
                dlg.start()
            }

            R.id.nav_mail -> {
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.setType("plain/Text")

                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("eunjanii@gmail.com"))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "<" + getString(R.string.app_name)
                        + " " + "피드백 전달" + ">")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "앱 버전 (AppVersion):" + getVersionInfo() + "\n기기명 (Device):\n안드로이드 OS (Android OS):\n내용 (Content):\n")
                emailIntent.setType("message/rfc822")
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




    //프로필

    //테드 퍼미션 설정 (카메라 사용시 권한 설정 팝업을 쉽게 구현하기 위해 사용)
    private fun setPermission() {
        val permission = object : PermissionListener {
            override fun onPermissionGranted() {//설정해 놓은 위험권한(카메라 접근 등)이 허용된 경우 이곳을 실행
                openGallery()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {//설정해 놓은 위험권한이 거부된 경우 이곳을 실행
                Toast.makeText(this@MainActivity,"요청하신 권한이 거부되었습니다.",Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permission)
            .setRationaleMessage("카메라 앱을 사용하시려면 권한을 허용해주세요.")
            .setDeniedMessage("권한을 거부하셨습니다.앱을 사용하시려면 [앱 설정]-[권한] 항목에서 권한을 허용해주세요.")
            .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
            .check()
    }



    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = MediaStore.Images.Media.CONTENT_TYPE
        //galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK)
    }


    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {


        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledBitmapFromResource(
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {

        val getBitmap = BitmapFactory.Options().run {


            inJustDecodeBounds = true
            BitmapFactory.decodeStream(contentResolver.openInputStream(uri),null, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeStream(contentResolver.openInputStream(uri),null, this)!!

        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imgRotate(uri, getBitmap)
        } else {
            return getBitmap
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun imgRotate(uri: Uri, bitmap: Bitmap) : Bitmap {
        val ins = contentResolver.openInputStream(uri)
        val exif = ExifInterface(ins)
        ins?.close()

        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when(orientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}