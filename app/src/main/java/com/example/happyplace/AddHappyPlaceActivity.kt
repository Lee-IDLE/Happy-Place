package com.example.happyplace

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.happyplace.databinding.ActivityAddHappyPlaceBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddHappyPlaceBinding

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAddPlace)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            binding.toolbarAddPlace.title = "ADD HAPPY PLACE"
        }

        binding.toolbarAddPlace.setNavigationOnClickListener{
            finish()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener{ view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_YEAR, dayOfMonth)
            updateDateInView()
        }
        binding.etDate.setOnClickListener(this@AddHappyPlaceActivity)
        binding.tvAddImage.setOnClickListener(this@AddHappyPlaceActivity)

        binding.tvAddImage.setOnClickListener{
            val intent = Intent(this@AddHappyPlaceActivity, CameraActivity::class.java)
            activityCameraResult.launch(intent)
        }
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
                        1 -> {
                            Toast.makeText(this@AddHappyPlaceActivity,
                                "Camera selection coming soon...",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                pictureDialog.show()
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

                    }
                }
                override fun onPermissionRationaleShouldBeShown
                            (permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    // 사용자에게 권한을 요청하는 이유 설명
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()

        // withListener 안에 객체를 넣어줘야 해서 자바로는 MultiplePermissionsListener() { 할일; }로 했지만
        // 코틀린에서는 object(객체): MultiplePermissionsListener(타입) { 할일 }으로 했다.
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

    private val activityCameraResult: ActivityResultLauncher<Intent>
    = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == RESULT_OK && result.data != null){
            val data = result.data
            val path = data?.getStringExtra("path") ?:""

            if(path.isNotEmpty()){
                try{
                    val bitmap = BitmapFactory.decodeFile(path)
                    binding.ivPlaceImage.setImageBitmap(bitmap)
                    binding.ivPlaceImage.rotation = 90f
                }catch (e: Exception) {
                    Log.e("Image Error", e.message, e)
                    e.printStackTrace()
                }


            }
        }
    }
}