package com.example.contact_application

object Constants {

    // Database or db name
    const val DATABASE_NAME: String = "CONTACT"
    // Database version
    const val DATABASE_VERSION: Int = 3

    // Table name
    const val TABLE_NAME: String = "CONTACT_TABLE"

    // Table column or field names
    const val COLUMN_ID: String = "C_ID"
    const val COLUMN_IMAGE: String = "C_IMAGE"
    const val COLUMN_NAME: String = "C_NAME"
    const val COLUMN_PHONE: String = "C_PHONE"
    const val COLUMN_EMAIL: String = "C_EMAIL"
    const val COLUMN_ADDED_TIME: String = "C_DATE_ADDED"
    const val COLUMN_UPDATED_TIME: String = "C_UPDATED_TIME"

    // Create a query to create the table
    const val CREATE_TABLE: String = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_IMAGE TEXT,
            $COLUMN_NAME TEXT,
            $COLUMN_EMAIL TEXT,
            $COLUMN_PHONE TEXT,
            $COLUMN_ADDED_TIME TEXT,
            $COLUMN_UPDATED_TIME TEXT
        );
    """
}
