package com.example.contact_application

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.Nullable


class DbHelper(@Nullable context: Context?) : SQLiteOpenHelper(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        // Create table in the database
        db?.execSQL(Constants.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Update table for any structural changes made to it

        // Drop table if it exists
        db?.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME)
        // Create table again
        onCreate(db)
    }

    // Insert function to insert data into database
    fun insertContact(image: String, name: String, email: String, phone: String, added_time: String, updated_time: String): Long {
        // Get writable database to write data on db
        val db = this.writableDatabase

        // Create content value class object to write data
        val contentValues = ContentValues().apply {
            put(Constants.COLUMN_IMAGE, image)
            put(Constants.COLUMN_NAME, name)
            put(Constants.COLUMN_EMAIL, email)
            put(Constants.COLUMN_PHONE, phone)
            put(Constants.COLUMN_ADDED_TIME, added_time)
            put(Constants.COLUMN_UPDATED_TIME, updated_time)
        }

        // Insert data in row, return an id of the record
        val id = db.insert(Constants.TABLE_NAME, null, contentValues)

        // Close the database
        db.close()

        // Return an id
        return id
    }

    // Delete data by id
    fun deleteContact(id: String?) {
        // Get writable database
        val db = writableDatabase

        // Delete query
        db.delete(Constants.TABLE_NAME, "${Constants.COLUMN_ID} = ?", arrayOf(id.toString()))
        db.close()
    }

    // Retrieve all data
    fun getAllData(): ArrayList<ModelContact> {
        // Create an ArrayList to store the contacts
        val arrayList = ArrayList<ModelContact>()

        // SQL command query to select all records from the table
        val selectQuery = "SELECT * FROM ${Constants.TABLE_NAME}"

        // Get readable database
        val db = readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        // Loop through all records and add them to the list
        if (cursor.moveToFirst()) {
            do {
                // Create a new ModelContact object
                val modelContact = ModelContact(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_ID)).toString(),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_NAME)),
                    image = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_IMAGE)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PHONE)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EMAIL)),
                    addedDate = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_ADDED_TIME)),
                    updatedTime = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_UPDATED_TIME))
                )
                // Add the contact to the ArrayList
                arrayList.add(modelContact)
            } while (cursor.moveToNext())
        }

        // Close the cursor and database
        cursor.close()
        db.close()

        // Return the list of contacts
        return arrayList
    }
}
