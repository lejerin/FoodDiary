package lej.happy.fooddiary.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import lej.happy.fooddiary.Model.LicenseItem
import lej.happy.fooddiary.R


class LicenseAdapter(licenseList: MutableList<LicenseItem>) : RecyclerView.Adapter<LicenseAdapter.PhotoViewHolder>() {

    private var licenseList :  MutableList<LicenseItem> = licenseList
    private lateinit var context: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {

        context = parent!!.context


        var view = LayoutInflater.from(parent!!.context).inflate(R.layout.row_item_license,parent,false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {

        var now = licenseList!![position]
        holder.title.text = now.title
        holder.addr.text = now.addr
        holder.copy.text = now.copy
        holder.name.text = now.name

    }


    override fun getItemCount(): Int {
        return licenseList!!.size
    }


    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById(R.id.license_title) as TextView
        val addr = view.findViewById(R.id.license_addr) as TextView
        val copy = view.findViewById(R.id.license_copy) as TextView
        val name = view.findViewById(R.id.license_name) as TextView

    }

}

