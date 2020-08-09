package lej.happy.fooddiary.Activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
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
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_add_post.add_date_text
import kotlinx.android.synthetic.main.activity_add_post.circleAnimIndicator
import kotlinx.android.synthetic.main.activity_add_post.select_date_btn
import kotlinx.android.synthetic.main.activity_add_post.viewpager
import kotlinx.coroutines.*
import lej.happy.fooddiary.Adapter.ViewPagerAdapter
import lej.happy.fooddiary.DB.AppDatabase
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.DB.Entity.Thumb
import lej.happy.fooddiary.Helper.ImageUtil
import lej.happy.fooddiary.Helper.LoadingDialog
import lej.happy.fooddiary.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddPostActivity : AppCompatActivity() , View.OnClickListener{

    val REQUEST_IMAGE_CAPTURE = 1  //카메라 사진 촬영 요청 코드 *임의로 값 입력
    lateinit var currentPhotoPath : String //문자열 형태의 사진 경로값 (초기값을 null로 시작하고 싶을 때 - lateinti var)
    val REQUEST_IMAGE_PICK = 10
    private val REQUEST_CODE_OPEN_MAP_SEARCH = 44


    private var isSaving = false

    private val photoList = mutableListOf<Uri>()
    private val photoViewPagerAdapter = ViewPagerAdapter(photoList as ArrayList<Uri>, 0)

    //선택한 현재 사진 뷰페이저 index num
    private var selectIndicatorPhotoIndex = 0

    private var post = Post()
    private var thumb = Thumb()

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
                    setPermission()

                }else{
                    Toast.makeText(this, "사진은 최대 4장까지 추가 가능합니다", Toast.LENGTH_SHORT).show()
                }
            }
            //사진 제거
            R.id.remove_photo_btn -> {
               // photoViewPagerAdapter.removeItem(selectIndicatorPhotoIndex)

                photoList.removeAt(selectIndicatorPhotoIndex)
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



    private fun showChoicePhotoDialog(){
        val builder = AlertDialog.Builder(this@AddPostActivity)
        builder.setMessage("사진을 선택해주세요")
            .setCancelable(false)
            .setPositiveButton("카메라") { dialog, id ->
                takeCapture()
            }
            .setNegativeButton("앨범") { dialog, id ->
                // Dismiss the dialog
                openGallery()
            }
        val alert = builder.create()
        alert.show()
    }

    //수정일경우 데이터 입력하기
    private fun setModifyData(){
        //본문,사진,위치,주소,위치,시간대,평가 표시

        add_text.setText(post.texts)
        location_title_text.setText(post.location)
        add_address_text.visibility = View.VISIBLE
        add_address_text.text = post.address

        //뷰페이저 init
        photoViewPagerAdapter.setId(post.id!!)

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


        //사진 정보 db 갖고오기
        CoroutineScope(Job() + Dispatchers.Main).launch(Dispatchers.Default) {
            val result = async {
                getDataInDb()
            }.await()
            withContext(Dispatchers.Main) {
                // some UI thread work for when the background work is done
                thumb = result
            }
        }

    }


    //테드 퍼미션 설정 (카메라 사용시 권한 설정 팝업을 쉽게 구현하기 위해 사용)
    private fun setPermission() {
        val permission = object : PermissionListener {
            override fun onPermissionGranted() {//설정해 놓은 위험권한(카메라 접근 등)이 허용된 경우 이곳을 실행
                showChoicePhotoDialog()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {//설정해 놓은 위험권한이 거부된 경우 이곳을 실행
                Toast.makeText(this@AddPostActivity,"요청하신 권한이 거부되었습니다.",Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permission)
            .setRationaleMessage("카메라 앱을 사용하시려면 권한을 허용해주세요.")
            .setDeniedMessage("권한을 거부하셨습니다.앱을 사용하시려면 [앱 설정]-[권한] 항목에서 권한을 허용해주세요.")
            .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
            .check()
    }

    private fun getDataInDb() : Thumb{

        val getDb = AppDatabase.getInstance(this)

        return getDb.thumbDao().selectById(post.id!!)
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
        if(checkAllInput() && !isSaving){

            isSaving = true


            //로딩화면 시작
            loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            //db에 저장
            val getDb = AppDatabase.getInstance(this)

            //내용, count, locationname, photo uri
            post.texts = add_text.text.toString()
            post.location = location_title_text.text.toString()


            System.out.println("포스트 포토" + post.photo3)
            System.out.println("리스트 사이즈 " + photoList.size)


            if(checkUriValid(photoList[0])){
                post.photo = ImageUtil.convert(decodeSampledBitmapFromResource(photoList[0], 100, 100))

                post.photo1 = photoList[0].toString()
                thumb.photo1_bitmap = ImageUtil.convert(decodeSampledBitmapFromResource(photoList[0], 300, 300))
            }

            if(photoList.size > 1){
                if(checkUriValid(photoList[1])){
                    post.photo2 = photoList[1].toString()
                    thumb.photo2_bitmap = ImageUtil.convert(decodeSampledBitmapFromResource(photoList[1], 300, 300))
                }
            }else{
                post.photo2 = null
                thumb.photo2_bitmap = null
            }

            if(photoList.size > 2){
                if(checkUriValid(photoList[2])){
                    post.photo3 = photoList[2].toString()
                    thumb.photo3_bitmap = ImageUtil.convert(decodeSampledBitmapFromResource(photoList[2], 300, 300))
                }
            }else{
                post.photo3 = null
                thumb.photo3_bitmap = null
            }

            if(photoList.size > 3){
                if(checkUriValid(photoList[3])){
                    post.photo4 = photoList[3].toString()
                    thumb.photo4_bitmap = ImageUtil.convert(decodeSampledBitmapFromResource(photoList[3], 300, 300))
                }
            }else{
                post.photo4 = null
                thumb.photo4_bitmap = null
            }
            


            if(isModify){
                //수정
                val addRunnable = Runnable {
                    try {
                        getDb.postDao().update(post)

                        thumb.id = post.id
                        getDb.thumbDao().update(thumb)
                        loadingDialog.dismiss()
                        val resultIntent = Intent()
                        resultIntent.putExtra("modifyPost", post);
                        setResult(Activity.RESULT_OK, resultIntent)
                        isSaving = false
                        finish()
                    } catch (e: Exception){
                        //저장 실패
                        isSaving = false
                    }
                }
                val addThread = Thread(addRunnable)
                addThread.start()

            }else{
                //신규추가
                //count 센 뒤 저장
                val r = Runnable {
                    try {
                        post.count  = getDb.postDao().getCount(post.date!!) + 1
                        val addRunnable = Runnable {
                            try {
                                thumb.id = getDb.postDao().insert(post)
                                getDb.thumbDao().insert(thumb)
                                loadingDialog.dismiss()
                                setResult(Activity.RESULT_OK)
                                isSaving = false
                                finish()
                            } catch (e: Exception){
                                //저장 실패
                                isSaving = false
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

    fun checkUriValid(uri: Uri) : Boolean{
        var ls : InputStream? = null
        try {
            ls = contentResolver.openInputStream(uri)
        }catch (e: Exception){

        }

        if(ls == null)
            return false

        return true
    }


    //기본 카메라 앱을 사용해서 사진 촬영
    private fun takeCapture() {
        //기본 카메라 앱 실행
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile : File? = try{
                    createImageFile()
                }catch (e:Exception){
                    null
                }
                photoFile?.also {
                    val photoURI : Uri = FileProvider.getUriForFile(
                        this,
                        "lej.happy.fooddiary.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
    //이미지 파일 생성
    private fun createImageFile(): File {
        val timestamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir : File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timestamp}_",".jpeg",storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }






    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = MediaStore.Images.Media.CONTENT_TYPE
        //galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //사진을 갖고왔을 때
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_PICK && data != null) {
            photoList.add(data.data!!)
            photoViewPagerAdapter.notifyDataSetChanged()

            initIndicator()
            if(photoList.size == 1){
                add_photo_more_btn.visibility = View.VISIBLE
                remove_photo_btn.visibility = View.VISIBLE
                add_photo_btn.visibility = View.GONE
            }
        }

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            val bitmap : Bitmap
            val file = File(currentPhotoPath)
            if(Build.VERSION.SDK_INT < 28){//안드로이드 9.0 보다 버전이 낮을 경우
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver,Uri.fromFile(file))
              //  img_photo.setImageBitmap(bitmap)
            }else{//안드로이드 9.0 보다 버전이 높을 경우
                val decode = ImageDecoder.createSource(
                    this.contentResolver,
                    Uri.fromFile(file)
                )
                bitmap = ImageDecoder.decodeBitmap(decode)
              //  img_photo.setImageBitmap(bitmap)
            }
            savePhoto(bitmap)
            photoList.add(Uri.fromFile(file))
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

    //갤러리에 저장
    private fun savePhoto(bitmap: Bitmap) {
        //사진 폴더에 저장하기 위한 경로 선언
        val folderPath = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/"
        val timestamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "${timestamp}.jpeg"
        val folder = File(folderPath)
        if(!folder.isDirectory){//해당 경로에 폴더가 존재하지
            folder.mkdir() // make directory의 줄임말로 해당경로에 폴더 자동으로
        }
        //실제적인 저장 처리
        val out = FileOutputStream(folderPath + fileName)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        //Toast.makeText(this,"사진이 앨범에 저장되었습니다.",Toast.LENGTH_SHORT).show()
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
        var checkNum = 0


        //맛 입력되어있는지
        if(selectEmotionText == null){
            warning_taste_text.visibility = View.VISIBLE
            checkNum = 3
        }else{
            warning_taste_text.visibility = View.INVISIBLE
        }

        //위치 입력되어있는지
        if(location_title_text.text.toString().equals("")){
            warning_location_text.visibility = View.VISIBLE
            checkNum = 2
        }else{
            warning_location_text.visibility = View.INVISIBLE
        }

        //사진 한 장 이상인지
        if(photoList.size == 0){
            warning_photo_text.visibility = View.VISIBLE
            checkNum = 1
        }else{
            warning_photo_text.visibility = View.INVISIBLE
        }

        if(checkNum > 0){

            when(checkNum){
                1 -> Toast.makeText(this, "사진 한 장은 필수입니다.", Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(this, "위치를 입력해주세요.", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, "평가를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }

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