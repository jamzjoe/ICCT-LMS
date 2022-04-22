package com.icct.icctlms.adapter

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.icct.icctlms.R
import com.icct.icctlms.data.MessageData


class StudentMessageAdapter(private val messageList: ArrayList<MessageData>) : RecyclerView.Adapter<StudentMessageAdapter.MyViewHolder>(){
    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }
    fun deleteItem(int: Int){

        messageList.removeAt(int)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{

        val messageView = LayoutInflater.from(parent.context).inflate(R.layout.chat_message_item, parent, false)
        return MyViewHolder(messageView, mListener)
    }

    override fun onBindViewHolder(holder: StudentMessageAdapter.MyViewHolder, position: Int) {
        val currentItem = messageList[position]
        val type = messageList[position].type
        if (type == "sender"){
            //view visible when type is sender
                //set sender message to sender message
            holder.senderMessage.visibility = View.VISIBLE
            holder.senderMessage.text = currentItem.message

            holder.receiverMessage.visibility = View.GONE
            holder.receiverProfile.visibility = View.GONE

            holder.senderProfile.visibility = View.VISIBLE

            holder.rightDate.visibility = View.VISIBLE
            holder.rightDate.text = currentItem.dateTime
            holder.leftDate.visibility = View.GONE
        }else if (type == "receiver"){
            holder.receiverMessage.visibility = View.VISIBLE
            holder.receiverMessage.text = currentItem.message

            holder.senderMessage.visibility = View.GONE
            holder.senderProfile.visibility = View.GONE

            holder.receiverProfile.visibility = View.VISIBLE

            holder.leftDate.visibility = View.VISIBLE
            holder.leftDate.text = currentItem.dateTime
            holder.rightDate.visibility = View.GONE
        }



    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class MyViewHolder(messageView: View, onItemClickListener: onItemClickListener) : RecyclerView.ViewHolder(messageView){
        val receiverMessage: TextView = messageView.findViewById(R.id.receiver_message_text_view)
        val senderMessage: TextView = messageView.findViewById(R.id.sender_message_text_view)
        val senderProfile : ImageView = messageView.findViewById(R.id.sender_message_profile)
        val receiverProfile : ImageView = messageView.findViewById(R.id.receiver_message_profile)

        val rightDate : TextView = messageView.findViewById(R.id.right_date)

        val leftDate : TextView = messageView.findViewById(R.id.left_date)
        init {
            itemView.setOnClickListener{
                onItemClickListener.onItemClick(adapterPosition)
            }
        }

    }

}

