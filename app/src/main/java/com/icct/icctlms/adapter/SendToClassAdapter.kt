package com.icct.icctlms.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.R
import com.icct.icctlms.data.CreateClassData
import com.icct.icctlms.data.RoomPostData
class SendToClassAdapter(private val classList: ArrayList<CreateClassData>) : RecyclerView.Adapter<SendToClassAdapter.MyViewHolder>(){
//third
    private lateinit var mListener: onItemClickListener
    private var position: Int = 0

    //second
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
//first
    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener

    }
    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(int: Int){
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()
        val roomID = classList[position].roomID.toString()

        //remove class
        val databaseGroup = FirebaseDatabase.getInstance().getReference("Class").child(uid)
        databaseGroup.child(roomID).removeValue()
        //remove post in a class
        val databaseRoomPost = FirebaseDatabase.getInstance().getReference("Room ID: $roomID").child("Post")
        databaseRoomPost.removeValue()




        classList.removeAt(int)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.send_to_layout, parent, false)

        //6. add parameter mListener
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = classList[position]
        holder.title.text = currentItem.subjectTitle
        holder.section.text = currentItem.section
    }

    override fun getItemCount(): Int {
        return classList.size
    }

//5. add parameter listener
    class MyViewHolder(itemView : View, listener : onItemClickListener) : RecyclerView.ViewHolder(itemView){
            val title: TextView = itemView.findViewById(R.id.send_title)
    val section : TextView = itemView.findViewById(R.id.send_section)
//fourth
        init {
itemView.setOnClickListener{
    listener.onItemClick(adapterPosition)
}
        }

    }

}