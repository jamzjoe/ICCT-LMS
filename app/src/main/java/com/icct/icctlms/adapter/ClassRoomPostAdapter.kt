package com.icct.icctlms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.R
import com.icct.icctlms.data.RoomPostData
import com.icct.icctlms.data.TeacherPostData

class ClassRoomPostAdapter(private val postList: ArrayList<RoomPostData>) : RecyclerView.Adapter<ClassRoomPostAdapter.MyViewHolder>(){

    private lateinit var mListener: ClassRoomPostAdapter.onItemClickListener
    private var position: Int = 0

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: ClassRoomPostAdapter.onItemClickListener){
        mListener = listener

    }

    fun deleteItem(int: Int){

        //remove post in a room
        val roomID = postList[position].roomID.toString()
        val postData = postList[position].roomPostID.toString()
        val databaseRoomPost = FirebaseDatabase.getInstance().getReference("Class Post").child("Room ID: $roomID")
        databaseRoomPost.child(postData).removeValue()
        postList.removeAt(int)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{

        val postView = LayoutInflater.from(parent.context).inflate(R.layout.room_post_layout, parent, false)
        return MyViewHolder(postView, mListener)
    }

    override fun onBindViewHolder(holder: ClassRoomPostAdapter.MyViewHolder, position: Int) {
        val currentItem = postList[position]

        holder.name.text = currentItem.name
        holder.type.text = currentItem.type
        holder.hour.text = currentItem.hours
        holder.date.text = currentItem.date
        holder.message.text = currentItem.message


    }

    override fun getItemCount(): Int {
        return postList.size
    }

    class MyViewHolder(postView: View, listener : onItemClickListener) : RecyclerView.ViewHolder(postView){

        val name : TextView = postView.findViewById(R.id.room_post_person_name)
        val type : TextView = postView.findViewById(R.id.room_post_type_display)
        val hour : TextView = postView.findViewById(R.id.room_post_hours)
        val date : TextView = postView.findViewById(R.id.room_post_date)
        val message : TextView = postView.findViewById(R.id.room_post_message_display)

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }

}

