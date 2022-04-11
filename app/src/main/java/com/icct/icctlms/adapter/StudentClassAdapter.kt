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
        holder.title.text = currentItem.subjectTitle
        holder.section.text = currentItem.section
        holder.letter.text = currentItem.letter
    }

    override fun getItemCount(): Int {
        return classList.size
    }

//5. add parameter listener
    class MyViewHolder(itemView : View, listener : onItemClickListener) : RecyclerView.ViewHolder(itemView){
            val title : TextView = itemView.findViewById(R.id.txt_title)
    val section : TextView = itemView.findViewById(R.id.create_class_section)
    val letter : TextView = itemView.findViewById(R.id.two_letter)
//fourth
        init {
itemView.setOnClickListener{
    listener.onItemClick(adapterPosition)
}
        }

    }

}