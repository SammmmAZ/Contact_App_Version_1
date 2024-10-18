package com.example.contact_application

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class AdapterClass(
    private val context: Context,
    private val contactList: List<ModelContact>,
    private val dbHelper: DbHelper
) : RecyclerView.Adapter<AdapterClass.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // create views for the contact item
        val profileImage: ImageView = itemView.findViewById(R.id.CImage)
        val contactName: TextView = itemView.findViewById(R.id.Cname)
        val contactEdit: ImageView = itemView.findViewById(R.id.Cedit)
        val contactDelete: ImageView = itemView.findViewById(R.id.Cdelete)
        val relativeLayout: View = itemView.findViewById(R.id.RelativeLayout)
        val classicProfileImage : CircleImageView = itemView.findViewById(R.id.ClassicProfImage)
    }

    // onCreateViewHolder method
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        // uses a layout inflater to inflate the contact card view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item_card, parent, false)
        return ContactViewHolder(view)
    }

    // onBindViewHolder method
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        // get data from the contactList
        val contact = contactList[position]

        // set data in the views
        holder.contactName.text = contact.name
        if (contact.image.isNullOrEmpty()) {
            holder.profileImage.setImageResource(R.drawable.baseline_account_circle_24) // Default image
        } else {
            holder.profileImage.setImageURI(Uri.parse(contact.image))
        }

        // handle item click to show contact details
        holder.relativeLayout.setOnClickListener {
            val intent = Intent(context, ContactDetail::class.java)
            intent.putExtra("contactId", contact.id) // Assuming contact has an `id`
            context.startActivity(intent)
            Toast.makeText(context, "Clicked on ${contact.name}", Toast.LENGTH_SHORT).show()
        }

        // handle edit button click
        holder.contactEdit.setOnClickListener {
            val intent = Intent(context, AddEditContactActivity::class.java)
            intent.putExtra("ID", contact.id)
            intent.putExtra("NAME", contact.name)
            intent.putExtra("PHONE", contact.phone)
            intent.putExtra("EMAIL", contact.email)
            intent.putExtra("ADDEDTIME", contact.addedDate)
            intent.putExtra("UPDATEDTIME", contact.updatedTime)
            intent.putExtra("IMAGE", contact.image)
            intent.putExtra("isEditMode", true)
            context.startActivity(intent)
        }

        // handle delete button click
        holder.contactDelete.setOnClickListener {
            dbHelper.deleteContact(contact.id) // Assuming deleteContact takes an id
            (context as MainActivity).onResume() // Refresh the main activity
        }
    }

    // getItemCount method
    override fun getItemCount(): Int {
        return contactList.size
    }
}
