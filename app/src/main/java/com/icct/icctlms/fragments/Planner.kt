package com.icct.icctlms.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.icct.icctlms.databinding.FragmentPlannerBinding

class Planner : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View {

        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }


}