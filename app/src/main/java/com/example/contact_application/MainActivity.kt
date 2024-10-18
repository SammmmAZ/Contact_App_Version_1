package com.example.contact_application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
    private lateinit var floatActionButton : FloatingActionButton
    private lateinit var recyclerView : RecyclerView

    // dp helper
    private lateinit var dbHelper : DbHelper

    // adapter class
    private lateinit var adapterClass : AdapterClass
    // get data from database
    private lateinit var contactList : List<ModelContact>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize views
        floatActionButton = findViewById(R.id.FloatingButton)
        recyclerView = findViewById(R.id.contactRv)

        // recylcer view details
        recyclerView.setHasFixedSize(true)

        // init dbHelper
        dbHelper = DbHelper(this)


        val sampleUriString = Uri.parse("android.resource://${packageName}/drawable/ic_sample_image").toString()

        // add event
        floatActionButton.setOnClickListener(){
            val intent = Intent(this, AddEditContactActivity::class.java)
            startActivity(intent)
        }

        // load data
        loadData();

        // Insert Sample Data
        insertSampleData()
    }

    private fun loadData() {
        dbHelper = DbHelper(this)
        contactList = dbHelper.getAllData()
        adapterClass = AdapterClass(this, contactList = contactList, dbHelper=dbHelper)
        recyclerView.setAdapter(adapterClass)
    }

    private fun insertSampleData() {
        val sampleUriString = Uri.parse("android.resource://${packageName}/drawable/ic_sample_image").toString()
        // Check if the database is empty
        contactList = dbHelper.getAllData()
        if (contactList.isEmpty()) {
            // Insert a sample contact
            dbHelper.insertContact(
                image = sampleUriString, // leave empty or provide a URI string
                name = "Example 1",
                email = "example@example.com",
                phone = "011111111",
                added_time = System.currentTimeMillis().toString(),
                updated_time = System.currentTimeMillis().toString()
            )
        }
    }

    public override fun onResume() {
        super.onResume()
        loadData() // to referesh data
    }
}