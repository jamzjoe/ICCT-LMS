package com.icct.icctlms.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.icct.icctlms.R
import com.icct.icctlms.data.GroupListData

class StudentGroupAdapter(private val groupList: ArrayList<GroupListData>) : RecyclerView.Adapter<StudentGroupAdapter.MyViewHolder>(){
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

        groupList.removeAt(int)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.group_layout, parent, false)

        //6. add parameter mListener
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = groupList[position]
        val roomID = groupList[position].roomID.toString()
        val getRoomMembers = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID)
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
        return groupList.size
    }

    //5. add parameter listener
    class MyViewHolder(itemView : View, listener : onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val title : TextView = itemView.findViewById(R.id.group_txt_title)
        val section : TextView = itemView.findViewById(R.id.create_group_section)
        val letter : TextView = itemView.findViewById(R.id.two_letter)
        val count : TextView = itemView.findViewById(R.id.group_count_members)
        //fourth
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }

    }

}