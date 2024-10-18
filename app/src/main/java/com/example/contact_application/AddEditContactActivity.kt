package com.example.contact_application

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.sql.Date
import java.util.Locale

import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImage.ActivityResult
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

class AddEditContactActivity : AppCompatActivity() {

    // create views & buttons
    private lateinit var ProfileImage: CircleImageView
    private lateinit var ProfileName: EditText
    private lateinit var ProfileEmail: EditText
    private lateinit var ProfileNumber: EditText
    private lateinit var fab: FloatingActionButton

    // system variable for inputs
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var number: String
    private lateinit var Image: Bitmap

    // create an image uri
    private lateinit var imageURI: Uri

    // Create and Initialize permission constants
    private val CAMERA_PERMISSION_CODE = 100
    private val STORAGE_PERMISSION_CODE = 200
    private val IMAGE_FROM_GALLERY_PERMISSION_CODE = 300
    private val IMAGE_FROM_CAMERA_PERMISSION_CODE = 400

    // create a string array of permission codes
    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>

    // Database helper
    private lateinit var dbHelper: DbHelper

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            // Explain to the user that the feature is unavailable because the feature requires a permission that the user has denied.
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // ActivityResultLaunchers for gallery and camera
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                // Launch the crop activity with options
                val cropOptions = CropImageContractOptions(
                    it,
                    CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = false
                    )
                )
                cropImage.launch(cropOptions)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Launch the crop activity with options
            val cropOptions = CropImageContractOptions(
                imageURI,
                CropImageOptions(
                    imageSourceIncludeGallery = false,
                    imageSourceIncludeCamera = true
                )
            )
            cropImage.launch(cropOptions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_edit_contact_activity)

        // initialize permission arrays
        cameraPermission = arrayOf(Manifest.permission.CAMERA)
        storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // Your initialize views
        ProfileName = findViewById(R.id.ProfileName)
        ProfileImage = findViewById(R.id.ProfileImage)
        ProfileEmail = findViewById(R.id.ProfileEmail)
        ProfileNumber = findViewById(R.id.ProfileNumber)

        // initialize Buttons
        fab = findViewById(R.id.FloatingButton)

        // Initialize database helper
        dbHelper = DbHelper(this)

        // add events
        fab.setOnClickListener {
            saveData()
        }

        supportActionBar?.apply {
            title = "ADD CONTACTS"
            setDisplayHomeAsUpEnabled(true) // Enable the Up button
            setDisplayShowHomeEnabled(true)
        }

        // Get intent data
        val intent = intent
        val isEditMode = intent.getBooleanExtra("isEditMode", false)

        if (isEditMode) {
            // Set toolbar title
            actionBar?.title = "Update Contact"

            // Get the other values from the intent
            val id = intent.getStringExtra("ID")
            val name = intent.getStringExtra("NAME")
            val phone = intent.getStringExtra("PHONE")
            val email = intent.getStringExtra("EMAIL")
            val Image = intent.getStringExtra("IMAGE")

            val drawableUri = Uri.parse("android.resource://${packageName}/${R.drawable.baseline_account_circle_24}")

            // Set values in EditText fields
            ProfileName.setText(name)
            ProfileNumber.setText(phone)
            ProfileEmail.setText(email)

            // Set imageUri if image is not null or empty
            imageURI = if (!Image.isNullOrEmpty()) {
                Uri.parse(Image).also { uri ->
                    ProfileImage.setImageURI(uri)
                }
            } else {
                drawableUri.also {
                    ProfileImage.setImageURI(it)
                }
            }
        }

        // add event listener for profile Image
        ProfileImage.setOnClickListener {
            showImagePickerDialogue()
        }
    }

    private fun showImagePickerDialogue() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this).apply {
            setTitle("Choose an option")
            setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (!checkCameraPermission()) {
                            requestCameraPermission()
                        } else {
                            pickFromCamera()
                        }
                    }
                    1 -> {
                        if (!checkStoragePermission()) {
                            requestStoragePermission()
                        } else {
                            pickFromGallery()
                        }
                    }
                }
            }
            create()
            show()
        }
    }

    private fun checkCameraPermission(): Boolean {
        val result = packageManager.checkPermission(Manifest.permission.CAMERA, packageName) == PackageManager.PERMISSION_GRANTED
        val result1 = packageManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, packageName) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_PERMISSION_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        return packageManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, packageName) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_PERMISSION_CODE)
    }

    private fun saveData() {
        // Save data
        name = ProfileName.text.toString()
        number = ProfileNumber.text.toString()
        email = ProfileEmail.text.toString()

        // Check if name, email, and number are not empty
        if (name.isNotEmpty() && number.isNotEmpty() && email.isNotEmpty()) {
            // Generate timestamps for added time and updated time
            val addedTime = System.currentTimeMillis().toString()
            val updatedTime = System.currentTimeMillis().toString()

            // Insert data into the database
            val id = dbHelper.insertContact(
                imageURI.toString(),
                name,
                email,
                number,
                addedTime,
                updatedTime
            )

            if (id > 0) {
                // Show a toast message indicating that data has been added
                Toast.makeText(applicationContext, "Contact added to database", Toast.LENGTH_SHORT).show()
            } else {
                // Show a toast message indicating that there was an error
                Toast.makeText(applicationContext, "Error adding contact", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Show a toast message indicating that there are empty fields
            Toast.makeText(applicationContext, "Please fill in the empty fields", Toast.LENGTH_SHORT).show()
        }
    }

    // allows back button to navigate back to main page
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    // function to choose an image from gallery
    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        }
        galleryLauncher.launch(intent)
    }

    // function to activate camera
    private fun pickFromCamera() {
        // Create a file to save the image
        val photoFile: File? = createImageFile()

        // Continue only if the file was successfully created
        photoFile?.let {
            imageURI = FileProvider.getUriForFile(
                this,
                "com.example.contact_application.fileprovider",
                it
            )

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
            }
            // Ensure that the intent can only return images
            cameraIntent.resolveActivity(packageManager)?.also {
                cameraLauncher.launch(cameraIntent)
            }
        }
    }

    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(System.currentTimeMillis()))
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
        } catch (ex: IOException) {
            null
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        handleCropResult(result as ActivityResult) // Cast the result to the correct type
    }

    private fun handleCropResult(result: ActivityResult) {
        if (result.isSuccessful) {
            // Get the cropped image URI
            imageURI = result.uriContent!!

            // Set the image in the ImageView
            ProfileImage.setImageURI(imageURI)
        } else {
            // Handle errors
            val exception = result.error
            Toast.makeText(this, "Crop failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }

}
