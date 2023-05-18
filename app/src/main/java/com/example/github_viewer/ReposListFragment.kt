package com.example.github_viewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels

class ReposListFragment : Fragment() {
    private lateinit var exitButton: Button
    private val dataModel: DataModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_repos_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exitButton = view.findViewById(R.id.button_exit)
        exitButton.setOnClickListener {
            clearAuthToken()
            Toast.makeText(this.activity, "Выход из аккаунта", Toast.LENGTH_SHORT).show()
            dataModel.fragmentNum.value = 1
        }
    }

    private fun clearAuthToken() {
        val sharedPreferences = activity?.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.remove("token")
        editor?.apply()
    }

    companion object {
        fun newInstance() = ReposListFragment()
    }


}