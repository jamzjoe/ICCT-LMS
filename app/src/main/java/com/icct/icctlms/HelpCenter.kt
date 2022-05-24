package com.icct.icctlms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase.*
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.adapter.HelpCenterAdapter
import com.icct.icctlms.data.HelpCenterData
import com.icct.icctlms.databinding.ActivityHelpCenterBinding
import com.icct.icctlms.tools.RandomCode

class HelpCenter : AppCompatActivity() {
    private lateinit var uid : String
    private lateinit var binding : ActivityHelpCenterBinding
    private lateinit var helpList : ArrayList<HelpCenterData>
    private lateinit var recyclerView: RecyclerView
    private lateinit var database : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHelpCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        uid = Firebase.auth.currentUser?.uid.toString()
        helpList = arrayListOf()

        recyclerView = binding.responseList
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)


        database = FirebaseDatabase.getInstance().getReference("Help Center").child(uid)
        viewResponse()
        sendQuestions()

    }

    private fun viewResponse() {
            database.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        helpList.clear()
                        for (each in snapshot.children){
                            val help = each.getValue(HelpCenterData::class.java)
                            helpList.add(help!!)
                        }

                        val adapter = HelpCenterAdapter(helpList)
                        recyclerView.adapter = adapter
                        helpList.sortBy{
                            it.sortKey
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun sendQuestions() {
        binding.sendResponse.setOnClickListener{
            val questions = binding.questionEt.text?.trim().toString()
            var response = ""
            var data = HelpCenterData()
            if(questions.contains("mahal")){
                response = "Tang ina mo magising ka na di ka niya mahal!"
            }else if(questions.contains("hi")){
                response = "Hello"
            }else if (questions.contains("hello")){
                response = "Hi"
            }else if (questions.contains("miss")){
                response = "Di ka niya miss sa true lang."
            }else if(questions.contains("love")){
                response = "I love you too.."
            }else if(questions.contains("hate")){
                response = "I hate you too..."
            }else if(questions.contains("balik")){
                response = "No masaya na ako may mahal na akong iba..."
            }else if(questions.contains("musta")){
                response = "Eto masaya sa piling ng iba, ikaw?"
            }
            else{
                response = "null"
            }
            data = HelpCenterData(questions, response, uid, RandomCode().sortKey)
            val setResponse = FirebaseDatabase.getInstance().getReference("Help Center").child(uid).child(RandomCode().randomCode())
                setResponse.setValue(data).addOnSuccessListener {
                binding.questionEt.text!!.clear()
                viewResponse()
            }
        }
    }

}