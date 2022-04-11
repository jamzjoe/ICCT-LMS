package com.icct.icctlms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.icct.icctlms.R
import com.icct.icctlms.data.TeacherPostData

class TeacherPostAdapter(private val postList: ArrayList<TeacherPostData>) : RecyclerView.Adapter<TeacherPostAdapter.MyViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{

        val postView = LayoutInflater.from(parent.context).inflate(R.layout.post_list, parent, false)
        return MyViewHolder(postView)
    }

    override fun onBindViewHolder(holder: TeacherPostAdapter.MyViewHolder, position: Int) {
        val currentItem = postList[position]

        holder.name.text = currentItem.name
        holder.date.text = currentItem.date
        holder.hour.text = currentItem.hours
        holder.type.text = currentItem.type
        holder.message.text = currentItem.message
        holder.personName.text = currentItem.personName

    }

    override fun getItemCount(): Int {
        return postList.size
    }

    class MyViewHolder(postView: View) : RecyclerView.ViewHolder(postView){
        val name: TextView = postView.findViewById(R.id.post_person_name)
        val date: TextView = postView.findViewById(R.id.post_date)
        val hour: TextView = postView.findViewById(R.id.post_hours)
        val type: TextView = postView.findViewById(R.id.post_type_display)
        val message: TextView = postView.findViewById(R.id.post_message_display)
        val personName: TextView = postView.findViewById(R.id.post_subject_title)

    }

}

