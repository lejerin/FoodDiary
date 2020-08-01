package lej.happy.fooddiary.Activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_add_post.add_date_text
import kotlinx.android.synthetic.main.activity_add_post.circleAnimIndicator
import kotlinx.android.synthetic.main.activity_add_post.select_date_btn
import kotlinx.android.synthetic.main.activity_add_post.viewpager
import kotlinx.android.synthetic.main.activity_detail_post.*
import lej.happy.fooddiary.Adapter.ViewPagerAdapter
import lej.happy.fooddiary.DB.AppDatabase
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.Helper.ImageUtil
import lej.happy.fooddiary.Helper.LoadingDialog
import lej.happy.fooddiary.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddPostActivity : AppCompatActivity() , View.OnClickListener{

    private val REQUEST_CODE_PERMISSION = 22
    private val REQUEST_CODE_OPEN_GALLARY = 33
    private val REQUEST_CODE_OPEN_MAP_SEARCH = 44

    private val photoList = mutableListOf<Uri>()
    private val photoViewPagerAdapter = ViewPagerAdapter(photoList as ArrayList<Uri>)

    //선택한 현재 사진 뷰페이저 index num
    private var selectIndicatorPhotoIndex = 0

    private var post = Post()

    //선택한 맛
    private var selectEmotionLayout : ConstraintLayout? = null
    private var selectEmotionImg: ImageView? = null
    private var selectEmotionText: TextView? = null

    private var isModify = false

    lateinit var loadingDialog : LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        //뷰페이저
        viewpager.adapter = photoViewPagerAdapter
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                circleAnimIndicator.selectDot(position);
                selectIndicatorPhotoIndex = position
            }

        })

        //맛 선택하기 (최고, 만족, 별로)
        best_emotion_layout.setOnClickListener {
            chageEmotionImg(best_emotion_layout)
        }
        good_emotion_layout.setOnClickListener {
            chageEmotionImg(good_emotion_layout)
        }
        bad_emotion_layout.setOnClickListener {
            chageEmotionImg(bad_emotion_layout)
        }


        if(intent.getSerializableExtra("post") != null){
            //수정
            isModify = true
            post = (intent.getSerializableExtra("post") as Post)!!
            System.out.println(post!!.id)
            add_date_text.text =  SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(post.date)

            //본문,사진,위치,주소,위치,시간대,평가 표시
            setModifyData()

        }else{
            //신규 추가
            //오늘 날짜로 초기값 설정
            add_date_text.text =  SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(Date())
            post.date = StringtoDate(add_date_text.text.toString())
        }


        //사진 추가
        add_photo_btn.setOnClickListener(this)
        add_photo_more_btn.setOnClickListener(this)

        //사진 제거
        remove_photo_btn.setOnClickListener(this)

        //위치 검색 버튼
        map_search_btn.setOnClickListener(this)

        //날짜 선택 버튼
        select_date_btn.setOnClickListener(this)

        //뒤로가기 버튼
        add_back_btn.setOnClickListener(this)

        //선택시간대 고르기 (아침, 점심, 저녁, 간식, 야식)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radioButton1 -> post.time = 1
                R.id.radioButton2 -> post.time = 2
                R.id.radioButton3 -> post.time = 3
                R.id.radioButton4 -> post.time = 4
                R.id.radioButton5 -> post.time = 5
            }
        }


        //저장 버튼
        save_post_btn.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            //사진 추가
            R.id.add_photo_btn , R.id.add_photo_more_btn -> {
                if(photoList.size < 4){
                    checkAndRequestForPermission()
                }else{
                    Toast.makeText(this, "사진은 최대 4장까지 추가 가능합니다", Toast.LENGTH_SHORT).show()
                }
            }
            //사진 제거
            R.id.remove_photo_btn -> {
                photoViewPagerAdapter.removeItem(selectIndicatorPhotoIndex)
                viewpager.setAdapter(photoViewPagerAdapter)
                selectIndicatorPhotoIndex = 0
                initIndicator()
                if(photoList.size == 0){
                    add_photo_more_btn.visibility = View.INVISIBLE
                    remove_photo_btn.visibility = View.INVISIBLE
                    add_photo_btn.visibility = View.VISIBLE
                }
            }
            //위치 검색 버튼
            R.id.map_search_btn -> {
                val mapSearchIntent = Intent(this, MapSearchActivity::class.java)
                startActivityForResult(mapSearchIntent, REQUEST_CODE_OPEN_MAP_SEARCH)
            }
            //날짜 선택 버튼
            R.id.select_date_btn -> {
                showSelectDateDialog()
            }
            //뒤로가기
            R.id.add_back_btn -> {
                finish()
            }
            //저장하기
            R.id.save_post_btn -> {
                savePost()
            }
        }
    }

    //수정일경우 데이터 입력하기
    private fun setModifyData(){
        //본문,사진,위치,주소,위치,시간대,평가 표시

        add_text.setText(post.texts)
        location_title_text.setText(post.location)
        add_address_text.visibility = View.VISIBLE
        add_address_text.text = post.address

        //뷰페이저 init
        photoList.add(Uri.parse(post.photo1))
        if(post.photo2 != null)  photoList.add(Uri.parse(post.photo2))
        if(post.photo3 != null)  photoList.add(Uri.parse(post.photo3))
        if(post.photo4 != null)  photoList.add(Uri.parse(post.photo4))
        photoViewPagerAdapter.notifyDataSetChanged()

        initIndicator()

        if(post.time != null) initTime(post.time!!)
        initTaste(post.taste)

        add_photo_btn.visibility = View.INVISIBLE
        add_photo_more_btn.visibility = View.VISIBLE
        remove_photo_btn.visibility = View.VISIBLE


    }

    private fun initTime(num: Int){
        when(num){
            1 -> radioButton1.isChecked = true
            2 -> radioButton2.isChecked = true
            3 -> radioButton3.isChecked = true
            4 -> radioButton4.isChecked = true
            5 -> radioButton5.isChecked = true
        }
    }

    private fun initTaste(num: Int){
        when(num){
            1 -> {
                best_emotion_layout.performClick()
            }
            2 -> {
                good_emotion_layout.performClick()
            }
            3 -> {
                bad_emotion_layout.performClick()
            }
        }
    }

    private fun savePost(){
        //사진 저장
        if(checkAllInput()){
            //로딩화면 시작
            loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            //db에 저장
            val postDb = AppDatabase.getInstance(this)

            //내용, count, locationname, photo uri
            post.texts = add_text.text.toString()
            post.location = location_title_text.text.toString()
            post.photo1 = photoList[0].toString()
            if(photoList.size > 1) post.photo2 = photoList[1].toString()
            if(photoList.size > 2) post.photo3 = photoList[2].toString()
            if(photoList.size > 3) post.photo4 = photoList[3].toString()

            var getBitmap = decodeSampledBitmapFromResource(photoList[0], 100, 100)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getBitmap = imgRotate(photoList[0], getBitmap)
            }
            post.photo = ImageUtil.convert(getBitmap)

            if(isModify){
                //수정
                val addRunnable = Runnable {
                    try {
                        postDb?.postDao()?.update(post)
                        loadingDialog.dismiss()
                        val resultIntent = Intent()
                        resultIntent.putExtra("modifyPost", post);
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } catch (e: Exception){
                        //저장 실패
                    }
                }
                val addThread = Thread(addRunnable)
                addThread.start()

            }else{
                //신규추가
                //count 센 뒤 저장
                val r = Runnable {
                    try {
                        post.count  = postDb?.postDao()?.getCount(post.date!!)!! + 1
                        val addRunnable = Runnable {
                            try {
                                postDb?.postDao()?.insert(post)
                                loadingDialog.dismiss()
                                setResult(Activity.RESULT_OK)
                                finish()
                            } catch (e: Exception){
                                //저장 실패
                            }
                        }
                        val addThread = Thread(addRunnable)
                        addThread.start()
                    } catch (e: Exception) {

                    }
                }

                val thread = Thread(r)
                thread.start()
            }

        }
    }

    //갤러리 사용자 권한 확인
    private fun checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        //galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(galleryIntent, REQUEST_CODE_OPEN_GALLARY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //사진을 갖고왔을 때
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_OPEN_GALLARY && data != null) {
            photoList.add(data.data!!)
            photoViewPagerAdapter.notifyDataSetChanged()

            initIndicator()
            if(photoList.size == 1){
                add_photo_more_btn.visibility = View.VISIBLE
                remove_photo_btn.visibility = View.VISIBLE
                add_photo_btn.visibility = View.GONE
            }
        }
        //주소 검색을 완료했을 때
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_OPEN_MAP_SEARCH && data != null) {
            post.address = data.getStringExtra("name")
            add_address_text.text = post.address
            add_address_text.visibility = View.VISIBLE
            post.location = data.getStringExtra("roadAddress")
            location_title_text.setText(post.location)

            post.x = data.getStringExtra("x").toDouble()
            post.y = data.getStringExtra("y").toDouble()

        }

    }

    private fun initIndicator() {

        circleAnimIndicator.removeDotPanel()
        //원사이의 간격
        circleAnimIndicator.setItemMargin(15);
        //애니메이션 속도
        circleAnimIndicator.setAnimDuration(300);
        //indecator 생성
        circleAnimIndicator.createDotPanel(photoList.size, R.drawable.viewpage_indicator_off , R.drawable.viewpager_indicator_on);
        circleAnimIndicator.selectDot(selectIndicatorPhotoIndex);
    }

    private fun showSelectDateDialog(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            // Display Selected date in textbox
            var monthtext = (monthOfYear + 1).toString()
            if(monthtext.length == 1){
                monthtext = "0" + monthtext
            }
            var daytext = dayOfMonth.toString()
            if(daytext.length == 1){
                daytext = "0" + daytext
            }
            add_date_text.text = "" + year + "년 " + (monthOfYear + 1)  + "월 " + dayOfMonth + "일"
            post.date = StringtoDate(add_date_text.text.toString())

        }, year, month, day)

        dpd.show()
    }

    private fun chageEmotionImg(a: ConstraintLayout){
        if(selectEmotionLayout != null){
            selectEmotionLayout!!.setBackgroundResource(R.drawable.emotion_background)
            selectEmotionText!!.setTextColor(ContextCompat.getColor(this,
                R.color.brightGray))
            selectEmotionImg!!.setColorFilter(ContextCompat.getColor(this,
                R.color.brightGray));
        }
        selectEmotionLayout = a
        if(a == best_emotion_layout){
            post.taste = 1
            selectEmotionImg = best_emotion
            selectEmotionText = best_emotion_text
        }
        else if(a == good_emotion_layout) {
            post.taste = 2
            selectEmotionImg = good_emotion
            selectEmotionText = good_emotion_text
        }
        else{
            post.taste = 3
            selectEmotionImg = bad_emotion
            selectEmotionText = bad_emotion_text
        }


        selectEmotionLayout!!.setBackgroundResource(R.drawable.emotion_background_selected)
        selectEmotionImg!!.setColorFilter(ContextCompat.getColor(this,
            R.color.colorPrimary));
        selectEmotionText!!.setTextColor(ContextCompat.getColor(this,
            R.color.colorPrimary))
    }


    private fun checkAllInput(): Boolean {
        var checkNum = false

        //사진 한 장 이상인지
        if(photoList.size == 0){
            warning_photo_text.visibility = View.VISIBLE
            checkNum = true
        }else{
            warning_photo_text.visibility = View.INVISIBLE
        }
        //위치 입력되어있는지
        if(location_title_text.text.toString().equals("")){
            warning_location_text.visibility = View.VISIBLE
            checkNum = true
        }else{
            warning_location_text.visibility = View.INVISIBLE
        }

        //맛 입력되어있는지
        if(selectEmotionText == null){
            warning_taste_text.visibility = View.VISIBLE
            checkNum = true
        }else{
            warning_taste_text.visibility = View.INVISIBLE
        }

        if(checkNum){
            return false
        }
        return true

    }


    fun StringtoDate(str: String, dateFormat: String = "yyyy년 M월 d일", timeZone: TimeZone = TimeZone.getDefault()): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(str)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        System.out.println("사진계산")


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

        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {

            inJustDecodeBounds = true
            BitmapFactory.decodeStream(contentResolver.openInputStream(uri),null, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeStream(contentResolver.openInputStream(uri),null, this)!!
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