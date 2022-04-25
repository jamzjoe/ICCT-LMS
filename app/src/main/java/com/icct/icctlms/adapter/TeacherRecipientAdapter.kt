package com.icct.icctlms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.icct.icctlms.R
import com.icct.icctlms.data.GroupListData
import com.icct.icctlms.data.RecipientData
import com.icct.icctlms.data.RoomMembersData
import com.icct.icctlms.data.UserData


class TeacherRecipientAdapter(private val memberList: ArrayList<UserData>) : RecyclerView.Adapter<TeacherRecipientAdapter.MyViewHolder>(){
    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }
    fun deleteItem(int: Int){

        memberList.removeAt(int)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{

        val recipientVi = LayoutInflater.from(parent.context).inflate(R.layout.recipient_list_item, parent, false)
        return MyViewHolder(recipientVi, mListener)
    }

    override fun onBindViewHolder(holder: TeacherRecipientAdapter.MyViewHolder, position: Int) {
        val currentItem = memberList[position]
        val type = currentItem.type
        val name = currentItem.name
        holder.name.text = name
        holder.type.text = type
        

    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    class MyViewHolder(recipientVi: View, onItemClickListener: onItemClickListener) : RecyclerView.ViewHolder(recipientVi){
        
            
        val name : TextView = recipientVi.findViewById(R.id.teacher_name)
        val type : TextView = recipientVi.findViewById(R.id.type)
        init {
            itemView.setOnClickListener{
                onItemClickListener.onItemClick(adapterPosition)
            }
        }

    }

}

