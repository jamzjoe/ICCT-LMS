package com.icct.icctlms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.icct.icctlms.R
import com.icct.icctlms.data.NotificationData


class NotificationAdapter(private val notificationList: ArrayList<NotificationData>) : RecyclerView.Adapter<NotificationAdapter.MyViewHolder>(){
    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }
    fun deleteItem(int: Int){

        notificationList.removeAt(int)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{

        val notificationView = LayoutInflater.from(parent.context).inflate(R.layout.notification_layout, parent, false)
        return MyViewHolder(notificationView, mListener)
    }

    override fun onBindViewHolder(holder: NotificationAdapter.MyViewHolder, position: Int) {
        val currentItem = notificationList[position]
        
        holder.description.text = currentItem.description
        holder.subject.text = currentItem.senderName
        holder.date.text = currentItem.dateTime

    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    class MyViewHolder(notificationView: View, onItemClickListener: onItemClickListener) : RecyclerView.ViewHolder(notificationView){
val subject: TextView = notificationView.findViewById(R.id.notif_subject)
val description: TextView = notificationView.findViewById(R.id.notif_desc)
val date : TextView = notificationView.findViewById(R.id.notification_date)
        init {
            itemView.setOnClickListener{
                onItemClickListener.onItemClick(adapterPosition)
            }
        }

    }

}

