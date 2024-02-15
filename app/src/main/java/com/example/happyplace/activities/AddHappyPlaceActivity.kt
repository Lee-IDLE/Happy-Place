package com.example.happyplace.activities

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.happyplace.R
import com.example.happyplace.database.DatabaseHandler
import com.example.happyplace.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplace.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddHappyPlaceBinding

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mlatitude: Double = 0.0
    private var mlongitude: Double = 0.0

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // toolbar 초기화
        setSupportActionBar(binding.toolbarAddPlace)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            binding.toolbarAddPlace.title = "ADD HAPPY PLACE"
        }

        binding.toolbarAddPlace.setNavigationOnClickListener{
            finish()
        }

        // 날짜 view 선택시 날짜 선택 다이얼로그 띄우고 사용자 선택 후 처리
        dateSetListener = DatePickerDialog.OnDateSetListener{ view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_YEAR, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()
        binding.etDate.setOnClickListener(this@AddHappyPlaceActivity)
        binding.tvAddImage.setOnClickListener(this@AddHappyPlaceActivity)
        binding.btnSave.setOnClickListener(this@AddHappyPlaceActivity)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.etDate -> {
                DatePickerDialog(this@AddHappyPlaceActivity, dateSetListener,
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_YEAR)).show()
            }
            R.id.tvAddImage -> {
                val pictureDialog = AlertDialog.Builder(this@AddHappyPlaceActivity)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery",
                    "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems){ dialog, which ->
                    when(which){
                        0 -> {choosePhotoFromGallery()}
                        1 -> {takePhoto()}
                    }
                }

                pictureDialog.show()
            }
            R.id.btnSave -> {
                when{
                    binding.etTitle.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    binding.etDescription.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                    }
                    binding.etLocation.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                    } else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            0,
                            binding.etTitle.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding.etDescription.toString(),
                            binding.etDate.text.toString(),
                            binding.etLocation.text.toString(),
                            mlatitude,
                            mlongitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                        if(addHappyPlace > 0){
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                }

            }
        }
    }

    // 사용자에게 갤러리에 있는 사진을 사용할 건지에 대한 권한을 물어본다
    private fun choosePhotoFromGallery(){
        Dexter.withContext(this@AddHappyPlaceActivity)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener{ // object를 써줬으니 ()는 필요 없다
                override fun onPermissionsChecked
                            (report: MultiplePermissionsReport?) {
                    // 권한이 모두 허가 되었는지 체크
                    if(report!!.areAllPermissionsGranted()){
                        // 갤러리에서 이미지 가져오기
                        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        //galleryIntent.setDataAndType(
                        //MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        //"image/*"
                        //)

                        activityResult.launch(galleryIntent)
                    }
                    else{
                        Toast.makeText(
                            this@AddHappyPlaceActivity,
                            "권한이 없습니다.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onPermissionRationaleShouldBeShown
                            (permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    // 사용자에게 권한을 요청하는 이유 설명
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun takePhoto(){
        Dexter.withContext(this@AddHappyPlaceActivity)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener{ // object를 써줬으니 ()는 필요 없다
                override fun onPermissionsChecked
                            (report: MultiplePermissionsReport?) {
                    // 권한이 모두 허가 되었는지 체크
                    if(report!!.areAllPermissionsGranted()){
                        // 카메라로 사진 찍어서 가져오기
                        val intent = Intent(this@AddHappyPlaceActivity, CameraActivity::class.java)
                        activityResult.launch(intent)
                    }
                    else{
                        Toast.makeText(
                            this@AddHappyPlaceActivity,
                            "권한이 없습니다.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onPermissionRationaleShouldBeShown
                            (permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    // 사용자에게 권한을 요청하는 이유 설명
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this@AddHappyPlaceActivity)
            .setMessage("It looks like you gave turned off permission required for this feature." +
                    "It can be enabled under the Application Settings.")
            .setPositiveButton("GO TO SETTINGS"){ _, _ ->
                try{
                    // 앱 설정 화면을 띄우도록 한다.
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){dialog, whitch ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateDateInView(){
        val myFormat = "yyyy.MM.dd"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())
    }

    private val activityResult: ActivityResultLauncher<Intent>
    = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        if(result.resultCode == RESULT_OK && result.data != null){
            val data = result.data

            // 갤러리
            result.data?.data?.let {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                saveImageToInternalStorage = saveImageToInternalStorage(bitmap)
                Log.i("Saved image: ", "Path :: $saveImageToInternalStorage")
                binding.ivPlaceImage.setImageURI(it)
            }

            // 카메라
            val path = data?.getStringExtra("path") ?:""
            if(path.isNotEmpty()){
                try{
                    val bitmap = BitmapFactory.decodeFile(path)
                    binding.ivPlaceImage.setImageBitmap(bitmap)
                    binding.ivPlaceImage.rotation = 90f

                    saveImageToInternalStorage = saveImageToInternalStorage(bitmap)
                    Log.i("Saved image: ", "Path :: $saveImageToInternalStorage")
                    // 사용 후 삭제(필요하다면)
                    val file = File(path)
                    file.delete()
                }catch (e: Exception) {
                    Log.e("Image Error", e.message, e)
                    e.printStackTrace()
                }


            }

        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        // MODE_PRIVATE: 같은 ID의 애플리케이션 혹은 호출된 어플리케이션에서만
        // 접근할 수 있도록 한다.
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        // UUID.randomUUID() 무작위 값
        file = File(file, "${UUID.randomUUID()}.jpg")

        try{
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

        }catch (e: IOException){
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }
}