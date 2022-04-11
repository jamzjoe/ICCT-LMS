package com.icct.icctlms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.icct.icctlms.R
import com.icct.icctlms.data.RoomMembersData


class MembersAdapter(private val memberList: ArrayList<RoomMembersData>) : RecyclerView.Adapter<MembersAdapter.MyViewHolder>(){
    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{

        val memberView = LayoutInflater.from(parent.context).inflate(R.layout.room_members_item, parent, false)
        return MyViewHolder(memberView, mListener)
    }

    override fun onBindViewHolder(holder: MembersAdapter.MyViewHolder, position: Int) {
        val currentItem = memberList[position]
        holder.name.text = currentItem.name
        holder.type.text = currentItem.type
        holder.uid.text = currentItem.uid
        

    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    class MyViewHolder(memberView: View, onItemClickListener: onItemClickListener) : RecyclerView.ViewHolder(memberView){
        
            
        val name : TextView = memberView.findViewById(R.id.members_name)
        val type : TextView = memberView.findViewById(R.id.members_type)
        val uid : TextView = memberView.findViewById(R.id.uid)
        init {
            itemView.setOnClickListener{
                onItemClickListener.onItemClick(adapterPosition)
            }
        }

    }

}

