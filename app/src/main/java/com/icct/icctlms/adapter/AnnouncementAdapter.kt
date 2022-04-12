package com.icct.icctlms.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.icct.icctlms.R
import com.icct.icctlms.data.AnnouncementData


class AnnouncementAdapter(private val announcementList: ArrayList<AnnouncementData>) : RecyclerView.Adapter<AnnouncementAdapter.MyViewHolder>(){
    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(int: Int){

        announcementList.removeAt(int)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{

        val announcementView = LayoutInflater.from(parent.context).inflate(R.layout.announcement_item, parent, false)
        return MyViewHolder(announcementView, mListener)
    }

    override fun onBindViewHolder(holder: AnnouncementAdapter.MyViewHolder, position: Int) {
        val currentItem = announcementList[position]
        holder.title.text = currentItem.title
        holder.date.text = currentItem.date
        holder.description.text = currentItem.description
        holder.name.text = currentItem.announcerName
        holder.announcementID.text = currentItem.announcementID
        

    }

    override fun getItemCount(): Int {
        return announcementList.size
    }

    class MyViewHolder(announcementView: View, onItemClickListener: onItemClickListener) : RecyclerView.ViewHolder(announcementView){
        val title : TextView = announcementView.findViewById(R.id.title_ann)
        val date : TextView = announcementView.findViewById(R.id.date_ann)
        val description : TextView = announcementView.findViewById(R.id.description_ann)
        val name : TextView = announcementView.findViewById(R.id.announcer)
        val announcementID : TextView = announcementView.findViewById(R.id.annID)

        
        init {
            itemView.setOnClickListener{
                onItemClickListener.onItemClick(adapterPosition)
            }
        }

    }

}

