package com.hokagelab.storyapp.ui.uploadstory

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.data.Resource
import com.hokagelab.storyapp.databinding.ActivityAddStoryBinding
import com.hokagelab.storyapp.ui.camerax.CameraActivity
import com.hokagelab.storyapp.ui.main.MainActivity
import com.hokagelab.storyapp.utils.StoryViewModelFactory
import com.hokagelab.storyapp.utils.Utils
import com.hokagelab.storyapp.utils.Utils.reduceFileImage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*


@RequiresApi(Build.VERSION_CODES.O)
class AddStoryActivity : AppCompatActivity() {

    private var _binding: ActivityAddStoryBinding? = null
    private val binding get() = _binding
    private var getFile: File? = null
    private var dialog: BottomSheetDialog? = null
    var token: String? = ""

    private val launchIntentGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImg: Uri = result.data?.data as Uri
                val file = uriToFile(selectedImg, this@AddStoryActivity)
                binding?.layoutShowImage?.visibility = View.VISIBLE
                binding?.layoutUploadImage?.visibility = View.GONE
                getFile = file
                binding?.ivPickedImage?.setImageURI(selectedImg)
                dialog?.dismiss()
                binding?.btnSendStory?.isEnabled = true
                binding?.btnSendStory?.setBackgroundResource(R.drawable.blue_button_active_background)
            }
        }

    private val launchIntentCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == CAMERA_X) {
                val file = it.data?.getSerializableExtra("picture") as File

                binding?.layoutShowImage?.visibility = View.VISIBLE
                binding?.layoutUploadImage?.visibility = View.GONE
                getFile = file
                binding?.ivPickedImage?.setImageBitmap(BitmapFactory.decodeFile(file.path))
                dialog?.dismiss()
                binding?.btnSendStory?.isEnabled = true
                binding?.btnSendStory?.setBackgroundResource(R.drawable.blue_button_active_background)
            }
        }

    private val addStoryViewModel: AddStoryViewModel by viewModels {
        StoryViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dialog = BottomSheetDialog(this)

        token = intent.getStringExtra("token")
        val bundle = Bundle()
        bundle.putString("token", token)

        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSION_CAMERA,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding?.btnImageUpload?.setOnClickListener {
            showButtonDialog()
        }

        binding?.btnRemoveImage?.setOnClickListener {
            binding?.layoutShowImage?.visibility = View.GONE
            binding?.layoutUploadImage?.visibility = View.VISIBLE
            binding?.btnSendStory?.isEnabled = false
            binding?.btnSendStory?.setBackgroundResource(R.drawable.blue_button_disable_background)
        }

        binding?.btnSendStory?.setOnClickListener {
            sendStory()
        }
    }

    @SuppressLint("InflateParams")
    private fun showButtonDialog(){
        val view = layoutInflater.inflate(R.layout.layout_pick_image_bottom_sheet, null)

        val layoutPhotoGallery = view.findViewById(R.id.layoutPhotoGallery) as LinearLayout
        val layoutCamera = view.findViewById(R.id.layoutCamera) as LinearLayout

        layoutPhotoGallery.setOnClickListener {
            pickImageFromGallery()
        }

        layoutCamera.setOnClickListener {
            takeImageUsingCamera()
        }

        dialog?.setContentView(view)
        dialog?.show()
    }

    private fun pickImageFromGallery(){
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"

        val chooseImage = Intent.createChooser(intent, "Pilih Gambar")
        launchIntentGallery.launch(chooseImage)
    }

    private fun takeImageUsingCamera() {
        val cameraXIntent = Intent(this@AddStoryActivity, CameraActivity::class.java)
        launchIntentCamera.launch(cameraXIntent)
    }

    private fun createTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(Utils.timeStamp, ".jpg", storageDir)
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun sendStory() {
        if (binding?.etImageDescription?.text?.toString()?.isEmpty() == true){
            val snackBar =
                binding?.let { Snackbar.make(it.layoutAddStory, getString(R.string.notif_description_empty), Snackbar.LENGTH_SHORT) }
            snackBar?.view?.setBackgroundColor(Color.parseColor("#FF4081"))
            snackBar?.setTextColor(Color.parseColor("#FFE2FF"))
            snackBar?.show()
        } else {
            val file = reduceFileImage(getFile as File)

            val description = binding?.etImageDescription?.text?.toString()?.toRequestBody("text/plain".toMediaType())
            val latitude = 0.toString().toRequestBody("text/plain".toMediaType())
            val longitude = 0.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )
            if (!token.equals("") && description != null) {
                token?.let {
                    addStoryViewModel.uploadStory(
                        it,
                        imageMultipart,
                        description,
                        latitude,
                        longitude
                    )
                        .observe(this) { result ->
                            when (result) {
                                is Resource.Loading -> {
                                    showLoadingProcess(true)
                                }

                                is Resource.Success -> {
                                    showLoadingProcess(false)
                                    val message = result.data.message
                                    val isError = result.data.error
                                    message.showMessage(isError)
                                }

                                is Resource.Error -> {
                                    showLoadingProcess(false)
                                    result.error.showMessage(true)
                                }
                            }
                        }
                }
            }
        }
    }

    private fun String.showMessage(isError: Boolean) {
        val dialog = Dialog(this@AddStoryActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_dialog)
        val body = dialog.findViewById(R.id.tvDialogInformation) as TextView
        body.text = this
        val iconDialog = dialog.findViewById(R.id.ivDialogLogo) as ImageView
        val confirmBtn = dialog.findViewById(R.id.btnDialogAction) as Button
        confirmBtn.visibility = View.VISIBLE
        if (!isError){
            iconDialog.setImageResource(R.drawable.ic_success)
            confirmBtn.setBackgroundResource(R.drawable.green_button_background)
        } else {
            iconDialog.setImageResource(R.drawable.ic_failed)
            confirmBtn.setBackgroundResource(R.drawable.red_button_background)
        }
        confirmBtn.setOnClickListener {
            if (!isError){
                val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("token", token)
                startActivity(intent)
                finish()
            }else {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showLoadingProcess(isLoading: Boolean) {
        binding?.btnSendStory?.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding?.progressBarSend?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS){
            if (!allPermissionGranted()){
                val snackBar =
                    binding?.let { Snackbar.make(it.layoutAddStory, getString(R.string.notif_request_camera_permission), Snackbar.LENGTH_SHORT) }
                snackBar?.view?.setBackgroundColor(Color.parseColor("#FF4081"))
                snackBar?.setTextColor(Color.parseColor("#FFE2FF"))
                snackBar?.show()
            }
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSION_CAMERA.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CAMERA_X = 200
        private val REQUIRED_PERMISSION_CAMERA = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}