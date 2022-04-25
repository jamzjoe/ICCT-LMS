package com.icct.icctlms.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.icct.icctlms.R
import com.icct.icctlms.data.MessageData
import de.hdodenhof.circleimageview.CircleImageView


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
        val myColor = currentItem.colors
        val decode = Color.parseColor(myColor)
        val name = currentItem.senderName
        val senderName = currentItem.receiverName
        val userType = currentItem.userType
        val senderInfo = "$senderName ($userType)"
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
            holder.rightInitial.text = name!![0].toString()
            holder.rightColor.backgroundTintList = ColorStateList.valueOf(decode)
        }else if (type == "receiver"){
            holder.receiverMessage.visibility = View.VISIBLE
            holder.receiverMessage.text = currentItem.message

            holder.senderMessage.visibility = View.GONE
            holder.senderProfile.visibility = View.GONE

            holder.receiverProfile.visibility = View.VISIBLE

            holder.leftDate.visibility = View.VISIBLE
            holder.leftDate.text = senderInfo
            holder.rightDate.visibility = View.GONE
            holder.leftInitial.text = currentItem.receiverName!![0].toString()
            holder.leftColor.backgroundTintList = ColorStateList.valueOf(decode)

        }



    }

    override fun getItemCount(): Int {
        return messageList.size
    }


    class MyViewHolder(messageView: View, onItemClickListener: onItemClickListener) : RecyclerView.ViewHolder(messageView){
        val receiverMessage: TextView = messageView.findViewById(R.id.receiver_message_text_view)
        val senderMessage: TextView = messageView.findViewById(R.id.sender_message_text_view)
        val senderProfile : FrameLayout = messageView.findViewById(R.id.sender_message_profile)
        val receiverProfile : FrameLayout = messageView.findViewById(R.id.receiver_message_profile)

        val rightInitial : TextView = messageView.findViewById(R.id.right_initial)
        val leftInitial : TextView = messageView.findViewById(R.id.left_initial)


        val rightDate : TextView = messageView.findViewById(R.id.right_date)
        val leftDate : TextView = messageView.findViewById(R.id.left_date)

        val rightColor : MaterialCardView = messageView.findViewById(R.id.right_color)
        val leftColor : MaterialCardView = messageView.findViewById(R.id.left_color)
        init {
            itemView.setOnClickListener{
                onItemClickListener.onItemClick(adapterPosition)
            }
        }

    }

}

