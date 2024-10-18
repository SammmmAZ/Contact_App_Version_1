package com.example.contact_application

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ContactDetail : AppCompatActivity() {

    // View variables
    private lateinit var profileIv: ImageView
    private lateinit var nameTv: TextView
    private lateinit var phoneTv: TextView
    private lateinit var emailTv: TextView
    private lateinit var addedTimeTv: TextView
    private lateinit var updatedTimeTv: TextView

    private lateinit var id: String

    // Database helper
    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contact_detail_activity)

        // Initialize the database helper
        dbHelper = DbHelper(this)

        // Get data from intent
        val intent = intent
        id = intent.getStringExtra("contactId") ?: ""

        // Initialize views
        nameTv = findViewById(R.id.nameTv)
        phoneTv = findViewById(R.id.ProfilePhone)
        emailTv = findViewById(R.id.ProfileEmail)
        addedTimeTv = findViewById(R.id.ProfileaddedTime)
        updatedTimeTv = findViewById(R.id.updatedTimeTv)
        profileIv = findViewById(R.id.profileIv)

        // Load data by ID
        loadDataById()
    }

    private fun loadDataById() {
        // Query to find data by ID
        val selectQuery = "SELECT * FROM ${Constants.TABLE_NAME} WHERE ${Constants.COLUMN_ID} = \"$id\""

        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                // Get data from the cursor
                val name = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_NAME))
                val image = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_IMAGE))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PHONE))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EMAIL))
                val addTime = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_ADDED_TIME))
                val updateTime = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_UPDATED_TIME))

                // Convert time to dd/MM/yy hh:mm:aa format
                val calendar = Calendar.getInstance(Locale.getDefault())

                calendar.timeInMillis = addTime.toLong()
                val timeAdd = DateFormat.format("dd/MM/yy hh:mm:aa", calendar)

                calendar.timeInMillis = updateTime.toLong()
                val timeUpdate = DateFormat.format("dd/MM/yy hh:mm:aa", calendar)

                // Set data to the views
                nameTv.text = name
                phoneTv.text = phone
                emailTv.text = email
                addedTimeTv.text = timeAdd
                updatedTimeTv.text = timeUpdate

                // Handle profile image
                if (image == "null") {
                    val drawableUri = Uri.parse("android.resource://${packageName}/${R.drawable.baseline_account_circle_24}")
                    profileIv.setImageURI(drawableUri)
                } else {
                    profileIv.setImageURI(Uri.parse(image))
                }

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
    }
}
