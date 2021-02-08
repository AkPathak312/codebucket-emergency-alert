package com.example.emergencyalert.ui.mainscreen

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.emergencyalert.R
import com.example.emergencyalert.data.repository.AuthRepository
import com.example.emergencyalert.data.repository.UtilSharedPreferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_dash_board.*
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class DashBoardActivity : AppCompatActivity() {
    var imageUri: Uri? = null;
    var imageFile: File? = null;
    var bitmap:Bitmap?=null;
    var newPath: String? = null;
    var currentImagePath: String? = null;
    var file: File? = null;
    var fusedLocationProviderClient: FusedLocationProviderClient? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        Dexter.withContext(applicationContext)
            .withPermissions(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
//                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {

                }

            }).check();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application);
        captureimage.setOnClickListener {
            selectImage();
        }

        btnsave.setOnClickListener {
            checkLocation();
        }
    }

    fun selectImage() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // cameraIntent.setType("image/*")
        if (cameraIntent.resolveActivity(packageManager) != null) {
            imageFile = null;
            imageFile = getImageFi();
            if (imageFile != null) {
                imageUri = FileProvider.getUriForFile(
                    applicationContext,
                    "com.example.android.emergency",
                    imageFile!!
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, 101)
            }
        }

    }

    fun getImageFi(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageName = "jpg_+$timestamp"
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!;
        imageFile = File.createTempFile(imageName, ".jpg", storageDir)
        currentImagePath = imageFile!!.absolutePath
        file = File(currentImagePath);
        Log.e("Current_PATH", currentImagePath)
        return imageFile!!;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            val imageBitmap = BitmapFactory.decodeFile(currentImagePath);
            file=File(currentImagePath);
            // newPath= saveImageToInternalStorage(imageBitmap);
            newPath = getPath(saveImage(imageBitmap, "${UUID.randomUUID()}"))

            imageview.setImageBitmap(imageBitmap);
        }
    }


    fun checkLocation() {
        submitProgress.visibility = View.VISIBLE;
        val lm =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            submitProgress.visibility = View.INVISIBLE;
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Please Turn on GPS before Saving.")
                .show()
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                submitProgress.visibility = View.INVISIBLE
                return
            } else {
                var task = fusedLocationProviderClient!!.lastLocation;
                task.addOnSuccessListener { result ->
                    Log.d(
                        "TOken",
                        "checkLocation: " + UtilSharedPreferences.getAuthToken(applicationContext)
                    )
                    // uploadForm();
                    //Toast.makeText(applicationContext, result.latitude.toString(), Toast.LENGTH_SHORT).show()
                    if (currentImagePath == null) {
                        Toast.makeText(applicationContext, "No Image Selected", Toast.LENGTH_SHORT)
                            .show()
                        submitProgress.visibility = View.INVISIBLE
                        return@addOnSuccessListener
                    } else if (description.text.toString().isNullOrEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "No Description Given",
                            Toast.LENGTH_SHORT
                        ).show()
                        submitProgress.visibility = View.INVISIBLE
                        return@addOnSuccessListener
                    }
                    uploadForm(result.latitude,result.longitude)
//                    val response = AuthRepository().setProfileImage(
//                        "Bearer " + UtilSharedPreferences.getAuthToken(applicationContext),
//                        result.latitude,
//                        result.longitude,
//                        newPath!!,
//                        description.text.toString()
//                    );
//                    response.observe(this, androidx.lifecycle.Observer {
//                        Log.d("SubmitForm", "checkLocation: " + response.value);
//                        submitProgress.visibility = View.INVISIBLE
//                        Toast.makeText(applicationContext, "Form Submitted", Toast.LENGTH_SHORT)
//                            .show()
//                        description.setText("");
//                        imageview.setImageDrawable(getDrawable(R.drawable.placeholder));
//                    })
                }
            }
        }
    }

    fun uploadForm(lat:Double,lng:Double) {
        val lol = "/storage/emulated/0/Pictures/Screenshots/Screenshot_20210207-121440.png";
        val client: OkHttpClient = OkHttpClient().newBuilder()
            .build()
        val mediaType: MediaType? = MediaType.parse("text/plain")
        Log.d("newPath", "newPath: ${newPath} ")
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("fromLat", lat.toString())
            .addFormDataPart("fromLng", lng.toString())
            .addFormDataPart(
                "image", file!!.name + "." + file!!.extension,
                RequestBody.create(
                    MediaType.parse("file"),
                   file
                )
            )
            .addFormDataPart("imageDesc", description!!.text.toString())
            .build()
        val request: Request = Request.Builder()
            .url("http://androidform.herokuapp.com/user/form")
            .method("POST", body)
            .addHeader(
                "Authorization",
                "Bearer ${UtilSharedPreferences.getAuthToken(applicationContext)}"
            )
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                Log.d("Okhttp", "onFailure: " + e.localizedMessage)
                runOnUiThread {
                    submitProgress.visibility = View.INVISIBLE;
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("Okhttp", "onResponse: " + response.body()!!.string())
                runOnUiThread {
                    submitProgress.visibility = View.INVISIBLE;
                    description.setText("");
                    imageview.setImageDrawable(getDrawable(R.drawable.placeholder));
                }
            }
        })
    }

    // Method to save an image to internal storage
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        // Get the image from drawable resource as drawable object
        //val drawable = ContextCompat.getDrawable(applicationContext,drawableId)

        // Get the bitmap from drawable object

        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)

        // Initializing a new file
        // The bellow line return a directory in internal storage
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)


        // Create a file to save the image
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
        }

        // Return the saved image uri
        Log.d("Path", "saveImageToInternalStorage: " + file.absolutePath)
        return file.absolutePath
    }

    private fun saveImage(bitmap: Bitmap, title: String): Uri {
        val savedImageURL = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "${UUID.randomUUID()}",
            "sasa $title"
        )
        return Uri.parse(savedImageURL)
    }


    fun getPath(uri: Uri): String {
        var cursor: Cursor? = null;
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = applicationContext.contentResolver.query(uri, proj, null, null, null);
        val column = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor!!.moveToFirst()
        return cursor.getString(column)
    }
}


