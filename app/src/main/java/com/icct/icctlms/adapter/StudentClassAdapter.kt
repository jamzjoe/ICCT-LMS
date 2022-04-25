package com.icct.icctlms.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.R
import com.icct.icctlms.data.CreateClassData
import com.icct.icctlms.data.RoomPostData
class StudentClassAdapter(private val classList: ArrayList<CreateClassData>) : RecyclerView.Adapter<StudentClassAdapter.MyViewHolder>(){
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


        classList.removeAt(int)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.classes_item, parent, false)

        //6. add parameter mListener
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = classList[position]
        val roomID = classList[position].roomID.toString()
        val getRoomMembers = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
        val uid = Firebase.auth.currentUser?.uid.toString()
        val getPending = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Accept")
            .child(uid)
        getPending.get().addOnSuccessListener{
            if (it.exists()){
                val pending = it.child("accept").value.toString()
                if (pending != "true"){
                    holder.pending.text = "Pending"
                }else{
                    holder.pending.visibility = View.GONE
                }
            }
        }
        getRoomMembers.child("Members").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val count = snapshot.childrenCount
                    var plural = ""
                    plural = if (count > 1){
                        "$count members"
                    }else{
                        "$count member"
                    }

                    holder.title.text = currentItem.subjectTitle
                    holder.section.text = currentItem.section
                    holder.letter.text = currentItem.letter
                    holder.count.text = plural
                }else{
                    holder.title.text = currentItem.subjectTitle
                    holder.section.text = currentItem.section
                    holder.letter.text = currentItem.letter
                    holder.count.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    override fun getItemCount(): Int {
        return classList.size
    }

    //5. add parameter listener
    class MyViewHolder(itemView : View, listener : onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val title : TextView = itemView.findViewById(R.id.txt_title)
        val section : TextView = itemView.findViewById(R.id.create_class_section)
        val letter : TextView = itemView.findViewById(R.id.two_letter)
        val count : TextView = itemView.findViewById(R.id.room_count_members)
        val pending : TextView = itemView.findViewById(R.id.pending_text)
        //fourth
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }

    }

}