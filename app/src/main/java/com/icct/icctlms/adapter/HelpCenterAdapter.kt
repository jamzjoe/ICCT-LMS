package com.icct.icctlms.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.icct.icctlms.R
import com.icct.icctlms.data.AnnouncementData
import com.icct.icctlms.data.HelpCenterData


class HelpCenterAdapter(private val helpList: ArrayList<HelpCenterData>) : RecyclerView.Adapter<HelpCenterAdapter.MyViewHolder>(){



    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(int: Int){

        helpList.removeAt(int)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{

        val helpListView = LayoutInflater.from(parent.context).inflate(R.layout.response_layout, parent, false)
        return MyViewHolder(helpListView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = helpList[position]
        val questions = currentItem.questions
        val response = currentItem.response
        holder.questions.text = "Your question: $questions"
        holder.response.text = "Possible answer: $response"
        

    }

    override fun getItemCount(): Int {
        return helpList.size
    }

    class MyViewHolder(helpListView: View) : RecyclerView.ViewHolder(helpListView){
       val questions : TextView = helpListView.findViewById(R.id.question_text)
        val response : TextView = helpListView.findViewById(R.id.respone_text)


    }

}

